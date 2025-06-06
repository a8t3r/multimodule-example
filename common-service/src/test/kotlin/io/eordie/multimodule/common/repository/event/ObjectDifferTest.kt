package io.eordie.multimodule.common.repository.event

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isNotNull
import io.eordie.multimodule.contracts.library.models.Author
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class ObjectDifferTest {
    @Test
    fun `should compare two objects with auditable type`() {
        val a = Author(UUID.randomUUID(), "foo", "bar", OffsetDateTime.now(), OffsetDateTime.now())
        val b = a.copy(firstName = "foz", lastName = null)
        val difference = ObjectDiffer.difference(a, b)
        assertThat(difference).isNotNull()
        assertThat(difference!!.set).isEmpty()
        assertThat(difference.unset).containsExactly(Author::lastName.name)
        assertThat(difference.updated).containsExactly(Author::firstName.name)
    }
}
