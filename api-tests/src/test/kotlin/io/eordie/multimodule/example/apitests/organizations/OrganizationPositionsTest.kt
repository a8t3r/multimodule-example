package io.eordie.multimodule.example.apitests.organizations

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.example.apitests.AbstractApplicationTest
import io.eordie.multimodule.example.apitests.AuthUtils.authWith
import io.eordie.multimodule.example.contracts.basic.exception.ValidationException
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPositionInput
import io.eordie.multimodule.example.contracts.organization.services.OrganizationStructureMutations
import io.eordie.multimodule.example.contracts.organization.services.OrganizationStructureQueries
import io.eordie.multimodule.example.contracts.utils.Roles
import io.micronaut.test.annotation.Sql
import jakarta.inject.Inject
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.InvocationTargetException

@Sql("initial-keycloak.sql", phase = Sql.Phase.BEFORE_ALL)
class OrganizationPositionsTest : AbstractApplicationTest() {

    companion object {
        private val organizationManager = authWith(
            defaultOrganization,
            Roles.MANAGE_ORGANIZATION,
            Roles.VIEW_ORGANIZATION
        )
    }

    @Inject
    lateinit var positionQueries: OrganizationStructureQueries

    @Inject
    lateinit var positionMutations: OrganizationStructureMutations

    @BeforeAll
    fun init() = runTest(organizationManager) {
        positionMutations.internalTruncate(OrganizationPositionFilter())
    }

    private suspend fun newPosition(name: String, parent: OrganizationPosition?): OrganizationPosition =
        positionMutations.position(
            defaultOrganization,
            OrganizationPositionInput(
                null,
                name,
                listOf(Roles.VIEW_ORGANIZATION.humanName()),
                parent?.id
            )
        )

    private suspend fun createExampleStructure(): Map<String, OrganizationPosition> = withContext(organizationManager) {
        val ctoPosition = newPosition("CTO", null)
        val seniorPosition = newPosition("Senior Developer", ctoPosition)
        val qaSeniorPosition = newPosition("QA Senior Developer", ctoPosition)
        val juniorPosition = newPosition("Junior Developer", seniorPosition)

        val positions = positionQueries.positions(defaultOrganization, OrganizationPositionFilter())
        assertThat(positions).hasSize(4)
        assertThat(positions.map { it.name to it.parentId }).containsExactly(
            ctoPosition.name to null,
            seniorPosition.name to ctoPosition.id,
            qaSeniorPosition.name to ctoPosition.id,
            juniorPosition.name to seniorPosition.id
        )
        positions.associateBy { it.name }
    }

    @Test
    fun `should create example organization structure`() = runTest(organizationManager) {
        createExampleStructure()
    }

    @Test
    fun `should replace parent in organization structure`() = runTest(organizationManager) {
        val positionIndex = createExampleStructure()
        val ctoId = positionIndex.getValue("CTO").id
        positionMutations.changePositionsParent(
            positionIndex.getValue("Senior Developer").id,
            ctoId
        )

        val positions = positionQueries.positions(defaultOrganization, OrganizationPositionFilter())
        assertThat(positions).hasSize(4)
        assertThat(positions.map { it.name to it.parentId }).containsExactly(
            "CTO" to null,
            "Senior Developer" to ctoId,
            "QA Senior Developer" to ctoId,
            "Junior Developer" to ctoId
        )
    }

    @Test
    fun `should detect cycle on position update`() = runTest(organizationManager) {
        val positionIndex = createExampleStructure()
        val ctoPosition = positionIndex.getValue("CTO")
        val e = assertThrows<InvocationTargetException> {
            positionMutations.position(
                defaultOrganization,
                OrganizationPositionInput(
                    ctoPosition.id,
                    ctoPosition.name,
                    ctoPosition.roles,
                    positionIndex.getValue("Junior Developer").id
                )
            )
        }
        assertThat(e.targetException).isInstanceOf(ValidationException::class.java)
        val errors = (e.targetException as ValidationException).errors
        assertThat(errors).hasSize(1)
    }

    @Test
    fun `should create simple organization structure`() = runTest(organizationManager) {
        val ctoPosition = newPosition("CTO", null)
        assertThat(ctoPosition.id).isNotNull()
        assertThat(ctoPosition.name).isEqualTo("CTO")
        assertThat(ctoPosition.roles).hasSize(1)
        assertThat(ctoPosition.parentId).isNull()

        val seniorPosition = newPosition("Senior Developer", ctoPosition)
        assertThat(seniorPosition.id).isNotNull()
        assertThat(seniorPosition.name).isEqualTo("Senior Developer")
        assertThat(seniorPosition.roles).hasSize(1)
        assertThat(seniorPosition.parentId).isEqualTo(ctoPosition.id)

        val positions = positionQueries.positions(defaultOrganization)
        assertThat(positions).hasSize(2)
        assertThat(positions.map { it.name to (it.parentId == null) }).containsExactly(
            "CTO" to true,
            "Senior Developer" to false
        )
    }
}
