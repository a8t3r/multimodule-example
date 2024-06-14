package io.eordie.multimodule.contracts.basic.filters

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore

interface StringRepresentationAware<T : Comparable<T>> : ComparableFilter<T> {
    @GraphQLIgnore
    fun transformToStringLiteral(): StringLiteralFilter {
        return StringLiteralFilter(
            eq?.toString(),
            ne?.toString(),
            of?.map { it.toString() },
            nof?.map { it.toString() },
            exists,
            gt?.toString(),
            gte?.toString(),
            lt?.toString(),
            lte?.toString()
        )
    }
}
