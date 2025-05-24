package io.eordie.multimodule.api.tests.regions

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.eordie.multimodule.api.tests.AbstractApplicationTest
import io.eordie.multimodule.common.utils.JtsUtils
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.regions.service.GeoCodingQueries
import jakarta.inject.Inject
import kotlinx.coroutines.future.await
import org.junit.jupiter.api.Test

class GeoCodingTest : AbstractApplicationTest() {

    @Inject
    lateinit var geoCoding: GeoCodingQueries

    @Test
    fun `should find regions by point`() = test {
        val env = env()
        val regions = geoCoding.findRegionsByPoint(TPoint(JtsUtils.WGS_84, 1.5401, 42.5872), country = "AD")
        assertThat(regions).hasSize(2)

        assertThat(regions[0].id).isEqualTo(9407)
        assertThat(regions[0].depth).isEqualTo(0)
        assertThat(regions[0].parentId).isNull()
        assertThat(regions[0].name(env).await()).isEqualTo("Andorra")

        assertThat(regions[1].id).isEqualTo(2804758)
        assertThat(regions[1].depth).isEqualTo(1)
        assertThat(regions[1].parentId).isEqualTo(9407)
        assertThat(regions[1].name(env).await()).isEqualTo("Ordino")
    }
}
