package io.eordie.multimodule.graphql.gateway.graphql

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.common.rsocket.context.SelectionSetContextElement
import kotlin.coroutines.CoroutineContext

fun DataFetchingEnvironment.newCoroutineContext(): CoroutineContext {
    val ctx = this.graphQlContext
    return ctx.newCoroutineContext() + SelectionSetContextElement(SelectionSetExtractor.from(this))
}
