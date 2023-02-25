package io.eordie.multimodule.example.repository

import io.eordie.multimodule.example.contracts.basic.exception.EntityNotFoundException
import io.eordie.multimodule.example.contracts.basic.exception.ValidationError
import io.eordie.multimodule.example.contracts.basic.exception.ValidationException
import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.example.filter.FiltersRegistry
import io.eordie.multimodule.example.repository.entity.CreatedByIF
import io.eordie.multimodule.example.repository.entity.DeletedIF
import io.eordie.multimodule.example.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.example.repository.entity.Permission
import io.eordie.multimodule.example.repository.entity.PermissionAware
import io.eordie.multimodule.example.rsocket.context.getAuthenticationContext
import io.eordie.multimodule.example.utils.asFlow
import io.eordie.multimodule.example.validation.EntityValidator
import io.konform.validation.ValidationResult
import io.micronaut.context.ApplicationContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.inject.qualifiers.Qualifiers
import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.singleOrNull
import org.babyfish.jimmer.DraftConsumer
import org.babyfish.jimmer.kt.toImmutableProp
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.runtime.DraftSpi
import org.babyfish.jimmer.runtime.ImmutableSpi
import org.babyfish.jimmer.runtime.Internal
import org.babyfish.jimmer.sql.ast.Selection
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.ast.tuple.Tuple6
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.constant
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.mutation.KSimpleSaveResult
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Suppress("TooManyFunctions")
open class KFactoryImpl<T : Any, ID : Comparable<ID>>(
    val entityType: KClass<T>,
    private val immutableType: ImmutableType
) : KFactory<T, ID> {

    @Inject
    private lateinit var context: ApplicationContext

    lateinit var sql: KSqlClient

    @Inject
    private lateinit var conversionService: ConversionService

    @Inject
    private lateinit var registryProvider: Provider<FiltersRegistry>

    private val validator: EntityValidator<T>? by lazy {
        context.findBean(
            Argument.of(EntityValidator::class.java, entityType.java) as Argument<EntityValidator<T>>
        ).getOrNull()
    }

    protected val registry: FiltersRegistry get() = registryProvider.get()

    companion object {
        private const val MAX_ALLOWED_SORTING = 5
        private const val MAX_LIMIT = 100
        private val CREATED_BY_PROPERTY = CreatedByIF::createdBy.toImmutableProp().id
        private val PERMISSIONS_PROPERTY = PermissionAware::permissions.toImmutableProp().id
        private val ORGANIZATION_ID_PROPERTY = OrganizationOwnerIF::organizationId.toImmutableProp().id
    }

    open val datasourceName = "default"
    open val defaultFetcher: Fetcher<T>? = null
    open fun sortingExpressions(table: KNonNullTable<T>): List<KPropExpression<out Comparable<*>>> = listOf(idProperty)

    private val isCreatedByAware = entityType.isSubclassOf(CreatedByIF::class)
    private val isPermissionAware = entityType.isSubclassOf(PermissionAware::class)
    private val isOrganizationOwnerAware = entityType.isSubclassOf(OrganizationOwnerIF::class)

    internal lateinit var idProperty: KPropExpression<ID>
    private lateinit var sortingExpressionsValue: Map<String, KPropExpression<out Comparable<*>>>

    @PostConstruct
    fun init() {
        sql = context.getBean(KSqlClient::class.java, Qualifiers.byName(datasourceName))
        sql.createQuery(entityType) {
            idProperty = table.getId()
            sortingExpressionsValue = sortingExpressions(table).associateBy { it.name() }
            select(constant(1))
        }
    }

    open fun listPredicates(context: CoroutineContext, table: KNonNullTable<T>): List<KNonNullExpression<Boolean>> =
        emptyList()

    open fun calculatePermissions(auth: AuthenticationDetails, values: List<T>): Map<ID, Set<Permission>> {
        return if (isPermissionAware) {
            values.associateBy({ it.getId() }, { calculatePermissions(auth, it) })
        } else {
            emptyMap()
        }
    }

    private fun T.getId(): ID = (this as ImmutableSpi).__get(idProperty.name()) as ID

    open fun calculatePermissions(auth: AuthenticationDetails, value: T): Set<Permission> {
        return if (isPermissionAware) {
            throw UnsupportedOperationException("should be implemented by factory")
        } else {
            emptySet()
        }
    }

    private fun <D> produce(context: CoroutineContext, base: T? = null, block: (D) -> Unit): D where D : T =
        produce(base, block) { context.getAuthenticationContext() }

    private fun <D> produce(
        base: T? = null,
        block: (D) -> Unit,
        authSupplier: () -> AuthenticationDetails
    ): D where D : T {
        val consumer = DraftConsumer<D> {
            block(it)
            if (base == null) {
                if (isCreatedByAware && !(it as DraftSpi).__isLoaded(CREATED_BY_PROPERTY)) {
                    it.__set(CREATED_BY_PROPERTY, authSupplier().userId)
                }
                if (isOrganizationOwnerAware && !(it as DraftSpi).__isLoaded(ORGANIZATION_ID_PROPERTY)) {
                    it.__set(ORGANIZATION_ID_PROPERTY, authSupplier().currentOrganizationId)
                }
            }
        }
        return Internal.produce(immutableType, base, consumer) as D
    }

    private fun applyPermissions(
        values: List<T>,
        context: CoroutineContext,
        permission: Permission = Permission.VIEW
    ): List<T> {
        return if (values.isEmpty() || !isPermissionAware) values else {
            val auth = context.getAuthenticationContext()
            val index = calculatePermissions(auth, values)
            values.mapNotNull { value ->
                val permissions = index[value.getId()].orEmpty()
                if (!permissions.contains(permission)) null else {
                    val block: (T) -> Unit = {
                        (it as DraftSpi).__set(PERMISSIONS_PROPERTY, permissions.toList())
                    }
                    produce<T>(value, block) { auth }
                }
            }
        }
    }

    private fun applyPermissions(
        value: T,
        context: CoroutineContext,
        permission: Permission = Permission.VIEW
    ): T? = applyPermissions(listOf(value), context, permission).firstOrNull()

    override suspend fun existsById(id: ID): Boolean {
        return findById(id) != null
    }

    override suspend fun findById(id: ID, fetcher: Fetcher<T>?): T? {
        val targetFetcher = fetcher ?: defaultFetcher
        val value = if (targetFetcher != null) {
            sql.entities.findById(targetFetcher, id)
        } else {
            sql.entities.findById(entityType, id)
        }

        return value?.let { applyPermissions(it, coroutineContext) }
    }

    override suspend fun getById(id: ID, fetcher: Fetcher<T>?): T {
        return requireNotNull(findById(id, fetcher))
    }

    override suspend fun deleteById(id: ID): Boolean {
        return deleteByIds(listOf(id)) > 0
    }

    override suspend fun deleteByIds(ids: Collection<ID>): Int {
        return if (ids.isEmpty()) 0 else {
            return sql.createUpdate(entityType) {
                val expression = table.get<Boolean>(DeletedIF::deleted.name) as KNonNullPropExpression<Boolean>
                set(expression, true)
                where(
                    table.getId<ID>().valueIn(ids),
                    expression.eq(false)
                )
            }.execute()
        }
    }

    override suspend fun truncateByIds(ids: Collection<ID>): Int {
        return if (ids.isEmpty()) 0 else {
            return sql.createDelete(entityType) {
                where(
                    table.getId<ID>().valueIn(ids),
                )
            }.execute()
        }
    }

    override suspend fun findByIds(ids: Collection<ID>, fetcher: Fetcher<T>?): Flow<T> =
        findByIds(ids, coroutineContext, fetcher).asFlow()

    protected fun findByIds(
        ids: Collection<ID>,
        context: CoroutineContext,
        fetcher: Fetcher<T>?
    ): List<T> {
        val targetFetcher = fetcher ?: defaultFetcher
        val data = if (targetFetcher != null) {
            sql.entities.findByIds(targetFetcher, ids)
        } else {
            sql.entities.findByIds(entityType, ids)
        }
        return applyPermissions(data, context)
    }

    override suspend fun findOneBySpecification(block: KMutableRootQuery<T>.() -> Unit): T? {
        return findBySpecificationPager(block).asFlow().singleOrNull()
    }

    override suspend fun findAllBySpecification(block: KMutableRootQuery<T>.() -> Unit): Flow<T> =
        findBySpecificationPager(block).asFlow()

    override suspend fun findBySpecification(pageable: Pageable, block: KMutableRootQuery<T>.() -> Unit): Page<T> {
        return findBySpecificationPager(block)(pageable)
    }

    override suspend fun findIdsBySpecification(block: KMutableRootQuery<T>.() -> Unit): Flow<ID> {
        val pager = createIdsPager(coroutineContext, block)
        return flow {
            var nextPageable = Pageable()
            do {
                var nextPageableBuilder: (() -> Pageable)? = null
                val data = pager(nextPageable)
                data.map { (item, builder) ->
                    emit(item)
                    nextPageableBuilder = builder
                }
                nextPageable = nextPageableBuilder?.invoke() ?: nextPageable.copy(cursor = null)
            } while (nextPageable.cursor != null)
        }
    }

    private fun Pageable.actualLimit(): Int = limit?.coerceAtMost(MAX_LIMIT) ?: MAX_LIMIT

    private suspend fun findBySpecificationPager(
        block: KMutableRootQuery<T>.() -> Unit
    ): (Pageable) -> Page<T> = findBySpecificationPager(block, null)

    override suspend fun findBySpecification(
        fetcher: Fetcher<T>,
        pageable: Pageable,
        block: KMutableRootQuery<T>.() -> Unit
    ): Page<T> = findBySpecificationPager(block, fetcher)(pageable)

    private suspend fun findBySpecificationPager(
        block: KMutableRootQuery<T>.() -> Unit,
        fetcher: Fetcher<T>?
    ): (Pageable) -> Page<T> {
        val context = coroutineContext
        return { pageable ->
            val pager = createIdsPager(context, block)
            val actualLimit = pageable.actualLimit()
            val items = mutableListOf<T>()
            var nextPageable: Pageable = pageable

            do {
                val data = pager(nextPageable)
                val index = findByIds(data.map { it.first }, context, fetcher).associateBy { it.getId() }

                val slice = data
                    .mapNotNull { (id, builder) -> index[id]?.let { it to builder } }
                    .take(actualLimit - items.size)

                items.addAll(slice.map { it.first })

                nextPageable = if (data.size < actualLimit) {
                    nextPageable.copy(cursor = null)
                } else {
                    val builder = slice.lastOrNull()?.second ?: data.lastOrNull()?.second
                    builder?.invoke() ?: nextPageable.copy(cursor = null)
                }
            } while (items.size < actualLimit && nextPageable.cursor != null)

            Page(items, nextPageable)
        }
    }

    private fun createIdsPager(
        context: CoroutineContext,
        block: KMutableRootQuery<T>.() -> Unit
    ): (Pageable) -> List<Pair<ID, () -> Pageable>> {
        return { pageable ->
            val cursor = InternalCursor.create(pageable, sortingExpressionsValue)
            val limit = pageable.actualLimit()
            val data = createQuery(block, context, cursor).limit(limit).execute()
            data.map {
                it._1 to {
                    cursor.next(conversionService, it).asPageable()
                        .copy(limit = limit, orderBy = pageable.orderBy)
                }
            }
        }
    }

    private fun createQuery(
        block: KMutableRootQuery<T>.() -> Unit,
        context: CoroutineContext,
        cursor: InternalCursor
    ): KConfigurableRootQuery<T, out Tuple6<ID, out Any, out Any, out Any, out Any, out Any>> {
        return sql.createQuery(entityType) {
            block(this)
            where(*listPredicates(context, table).toTypedArray())

            val orderBy = cursor.getOrderBy(table)
            if (orderBy.size > MAX_ALLOWED_SORTING) {
                error("too many sort orders")
            }
            orderBy(orderBy)

            val predicates = cursor.toPredicates(conversionService)
            where(or(*predicates.toTypedArray()))

            val projection = orderBy.map { it.expression }.filterIsInstance<Selection<*>>()
            val (a, b, c, d, e) = projection + List(size = MAX_ALLOWED_SORTING - projection.size) { constant(1) }
            select(table.getId<ID>() as Selection<ID>, a, b, c, d, e)
        }
    }

    override suspend fun <S : T> update(id: ID, block: S.() -> Unit): T {
        return updateIf<S>(id) {
            block.invoke(this)
            true
        }.first
    }

    private fun ValidationResult<T>?.check() {
        val result = this
        if (result != null && !result.isValid) {
            throw ValidationException(result.errors.map { ValidationError(it.dataPath, it.message) })
        }
    }

    private suspend fun persist(entity: T): T {
        if (applyPermissions(entity, coroutineContext, Permission.MANAGE) == null) {
            missingPermission(Permission.MANAGE)
        }

        validator?.let {
            it.onCreate().validate(entity).check()
            it.onUpdate().validate(entity).check()
        }

        return sql.entities.save(entity).get()
    }

    override suspend fun <S : T> update(entity: S): T {
        if (applyPermissions(entity, coroutineContext, Permission.MANAGE) == null) {
            missingPermission(Permission.MANAGE)
        }

        validator?.let {
            it.onUpdate().validate(entity).check()
        }

        val result = sql.entities.save(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }

        return if (!result.isModified) error("entity wasn't modified") else result.get()
    }

    private suspend fun <S : T> KSimpleSaveResult<S>.get(): T {
        val modifiedEntity = defaultFetcher?.let { getById(modifiedEntity.getId()) } ?: modifiedEntity
        return applyPermissions(modifiedEntity, coroutineContext) ?: missingPermission(Permission.VIEW)
    }

    private fun missingPermission(permission: Permission): Nothing {
        error("subject has no '$permission' permission")
    }

    override suspend fun <S : T> updateIf(id: ID, block: S.() -> Boolean): Pair<T, Boolean> {
        val value = sql.entities.findById(entityType, id) ?: throw EntityNotFoundException(id, entityType)

        val mutate = AtomicBoolean()
        val draft = produce<S>(coroutineContext, value) { mutate.set(block(it)) }
        return if (mutate.get()) update(draft) to true else value to false
    }

    override suspend fun <S : T> saveIf(id: ID?, block: (Boolean, S) -> Boolean): Pair<T, Boolean> {
        val value = id?.let { findById(id) }
        val mutate = AtomicBoolean()
        val draft = produce<S>(coroutineContext, value) { mutate.set(block(value == null, it)) }
        return if (mutate.get()) {
            val function: suspend (T) -> T = if (value == null) ::persist else ::update
            function(draft) to true
        } else {
            requireNotNull(value) to false
        }
    }

    override suspend fun <S : T> save(id: ID?, block: (Boolean, S) -> Unit): T {
        return saveIf<S>(id) { isNew, value ->
            block(isNew, value)
            true
        }.first
    }

    override suspend fun <S : T> save(block: S.() -> Unit): T {
        val draft = produce(coroutineContext, null, block)
        val id = if (!(draft as ImmutableSpi).__isLoaded(idProperty.name())) null else {
            draft.__get(idProperty.name()) as ID?
        }
        val value = id?.let { findById(id) }
        val function: suspend (T) -> T = if (value == null) ::persist else ::update
        return function(draft)
    }
}
