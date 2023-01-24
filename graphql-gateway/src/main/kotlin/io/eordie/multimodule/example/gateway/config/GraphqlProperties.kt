package io.eordie.multimodule.example.gateway.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("graphql.instrumentation")
class GraphqlProperties {
    var maxDepth: Int = 0
    var maxComplexity: Int = 0
}
