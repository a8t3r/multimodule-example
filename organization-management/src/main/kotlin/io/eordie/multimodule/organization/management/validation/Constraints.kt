package io.eordie.multimodule.organization.management.validation

import org.valiktor.Constraint

interface OrganizationConstraint : Constraint {
    override val messageBundle: String get() = "organization/messages"
}

object UserAlreadyEmployed : OrganizationConstraint

object MissingMembership : OrganizationConstraint

object DepartmentNotBelongsToOrganization : OrganizationConstraint

object PendingInvitation : OrganizationConstraint

object AccessionRequestFinalState : OrganizationConstraint
