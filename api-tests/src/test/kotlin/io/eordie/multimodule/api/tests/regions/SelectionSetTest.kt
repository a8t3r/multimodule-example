package io.eordie.multimodule.api.tests.regions

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.api.tests.AbstractApplicationTest
import io.eordie.multimodule.common.rsocket.context.SelectionSetContextElement
import io.eordie.multimodule.contracts.basic.filters.LongNumericFilter
import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import io.eordie.multimodule.contracts.regions.models.RegionsFilter
import io.eordie.multimodule.contracts.regions.service.RegionQueries
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

class SelectionSetTest : AbstractApplicationTest() {
    @Inject
    lateinit var regions: RegionQueries

    @Test
    fun `should select only projection fields`() =
        test(authorization + SelectionSetContextElement(SelectionSet(listOf("id", "name")))) {
            val page = regions.regions(RegionsFilter(id = LongNumericFilter(eq = 2804758)))
            val region = page.data.single()
            assertThat(region.id).isEqualTo(2804758)
            assertThat(region.depth).isEqualTo(0)
            assertThat(region.parentId).isNull()
            assertThat(region.country).isEqualTo("dead-beef")
        }
}
