package io.eordie.multimodule.api.tests.regions

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.each
import assertk.assertions.extracting
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isIn
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import io.eordie.multimodule.api.tests.AbstractApplicationTest
import io.eordie.multimodule.contracts.basic.filters.IntNumericFilter
import io.eordie.multimodule.contracts.basic.filters.LongNumericFilter
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.geometry.GeoJsonPoint
import io.eordie.multimodule.contracts.regions.models.Region
import io.eordie.multimodule.contracts.regions.models.RegionsFilter
import io.eordie.multimodule.contracts.regions.service.RegionQueries
import jakarta.inject.Inject
import kotlinx.coroutines.future.await
import org.junit.jupiter.api.Test
import kobby.kotlin.entity.Region as GraphqlRegion

class RegionsTest : AbstractApplicationTest() {

    @Inject
    lateinit var regions: RegionQueries

    @Test
    fun `should get region by id`() = test {
        val region = requireNotNull(regions.region(2804758))

        assertThat(region.id).isEqualTo(2804758)
        assertThat(region.depth).isEqualTo(1)
        assertThat(region.parentId).isEqualTo(9407)
        assertThat(region.country).isEqualTo("AD")
        assertThat(region.name(env()).await()).isEqualTo("Ordino")
    }

    @Test
    fun `should get region path by id`() = test {
        val regions = regions.regionPath(2804758)
        assertThat(regions).hasSize(2)
        assertThat(regions).extracting(Region::id).containsExactly(9407L, 2804758L)
    }

    @Test
    fun `should query regions from andorra`() = test {
        val page = regions.regions(RegionsFilter(country = StringLiteralFilter { eq = "AD" }))
        assertThat(page.data).hasSize(8)

        val groups = page.data.groupBy { it.depth }.withDefault { emptyList() }
        assertThat(groups.getValue(0)).hasSize(1)
        assertThat(groups.getValue(1)).hasSize(7)
        assertThat(groups.getValue(2)).isEmpty()
        assertThat(groups.getValue(3)).isEmpty()
    }

    @Test
    fun `should find region by name`() = test {
        val page = regions.regions(
            RegionsFilter(
                name = StringLiteralFilter {
                    startsWith = "Ord"
                    endsWith = "ino"
                }
            )
        )
        assertThat(page.data).hasSize(1)

        val region = page.data.first()
        assertThat(region.id).isEqualTo(2804758)
        assertThat(region.depth).isEqualTo(1)
        assertThat(region.parentId).isEqualTo(9407)
        assertThat(region.country).isEqualTo("AD")
        assertThat(region.name(env()).await()).isEqualTo("Ordino")
    }

    @Test
    fun `should query region by complex filter`() = test {
        val filter = RegionsFilter(
            country = StringLiteralFilter { eq = "AD" },
            depth = IntNumericFilter { of = listOf(0, 1, 2, 3) },
            parentId = LongNumericFilter { nil = true }
        )
        val page = regions.regions(filter)
        assertThat(page.data).hasSize(1)

        val region = page.data.first()
        assertThat(region.id).isEqualTo(9407)
        assertThat(region.depth).isEqualTo(0)
        assertThat(region.parentId).isNull()
        assertThat(region.country).isEqualTo("AD")
    }

    @Test
    fun `should find region by point`() = test {
        val result = schema.query {
            findRegionsByPoint(point = GeoJsonPoint(1.576090241356109, 42.54523447931922)) {
                country = "AD"
                id
                country
                centroid
            }
        }

        assertThat(result.findRegionsByPoint).isNotNull().all {
            hasSize(2)
            each {
                it.prop(GraphqlRegion::id).isIn(9407L, 2804755L)
                it.prop(GraphqlRegion::centroid).isNotNull()
                it.prop(GraphqlRegion::country).isEqualTo("AD")
            }
        }
    }
}
