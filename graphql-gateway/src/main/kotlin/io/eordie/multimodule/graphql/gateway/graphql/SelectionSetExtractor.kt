package io.eordie.multimodule.graphql.gateway.graphql

import graphql.execution.conditional.ConditionalNodes
import graphql.language.Field
import graphql.language.FragmentDefinition
import graphql.language.FragmentSpread
import graphql.language.NamedNode
import graphql.language.SelectionSet
import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.basic.paging.SelectionSet as InnerSelectionSet

object SelectionSetExtractor {

    private val conditional = ConditionalNodes()

    fun from(environment: DataFetchingEnvironment): InnerSelectionSet {
        return InnerSelectionSet(getFieldNames(environment))
    }

    private fun getFieldNames(environment: DataFetchingEnvironment): List<String> {
        val selectionSet = environment.executionStepInfo?.field?.singleField?.selectionSet ?: return emptyList()
        return selectionSet.fieldNames(
            environment,
            environment.fragmentsByName.orEmpty(),
            environment.variables.orEmpty()
        )
    }

    private fun SelectionSet.fieldNames(
        environment: DataFetchingEnvironment,
        definitions: Map<String, FragmentDefinition>,
        variables: Map<String, Any>,
        parent: String = ""
    ): List<String> {
        val selectionSet = this
        return selectionSet.selections
            .mapNotNull {
                when {
                    it is Field -> it
                    it is FragmentSpread && definitions.containsKey(it.name) -> requireNotNull(definitions[it.name])
                    else -> null
                }
            }
            .mapNotNull {
                it.takeIf {
                    it.directives.isNullOrEmpty() || conditional.shouldInclude(
                        it,
                        variables,
                        environment.graphQLSchema,
                        environment.graphQlContext
                    )
                }
            }
            .flatMap {
                val path = if (it is FragmentDefinition) parent else {
                    val name = (it as NamedNode<*>).name
                    if (parent.isNotBlank()) "$parent.$name" else name
                }

                it.selectionSet?.fieldNames(environment, definitions, variables, path).orEmpty() + path
            }
    }
}
