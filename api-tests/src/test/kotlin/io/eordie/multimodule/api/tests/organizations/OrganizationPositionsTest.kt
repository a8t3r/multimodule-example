package io.eordie.multimodule.api.tests.organizations

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionInput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrganizationPositionsTest : AbstractOrganizationTest() {

    @BeforeEach
    fun init() = test(organizationManager) { truncatePositions(developersOrg) }

    @Test
    fun `should create example organization structure`() = test(organizationManager) {
        createExampleStructure(developersOrg)
    }

    @Test
    fun `position creation by key should be idempotent`() = test(organizationManager) {
        val one = newPosition(developersOrg, "Idempotent", null)
        val two = newPosition(developersOrg, "Idempotent", null)
        assertThat(one.id).isEqualTo(two.id)
        assertThat(one.name).isEqualTo(two.name)
    }

    @Test
    fun `should allow multiple deletion`() = test(organizationManager) {
        val position = newPosition(developersOrg, "Deletion", null)
        assertThat(structureMutations.deletePosition(position.id)).isEqualTo(true)
        assertThat(structureMutations.deletePosition(position.id)).isEqualTo(false)
    }

    @Test
    fun `should replace parent in organization structure`() = test(organizationManager) {
        val positionIndex = createExampleStructure(developersOrg)
        val ctoId = positionIndex.getValue("CTO").id
        structureMutations.changePositionsParent(
            positionIndex.getValue("Senior Developer").id,
            ctoId
        )

        val positions = structureQueries.positions(developersOrg, OrganizationPositionFilter())
        assertThat(positions).hasSize(4)
        assertThat(positions.map { it.name to it.parentId }).containsExactlyInAnyOrder(
            "CTO" to null,
            "Senior Developer" to ctoId,
            "QA Senior Developer" to ctoId,
            "Junior Developer" to ctoId
        )
    }

    @Test
    fun `should detect cycle on position update`() = test(organizationManager) {
        val positionIndex = createExampleStructure(developersOrg)
        val ctoPosition = positionIndex.getValue("CTO")
        val e = assertThrows<ValidationException> {
            structureMutations.position(
                developersOrg,
                OrganizationPositionInput(
                    ctoPosition.id,
                    ctoPosition.name,
                    ctoPosition.roles,
                    positionIndex.getValue("Junior Developer").id
                )
            )
        }
        assertThat(e.errors).hasSize(1)
        assertThat(e.errors[0].constraint).isEqualTo("Cycle")
        assertThat(e.errors[0].message).isEqualTo("Cycle detected between parent and child")
    }

    @Test
    fun `should create simple organization structure`() = test(organizationManager) {
        val ctoPosition = newPosition(developersOrg, "CTO", null)
        assertThat(ctoPosition.id).isNotNull()
        assertThat(ctoPosition.name).isEqualTo("CTO")
        assertThat(ctoPosition.roles).hasSize(1)
        assertThat(ctoPosition.parentId).isNull()

        val seniorPosition = newPosition(developersOrg, "Senior Developer", ctoPosition)
        assertThat(seniorPosition.id).isNotNull()
        assertThat(seniorPosition.name).isEqualTo("Senior Developer")
        assertThat(seniorPosition.roles).hasSize(1)
        assertThat(seniorPosition.parentId).isEqualTo(ctoPosition.id)

        val positions = structureQueries.positions(developersOrg)
        assertThat(positions).hasSize(2)
        assertThat(positions.map { it.name to (it.parentId == null) }).containsExactlyInAnyOrder(
            "CTO" to true,
            "Senior Developer" to false
        )
    }
}
