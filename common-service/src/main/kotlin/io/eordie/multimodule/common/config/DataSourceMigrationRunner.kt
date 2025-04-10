package io.eordie.multimodule.common.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.context.ApplicationContext
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.core.naming.NameResolver
import io.micronaut.flyway.FlywayConfigurationProperties
import io.micronaut.jdbc.DataSourceResolver
import io.micronaut.kotlin.context.getBean
import jakarta.inject.Singleton
import java.util.*
import javax.sql.DataSource

@Singleton
class DataSourceMigrationRunner(
    private val applicationContext: ApplicationContext,
    private val dataSourceResolver: Optional<DataSourceResolver>
) : BeanCreatedEventListener<DataSource> {

    private val logger = KotlinLogging.logger { }

    override fun onCreated(event: BeanCreatedEvent<DataSource>): DataSource {
        val dataSource = event.bean
        val isDefault = event.beanIdentifier.name == "default"
        if (event.beanDefinition is NameResolver) {
            (event.beanDefinition as NameResolver)
                .resolveName()
                .map { applicationContext.getBean<FlywayConfigurationProperties>(it) }
                .ifPresent { flywayConfig: FlywayConfigurationProperties ->
                    val unwrappedDataSource = dataSourceResolver.map { it.resolve(dataSource) }.orElse(dataSource)
                    run(flywayConfig, unwrappedDataSource)

                    if (isDefault) {
                        runCommon(unwrappedDataSource)
                    }
                }
        }

        return dataSource
    }

    private fun runCommon(dataSource: DataSource) {
        val properties = FlywayConfigurationProperties("default").apply {
            fluentConfiguration.locations("classpath:/db/common/migration")
            fluentConfiguration.baselineOnMigrate(true)
        }
        run(properties, dataSource)
    }

    private fun run(config: FlywayConfigurationProperties, dataSource: DataSource) {
        val fluentConfiguration = config.fluentConfiguration.apply {
            dataSource(dataSource)
            configuration(config.properties)
        }

        config.fluentConfiguration.locations.forEach { location ->
            val module = location.path.substringAfter("db/").substringBefore("/migration")

            logger.info { "Running migrations for module $module" }
            fluentConfiguration.locations(location)
                .table("flyway_${module}_schema_history")
                .apply {
                    if (fluentConfiguration.isBaselineOnMigrate) {
                        baselineVersion("0")
                    }
                }
                .load()
                .migrate()
        }
    }
}
