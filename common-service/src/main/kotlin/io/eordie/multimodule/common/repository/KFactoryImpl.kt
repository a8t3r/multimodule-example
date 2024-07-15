package io.eordie.multimodule.common.repository

import io.eordie.multimodule.common.filter.FiltersRegistry
import io.eordie.multimodule.common.repository.entity.CreatedByIF
import io.eordie.multimodule.common.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.common.repository.ext.name
import io.eordie.multimodule.common.rsocket.context.Microservices
import io.eordie.multimodule.common.rsocket.context.getAuthentication
import io.eordie.multimodule.common.rsocket.context.getAuthenticationContext
import io.eordie.multimodule.common.utils.asFlow
import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.basic.exception.EntityNotFoundException
import io.eordie.multimodule.contracts.basic.exception.ValidationError
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.micronaut.context.ApplicationContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.kotlin.context.getBean
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
import org.babyfish.jimmer.sql.ast.mutation.DeleteMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.ast.tuple.Tuple6
import org.babyfish.jimmer.sql.event.EntityListener
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.constant
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.mutation.KMutableUpdate
import org.babyfish.jimmer.sql.kt.ast.mutation.KSimpleSaveResult
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.mapToMessage
import java.sql.Connection
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.coroutineContext
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Suppress("TooManyFunctions")
open class KFactoryImpl<T : Any, ID : Comparable<ID>>(
    val entityType: KClass<T>
) : KFactory<T, ID> {

    @Inject
    private lateinit var context: ApplicationContext

    lateinit var sql: KSqlClient

    @Inject
    private lateinit var conversionService: ConversionService

    @Inject
    private lateinit var registryProvider: Provider<FiltersRegistry>

    private val immutableType = ImmutableType.get(entityType.java)

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
        private val PERMISSIONS_PROPERTY = PermissionAwareIF::permissions.toImmutableProp().id
    }

    open val requireEmployeeAcl = true
    open val datasourceName = "default"
    open fun entityListener(): EntityListener<T>? = null
    open fun sortingExpressions(table: KNonNullTable<T>): List<KPropExpression<out Comparable<*>>> = listOf(idProperty)

    private val isCreatedByAware = entityType.isSubclassOf(CreatedByIF::class)
    private val isPermissionAware = entityType.isSubclassOf(PermissionAwareIF::class)
    private val isOrganizationOwnerAware = entityType.isSubclassOf(OrganizationOwnerIF::class)

    internal lateinit var idProperty: KPropExpression<ID>
    private lateinit var sortingExpressionsValue: Map<String, KPropExpression<out Comparable<*>>>

    @PostConstruct
    fun init() {
        sql = context.getBean(KSqlClient::class.java, Qualifiers.byName(datasourceName)).apply {
            val entityListener = entityListener()
            if (entityListener != null) {
                getTriggers(true).addEntityListener(entityType, entityListener)
            }
        }
        sql.createQuery(entityType) {
            idProperty = table.getId()
            sortingExpressionsValue = sortingExpressions(table).associateBy { it.name() }
            select(constant(1))
        }
    }

    open fun listPredicates(acl: ResourceAcl, table: KNonNullTable<T>): List<KNonNullExpression<Boolean>> =
        emptyList()

    private fun T.getId(): ID = (this as ImmutableSpi).__get(idProperty.name()) as ID

    open suspend fun calculatePermissions(acl: ResourceAcl, values: List<T>): Map<T, Set<Permission>> {
        return if (isPermissionAware) {
            values.associateWith { calculatePermissions(acl, it) }
        } else {
            emptyMap()
        }
    }

    open suspend fun calculatePermissions(acl: ResourceAcl, value: T): Set<Permission> {
        return if (isPermissionAware) {
            throw UnsupportedOperationException("should be implemented by factory")
        } else {
            emptySet()
        }
    }

    private suspend fun <D> produce(base: T? = null, block: (D) -> Unit): D where D : T {
        val context = coroutineContext
        val consumer = DraftConsumer<D> {
            block(it)
            if (base == null) {
                val auth = context.getAuthenticationContext()
                if (isCreatedByAware && !(it as DraftSpi).__isLoaded(CREATED_BY_PROPERTY)) {
                    it.__set(CREATED_BY_PROPERTY, auth.userId)
                }
                if (isOrganizationOwnerAware && !(it as DraftSpi).__isLoaded("organizationId")) {
                    it.__set("organizationId", auth.currentOrganizationId)
                }
            }
        }
        return Internal.produce(immutableType, base, consumer) as D
    }

    suspend fun resourceAcl(): ResourceAcl {
        val employeeAcl = if (!requireEmployeeAcl) emptyList() else {
            val microservices = context.getBean<Microservices>()
            microservices.loadAclElement().resource
        }
        return ResourceAcl(getAuthentication(), employeeAcl)
    }

    private suspend fun applyPermissions(
        values: List<T>,
        permission: Permission = Permission.VIEW
    ): List<T> {
        return if (values.isEmpty() || !isPermissionAware) values else {
            val acl = resourceAcl()
            val index = calculatePermissions(acl, values)
            values.mapNotNull { value ->
                val permissions = index[value].orEmpty()
                if (!permissions.contains(permission)) null else {
                    val block: (T) -> Unit = {
                        (it as DraftSpi).__set(PERMISSIONS_PROPERTY, permissions.toList())
                    }
                    produce<T>(value, block)
                }
            }
        }
    }

    private suspend fun applyPermissions(
        value: T,
        permission: Permission = Permission.VIEW
    ): T? = applyPermissions(listOf(value), permission).firstOrNull()

    override suspend fun existsById(id: ID): Boolean {
        return findById(id) != null
    }

    override suspend fun findById(id: ID, fetcher: Fetcher<T>?): T? {
        val value = if (fetcher != null) {
            sql.entities.findById(fetcher, id)
        } else {
            sql.entities.findById(entityType, id)
        }

        return value?.let { applyPermissions(it) }
    }

    override suspend fun getById(id: ID, fetcher: Fetcher<T>?): T {
        return findById(id, fetcher) ?: throw EntityNotFoundException(id, entityType)
    }

    override suspend fun deleteById(id: ID): Boolean {
        return deleteByIds(listOf(id)) > 0
    }

    override suspend fun deleteByIds(ids: Collection<ID>): Int {
        return if (ids.isEmpty()) 0 else {
            wrapped {
                sql.entities.deleteAll(entityType, ids, it) {
                    setMode(DeleteMode.AUTO)
                }
            }.totalAffectedRowCount
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
        internalFindByIds(ids, fetcher).asFlow()

    protected suspend fun internalFindByIds(
        ids: Collection<ID>,
        fetcher: Fetcher<T>?
    ): List<T> {
        val data = if (fetcher != null) {
            sql.entities.findByIds(fetcher, ids)
        } else {
            sql.entities.findByIds(entityType, ids)
        }
        return applyPermissions(data)
    }

    override suspend fun findOneBySpecification(block: KMutableRootQuery<T>.() -> Unit): T? {
        return findBySpecificationPager(block).asFlow().singleOrNull()
    }

    override suspend fun findAllBySpecification(fetcher: Fetcher<T>?, block: KMutableRootQuery<T>.() -> Unit): Flow<T> =
        findBySpecificationPager(block, fetcher).asFlow()

    override suspend fun findBySpecification(pageable: Pageable, block: KMutableRootQuery<T>.() -> Unit): Page<T> {
        return findBySpecificationPager(block)(pageable)
    }

    override suspend fun findIdsBySpecification(block: KMutableRootQuery<T>.() -> Unit): Flow<ID> {
        val pager = createIdsPager(resourceAcl(), block)
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
    ): suspend (Pageable) -> Page<T> = findBySpecificationPager(block, null)

    override suspend fun findBySpecification(
        fetcher: Fetcher<T>,
        pageable: Pageable,
        block: KMutableRootQuery<T>.() -> Unit
    ): Page<T> = findBySpecificationPager(block, fetcher)(pageable)

    private suspend fun findBySpecificationPager(
        block: KMutableRootQuery<T>.() -> Unit,
        fetcher: Fetcher<T>?
    ): suspend (Pageable) -> Page<T> {
        val acl = resourceAcl()
        return { pageable ->
            val pager = createIdsPager(acl, block)
            val actualLimit = pageable.actualLimit()
            val items = mutableListOf<T>()
            var nextPageable: Pageable = pageable

            do {
                val data = pager(nextPageable)
                val index = internalFindByIds(data.map { it.first }, fetcher).associateBy { it.getId() }

                // filter accessible elements and take missing amount
                val slice = data
                    .mapNotNull { (id, builder) -> index[id]?.let { it to builder } }
                    .take(actualLimit - items.size)

                items.addAll(slice.map { it.first })

                nextPageable = if (data.size < actualLimit) {
                    nextPageable.copy(cursor = null)
                } else {
                    // the empty slice means that current data page has no accessible elements
                    val builder = (slice.lastOrNull() ?: data.lastOrNull())?.second
                    builder?.invoke() ?: nextPageable.copy(cursor = null)
                }
            } while (items.size < actualLimit && nextPageable.cursor != null)

            Page(items, nextPageable)
        }
    }

    private fun createIdsPager(
        acl: ResourceAcl,
        block: KMutableRootQuery<T>.() -> Unit
    ): (Pageable) -> List<Pair<ID, () -> Pageable>> {
        return { pageable ->
            val cursor = InternalCursor.create(pageable, idProperty.name(), sortingExpressionsValue)
            val limit = pageable.actualLimit()
            val data = createQuery(block, acl, cursor).limit(limit).execute()
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
        acl: ResourceAcl,
        cursor: InternalCursor
    ): KConfigurableRootQuery<T, out Tuple6<ID, out Any, out Any, out Any, out Any, out Any>> {
        return sql.createQuery(entityType) {
            block(this)
            where(*listPredicates(acl, table).toTypedArray())

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

    private fun ConstraintViolationException.check(): Nothing {
        val result = this
        throw ValidationException(
            result.constraintViolations
                .mapToMessage("custom_messages")
                .map { ValidationError(it.property, it.message) }
        )
    }

    private suspend fun persist(entity: T): T {
        if (applyPermissions(entity, Permission.MANAGE) == null) {
            missingPermission(Permission.MANAGE)
        }

        validator?.let {
            try {
                it.onCreate(entity)
                it.onUpdate(entity)
            } catch (e: ConstraintViolationException) {
                e.check()
            }
        }

        return wrapped {
            sql.entities.save(entity, it)
        }.get()
    }

    override suspend fun <S : T> update(entity: S): T {
        if (applyPermissions(entity, Permission.MANAGE) == null) {
            missingPermission(Permission.MANAGE)
        }

        validator?.let {
            try {
                it.onUpdate(entity)
            } catch (e: ConstraintViolationException) {
                e.check()
            }
        }

        val result = wrapped {
            sql.entities.save(entity, it) {
                setMode(SaveMode.UPDATE_ONLY)
            }
        }
        return if (!result.isModified) error("entity wasn't modified") else result.get()
    }

    private suspend fun <R : Any> wrapped(block: (Connection) -> R): R {
        val context = coroutineContext
        return sql.javaClient.connectionManager.execute { connection ->
            block.invoke(io.eordie.multimodule.common.repository.ConnectionWrapper(connection, context))
        }
    }

    private suspend fun <S : T> KSimpleSaveResult<S>.get(): T {
        return applyPermissions(modifiedEntity) ?: missingPermission(Permission.VIEW)
    }

    protected suspend fun rawUpdate(block: KMutableUpdate<T>.() -> Unit): Boolean {
        return wrapped {
            sql.createUpdate(entityType, block).execute(it)
        } > 0
    }

    private fun missingPermission(permission: Permission): Nothing {
        error("subject has no '$permission' permission")
    }

    override suspend fun <S : T> updateIf(id: ID, block: S.() -> Boolean): Pair<T, Boolean> {
        val value = sql.entities.findById(entityType, id) ?: throw EntityNotFoundException(id, entityType)

        val mutate = AtomicBoolean()
        val draft = produce<S>(value) { mutate.set(block(it)) }
        return if (mutate.get()) update(draft) to true else value to false
    }

    override suspend fun <S : T> saveIf(
        id: ID?,
        fetcher: Fetcher<T>?,
        block: (Boolean, S) -> Boolean
    ): Pair<T, Boolean> {
        val value = id?.let { findById(id, fetcher) }
        val mutate = AtomicBoolean()
        val draft = produce<S>(value) {
            mutate.set(block(value == null, it))
        }

        return if (mutate.get()) {
            val function: suspend (T) -> T = if (value == null) ::persist else ::update
            function(draft) to true
        } else {
            requireNotNull(value) to false
        }
    }

    override suspend fun <S : T> save(id: ID?, fetcher: Fetcher<T>?, block: (Boolean, S) -> Unit): T {
        return saveIf<S>(id, fetcher) { isNew, value ->
            block(isNew, value)
            true
        }.first
    }

    override suspend fun <S : T> save(block: S.() -> Unit): T {
        val draft = produce(null, block)
        val id = if (!(draft as ImmutableSpi).__isLoaded(idProperty.name())) null else {
            draft.__get(idProperty.name()) as ID?
        }
        val value = id?.let { findById(id) }
        val function: suspend (T) -> T = if (value == null) ::persist else ::update
        return function(draft)
    }
}