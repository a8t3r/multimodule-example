package io.eordie.multimodule.api.tests.regions

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.eordie.multimodule.api.tests.AbstractApplicationTest
import io.eordie.multimodule.common.security.context.SelectionSetContextElement
import io.eordie.multimodule.contracts.basic.filters.LongNumericFilter
import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import io.eordie.multimodule.contracts.regions.models.Region
import io.eordie.multimodule.contracts.regions.models.RegionsFilter
import io.eordie.multimodule.contracts.regions.service.RegionQueries
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KCallable

class SelectionSetTest : AbstractApplicationTest() {
    @Inject
    lateinit var regions: RegionQueries

    private fun of(vararg fields: KCallable<Any>): CoroutineContext =
        authorization + SelectionSetContextElement(SelectionSet(fields.map { it.name }))

    @Test
    fun `should select only projection fields`() = test(of(Region::id)) {
        val page = regions.regions(RegionsFilter(id = LongNumericFilter { eq = 2804758 }))
        val region = page.data.single()
        assertThat(region.id).isEqualTo(2804758)
        assertThat(region.depth).isEqualTo(0)
        assertThat(region.parentId).isNull()
        assertThat(region.country).isEqualTo("dead-beef")
    }

    @Test
    fun `should select field by entity reference`() = test(of(Region::id, Region::parent)) {
        val page = regions.regions(RegionsFilter(id = LongNumericFilter { eq = 2804758 }))
        val region = page.data.single()
        assertThat(region.id).isEqualTo(2804758)
        assertThat(region.depth).isEqualTo(0)
        assertThat(region.parentId).isEqualTo(9407)
        assertThat(region.country).isEqualTo("dead-beef")
    }
}
