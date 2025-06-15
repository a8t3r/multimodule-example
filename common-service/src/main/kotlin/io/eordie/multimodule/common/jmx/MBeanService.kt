package io.eordie.multimodule.common.jmx

import com.github.jasync.sql.db.pool.ConnectionPool
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import io.eordie.multimodule.contracts.utils.JsonModule
import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration
import jakarta.inject.Provider
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.net.URI
import javax.annotation.PostConstruct

@Singleton
class MBeanService(
    private val client: KSqlClient,
    properties: DatasourceConfiguration,
    private val mbeanFactory: Provider<CustomEndpointMBeanFactory>
) : AutoCloseable {

    companion object {
        private const val CHANNEL_NAME = "mbeans"
    }

    @Serializable
    data class Notification(
        val name: String,
        val property: String,
        val value: String
    )

    private val pool: ConnectionPool<PostgreSQLConnection> = PostgreSQLConnectionBuilder.createConnectionPool {
        val uri = URI(properties.url.substring(5))
        this.host = uri.host
        this.port = uri.port
        this.database = uri.path.substringAfter("/")
        this.username = properties.username
        this.password = properties.password
    }

    private lateinit var connection: PostgreSQLConnection

    @PostConstruct
    fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            connection = pool.take().asDeferred().await()
            connection.sendPreparedStatement("LISTEN $CHANNEL_NAME")
            connection.registerNotifyListener {
                handleNotification(it.payload)
            }
        }
    }

    private fun handleNotification(payload: String) {
        val notification = JsonModule.getInstance().decodeFromString<Notification>(payload)
        mbeanFactory.get().updateLocalState(notification.name, notification.property, notification.value)
    }

    override fun close() {
        connection.disconnect()
        pool.disconnect()
    }

    fun actualize(key: MBeanKey, actual: Boolean) = client.createUpdate(MBeanModel::class) {
        set(table.actual, actual)
        where(table.id eq key)
    }

    fun save(bean: MBeanModel) {
        client.save(bean, SaveMode.UPSERT)
    }

    fun findByName(beanName: String): List<MBeanModel> = client.executeQuery(MBeanModel::class) {
        where(table.id.name eq beanName)
        select(table)
    }
}
