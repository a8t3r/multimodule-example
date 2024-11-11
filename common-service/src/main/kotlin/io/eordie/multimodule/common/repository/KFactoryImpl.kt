package io.eordie.multimodule.common.repository

import io.eordie.multimodule.common.filter.FiltersRegistry
import io.eordie.multimodule.common.repository.entity.CreatedByIF
import io.eordie.multimodule.common.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.common.repository.entity.UpdatedByIF
import io.eordie.multimodule.common.repository.ext.and
import io.eordie.multimodule.common.repository.ext.name
import io.eordie.multimodule.common.repository.ext.or
import io.eordie.multimodule.common.rsocket.client.route.ValidationCheck.toErrors
import io.eordie.multimodule.common.security.context.Microservices
import io.eordie.multimodule.common.security.context.getAuthenticationContext
import io.eordie.multimodule.common.utils.asFlow
import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.common.validation.MissingPermission
import io.eordie.multimodule.common.validation.error
import io.eordie.multimodule.contracts.basic.BasePermission
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.basic.exception.EntityNotFoundException
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.micronaut.context.ApplicationContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.transaction.exceptions.UnexpectedRollbackException
import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.runBlocking
import org.babyfish.jimmer.DraftConsumer
import org.babyfish.jimmer.kt.toImmutableProp
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.meta.TargetLevel
import org.babyfish.jimmer.runtime.DraftSpi
import org.babyfish.jimmer.runtime.ImmutableSpi
import org.babyfish.jimmer.runtime.Internal
import org.babyfish.jimmer.sql.ast.Selection
import org.babyfish.jimmer.sql.ast.mutation.DeleteMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.ast.tuple.Tuple6
import org.babyfish.jimmer.sql.event.EntityListener
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KEntities
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.constant
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.mutation.KSimpleSaveResult
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.valiktor.ConstraintViolationException
import java.sql.Connection
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Suppress("TooManyFunctions")
open class KFactoryImpl<T : Any, S : T, ID : Comparable<ID>>(
    val entityType: KClass<T>
) : KFactory<T, S, ID> {

    @Inject
    private lateinit var context: ApplicationContext

    lateinit var sql: KSqlClient
    private lateinit var noCacheSql: KSqlClient

    @Inject
    private lateinit var conversionService: ConversionService

    @Inject
    private lateinit var registryProvider: Provider<FiltersRegistry>

    @Inject
    private lateinit var microservices: Provider<Microservices>

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
        private val UPDATED_BY_PROPERTY = UpdatedByIF::updatedBy.toImmutableProp().id
        private val PERMISSIONS_PROPERTY = PermissionAwareIF::permissions.toImmutableProp().id
    }

    open val requireEmployeeAcl = true
    open val prefetchByKeysEnabled = true
    open val datasourceName = "default"
    open val entityListener: EntityListener<T>? = null
    open fun sortingExpressions(table: KNonNullTable<T>): List<KPropExpression<out Comparable<*>>> {
        return immutableType.selectableScalarProps.map { table.get(it.value) }
    }

    private val isCreatedByAware = entityType.isSubclassOf(CreatedByIF::class)
    private val isUpdatedByAware = entityType.isSubclassOf(UpdatedByIF::class)
    private val isPermissionAware = entityType.isSubclassOf(PermissionAwareIF::class)
    private val isOrganizationOwnerAware = entityType.isSubclassOf(OrganizationOwnerIF::class)

    internal lateinit var idPropertyName: String
    private lateinit var sortingExpressionsValue: Map<String, KPropExpression<out Comparable<*>>>

    internal class ConnectionWrapper(delegate: Connection, val context: CoroutineContext) : Connection by delegate

    private class ConnectionContextElement(
        val connection: Connection
    ) : CoroutineContext.Element {
        companion object Key : CoroutineContext.Key<ConnectionContextElement>

        override val key: CoroutineContext.Key<*> = Key
    }

    private suspend fun currentConnection() = coroutineContext[ConnectionContextElement.Key]?.connection

    suspend fun <R : Any> transaction(block: suspend CoroutineScope.(Connection) -> R): R {
        val context = coroutineContext
        val previous = currentConnection()
        return if (previous != null) coroutineScope { block(previous) } else {
            noCacheSql.javaClient.connectionManager.execute { connection ->
                connection.autoCommit = false
                runBlocking(context + ConnectionContextElement(connection)) {
                    val result = kotlin.runCatching { block(connection) }
                    if (result.isSuccess) connection.commit() else connection.rollback()
                    result.getOrElse { throw UnexpectedRollbackException(it.message, it) }
                }
            }
        }
    }

    context(ResourceAcl)
    fun KNonNullTable<*>.accept(filter: Any?): KNonNullExpression<Boolean>? {
        val resourceAcl: ResourceAcl = this@ResourceAcl
        return filter?.let { registry.toPredicates(resourceAcl, it, this) }
    }

    @PostConstruct
    fun init() {
        sql = context.getBean(KSqlClient::class.java, Qualifiers.byName(datasourceName)).apply {
            val entityListener = entityListener
            if (entityListener != null) {
                getTriggers(true).addEntityListener(entityType, entityListener)
            }
        }
        noCacheSql = sql.caches { disableAll() }
        sql.createQuery(entityType) {
            idPropertyName = table.getId<Any>().name()
            sortingExpressionsValue = sortingExpressions(table).associateBy { it.name() }
            select(constant(1))
        }
    }

    open fun ResourceAcl.listPredicates(table: KNonNullTable<T>): List<KNonNullExpression<Boolean>> =
        emptyList()

    private fun T.getId(): ID = (this as ImmutableSpi).__get(idPropertyName) as ID

    open suspend fun calculatePermissions(acl: ResourceAcl, values: List<T>): Map<T, Set<Permission>> =
        if (!isPermissionAware) emptyMap() else {
            values.associateWith { calculatePermissions(acl, it) }
        }

    open suspend fun calculatePermissions(acl: ResourceAcl, value: T): Set<Permission> =
        if (!isPermissionAware) emptySet() else {
            throw UnsupportedOperationException("should be implemented by factory")
        }

    private suspend fun <D> produce(base: T? = null, block: (D) -> Unit): D where D : T {
        val context = coroutineContext
        val consumer = DraftConsumer<D> {
            if (base == null) {
                val auth = context.getAuthenticationContext()
                if (isCreatedByAware && !(it as DraftSpi).__isLoaded(CREATED_BY_PROPERTY)) {
                    it.__set(CREATED_BY_PROPERTY, auth.userId)
                }
                if (isOrganizationOwnerAware && !(it as DraftSpi).__isLoaded("organizationId")) {
                    it.__set("organizationId", auth.currentOrganizationId)
                }
            }

            if (isUpdatedByAware && !(it as DraftSpi).__isLoaded(UPDATED_BY_PROPERTY)) {
                it.__set(UPDATED_BY_PROPERTY, context.getAuthenticationContext().userId)
            }

            block(it)
        }
        return Internal.produce(immutableType, base, consumer) as D
    }

    suspend fun resourceAcl(): ResourceAcl = microservices.get().buildAcl(coroutineContext, requireEmployeeAcl)

    private suspend fun applyPermissions(
        values: List<T>,
        permission: BasePermission
    ): List<T> {
        return if (values.isEmpty() || !isPermissionAware) values else {
            val acl = resourceAcl()
            val index = calculatePermissions(acl, values)
            values.mapNotNull { value ->
                val permissions = index[value].orEmpty()
                if (permissions.none { it.name == permission.name }) null else {
                    val block: (T) -> Unit = {
                        (it as DraftSpi).__set(PERMISSIONS_PROPERTY, permissions.toList())
                    }
                    produce<T>(value, block)
                }
            }
        }
    }

    suspend fun checkPermission(value: T, permission: BasePermission): T {
        return applyPermissions(listOf(value), permission).firstOrNull() ?: kotlin.run {
            MissingPermission(permission).error()
        }
    }

    override suspend fun existsById(id: ID): Boolean {
        return findById(id) != null
    }

    private suspend fun entities(): KEntities {
        val currentConnection = currentConnection()
        return if (currentConnection == null) sql.entities else {
            noCacheSql.entities.forConnection(currentConnection)
        }
    }

    override suspend fun findById(id: ID, fetcher: Fetcher<T>?): T? {
        val value = if (fetcher != null) {
            entities().findById(fetcher, id)
        } else {
            entities().findById(entityType, id)
        }

        return value?.let { checkPermission(it, BasePermission.VIEW) }
    }

    override suspend fun getById(id: ID, fetcher: Fetcher<T>?): T {
        return findById(id, fetcher) ?: throw EntityNotFoundException(id, entityType)
    }

    override suspend fun deleteById(id: ID): Boolean {
        return deleteByIds(listOf(id)) > 0
    }

    override suspend fun deleteByIds(ids: Collection<ID>): Int {
        return if (ids.isEmpty()) 0 else {
            internalFindByIds(ids, null).forEach {
                checkPermission(it, BasePermission.PURGE)
            }

            wrapped {
                sql.entities.deleteAll(entityType, ids, it) {
                    setMode(DeleteMode.AUTO)
                }.totalAffectedRowCount
            }
        }
    }

    override suspend fun truncateByIds(ids: Collection<ID>): Int {
        return if (ids.isEmpty()) 0 else {
            wrapped {
                sql.createDelete(entityType) {
                    where(
                        table.getId<ID>().valueIn(ids)
                    )
                }.execute(it)
            }
        }
    }

    override suspend fun findByIds(ids: Collection<ID>, fetcher: Fetcher<T>?): Flow<T> =
        internalFindByIds(ids, fetcher).asFlow()

    protected suspend fun internalFindByIds(
        ids: Collection<ID>,
        fetcher: Fetcher<T>?
    ): List<T> {
        val data = if (fetcher != null) {
            entities().findByIds(fetcher, ids)
        } else {
            entities().findByIds(entityType, ids)
        }
        return applyPermissions(data, BasePermission.VIEW)
    }

    override suspend fun findOneBySpecification(fetcher: Fetcher<T>?, block: KMutableRootQuery<T>.() -> Unit): T? {
        return findBySpecificationPager(block, fetcher).asFlow().singleOrNull()
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
            var nextPageable = pageable.copy(supportedOrders = sortingExpressionsValue.keys)

            do {
                // raw ids without permission checks
                val data = pager(nextPageable)

                // retrieve elements by ids and check permissions
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
            val cursor = InternalCursor.create(pageable, idPropertyName, sortingExpressionsValue)
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
            where(*acl.listPredicates(table).toTypedArray())

            val orderBy = cursor.getOrderBy(table)
            if (orderBy.size > MAX_ALLOWED_SORTING) {
                error("too many sort orders")
            }
            orderBy(orderBy)
            where(cursor.toPredicates(conversionService).or())

            val projection = orderBy.map { it.expression }.filterIsInstance<Selection<*>>()
            val (a, b, c, d, e) = projection + List(size = MAX_ALLOWED_SORTING - projection.size) { constant(1) }
            select(table.getId<ID>() as Selection<ID>, a, b, c, d, e)
        }
    }

    override suspend fun update(id: ID, block: S.() -> Unit): T {
        return updateIf(id) {
            block.invoke(this)
            true
        }.first
    }

    private suspend fun persist(entity: S): T {
        checkPermission(entity, BasePermission.MANAGE)

        validator?.let {
            try {
                it.onCreate(entity)
                it.onUpdate(entity)
            } catch (ignored: ConstraintViolationException) {
                throw ValidationException(ignored.toErrors(coroutineContext))
            }
        }

        return wrapped {
            sql.entities.save(entity, it) {
                setMode(SaveMode.INSERT_ONLY)
            }
        }.get()
    }

    override suspend fun update(entity: S): T {
        checkPermission(entity, BasePermission.MANAGE)

        validator?.let {
            try {
                it.onUpdate(entity)
            } catch (ignored: ConstraintViolationException) {
                throw ValidationException(ignored.toErrors(coroutineContext))
            }
        }

        val result = wrapped {
            sql.entities.save(entity, it) {
                setMode(SaveMode.UPDATE_ONLY)
            }
        }
        return if (!result.isModified) error("entity wasn't modified") else result.get()
    }

    protected suspend fun <R : Any> wrapped(block: (Connection) -> R): R {
        val context = coroutineContext
        val previousConnection = currentConnection()
        return if (previousConnection != null) {
            block.invoke(ConnectionWrapper(previousConnection, context))
        } else {
            sql.javaClient.connectionManager.execute { connection ->
                block.invoke(ConnectionWrapper(connection, context))
            }
        }
    }

    private suspend fun KSimpleSaveResult<S>.get(): T {
        return checkPermission(modifiedEntity, BasePermission.VIEW)
    }

    override suspend fun updateIf(id: ID, block: S.() -> Boolean): Pair<T, Boolean> {
        val value = entities().findById(entityType, id) ?: throw EntityNotFoundException(id, entityType)

        val mutate = AtomicBoolean()
        val draft = produce<S>(value) { mutate.set(block(it)) }
        return if (mutate.get()) update(draft) to true else value to false
    }

    private suspend fun <S : ImmutableSpi> loadByKeys(prefetch: S): T? {
        return if (!prefetchByKeysEnabled) null else {
            loadByKeys(prefetch, null)
        }
    }

    private suspend fun loadByKeys(block: (EntityState, S) -> Boolean, fetcher: Fetcher<T>?): T? {
        return if (!prefetchByKeysEnabled) null else {
            val prefetch = produce<S>(null) { block.invoke(EntityState.PREFETCH, it) } as ImmutableSpi
            loadByKeys(prefetch, fetcher)
        }
    }

    private suspend fun <S : ImmutableSpi> loadByKeys(prefetch: S, fetcher: Fetcher<T>?): T? {
        val matchedKeyProps = immutableType.keyMatcher.matchedKeyProps(prefetch)
        return if (matchedKeyProps.isEmpty() || matchedKeyProps.any { !prefetch.__isLoaded(it.id) }) null else {
            sql.createQuery(entityType) {
                val predicate = matchedKeyProps.map { keyProp ->
                    val propertyValue = prefetch.__get(keyProp.id)
                    if (keyProp.isReference(TargetLevel.ENTITY)) {
                        val targetIdExpression = table.getAssociatedId<Any>(keyProp)
                        val target = propertyValue as ImmutableSpi?
                        if (target == null) {
                            targetIdExpression.isNull()
                        } else {
                            targetIdExpression.eq(target.__get(keyProp.targetType.idProp.id))
                        }
                    } else {
                        if (propertyValue == null) {
                            table.get<Any>(keyProp).isNull()
                        } else {
                            table.get<Any>(keyProp).eq(propertyValue)
                        }
                    }
                }.and()

                where(predicate)
                val selection = fetcher?.let { table.fetch(it) } ?: table
                select(selection)
            }.fetchOneOrNull(currentConnection())
        }
    }

    override suspend fun saveIf(
        id: ID?,
        fetcher: Fetcher<T>?,
        block: (EntityState, S) -> Boolean
    ): Pair<T, Boolean> {
        val value = if (id != null) findById(id, fetcher) else loadByKeys(block, fetcher)
        val mutate = AtomicBoolean()
        val draft = produce<S>(value) {
            val state = if (value == null) EntityState.NEW else EntityState.EXISTING
            mutate.set(block(state, it))
        }

        return if (mutate.get()) {
            val function: suspend (S) -> T = if (value == null) ::persist else ::update
            function(draft) to true
        } else {
            requireNotNull(value) to false
        }
    }

    override suspend fun save(id: ID?, fetcher: Fetcher<T>?, block: (EntityState, S) -> Unit): T {
        return saveIf(id, fetcher) { isNew, value ->
            block(isNew, value)
            true
        }.first
    }

    override suspend fun save(block: S.() -> Unit): T {
        val draft = produce(null, block)
        val id = if (!(draft as ImmutableSpi).__isLoaded(idPropertyName)) null else {
            draft.__get(idPropertyName) as ID?
        }
        val value = if (id != null) findById(id) else loadByKeys(draft)
        val function: suspend (S) -> T = if (value == null) ::persist else ::update
        return function(draft)
    }
}
