package io.eordie.multimodule.example.rsocket

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.example.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.example.contracts.library.models.BooksFilter
import io.eordie.multimodule.example.contracts.utils.Roles
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.eordie.multimodule.example.rsocket.meta.ProtobufPayloadBuilder
import io.ktor.utils.io.core.*
import io.micronaut.core.annotation.Introspected
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaType

class ProtobufPayloadBuilderTest {
    private val proto = ProtobufPayloadBuilder()

    private val book = BookDemo::class.createType()
    private val filter = BooksFilter::class.createType()
    private val nullableBook = BookDemo::class.createType(nullable = true)
    private val pageOfBooks =
        Page::class.createType(listOf(KTypeProjection(KVariance.OUT, BookDemo::class.createType())))

    @Introspected
    @Serializable
    data class BookDemo(
        val id: UuidStr,
        val name: String,
        val authorIds: List<UuidStr>
    )

    @Test
    fun `should serde data class`() {
        val expected = BookDemo(UUID.randomUUID(), "foobar", listOf(UUID.randomUUID()))
        val payload = proto.encodeToPayload(expected)
        val actual = proto.decodeFromPayload(payload, BookDemo::class)
        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun `should serde null value for data type`() {
        val payload = proto.encodeToPayload(null, nullableBook)
        val actual = proto.decodeFromPayload(payload, nullableBook)
        assertThat(actual).isNull()
    }

    @Test
    fun `should serde generic type`() {
        val expectedPage = Page(
            listOf(BookDemo(UUID.randomUUID(), "foobar", listOf(UUID.randomUUID()))),
            Pageable("foobar")
        )

        val payload = proto.encodeToPayload(expectedPage, pageOfBooks)
        val actualPage = proto.decodeFromPayload(payload, pageOfBooks)
        assertThat(expectedPage).isEqualTo(actualPage)
    }

    @Test
    fun `should serde argument list`() {
        val expectedFilter = BooksFilter(id = UUIDLiteralFilter(eq = UUID.randomUUID()))
        val expectedBook = BookDemo(UUID.randomUUID(), "foobar", listOf(UUID.randomUUID()))
        val payload = proto.encodeToPayload(
            listOf(expectedFilter, expectedBook),
            listOf(filter.javaType, book.javaType)
        )

        val (actualFilter, actualBook) = proto.decodeFromPayload(payload, listOf(filter, book))
        assertThat(expectedFilter).isEqualTo(actualFilter)
        assertThat(expectedBook).isEqualTo(actualBook)
    }

    @Test
    fun `should serde nullable values`() {
        val payload = proto.encodeToPayload(
            listOf(null, null),
            listOf(book.javaType, filter.javaType)
        )

        val (book, filter) = proto.decodeFromPayload(payload, listOf(nullableBook, filter))
        assertThat(book).isNull()
        assertThat((filter as BooksFilter)).isNotNull()
        assertThat(filter.id).isNull()
        assertThat(filter.name).isNull()
        assertThat(filter.authors).isNull()
        assertThat(filter.deleted).isNull()
    }

    @Test
    fun `should fail on deserialization null value without default parameters`() {
        val ex = assertThrows(SerializationException::class.java) {
            val payload = buildPayload { data { writeInt(0) } }
            proto.decodeFromPayload(payload, pageOfBooks)
        }
        assertThat(ex.message).startsWith("Field 'pageable' is required for type")
    }

    @Test
    fun `should serialize and deserialize auth details`() {
        val expected = AuthenticationDetails(
            UUID.randomUUID(),
            "foobar",
            listOf(Roles.MANAGE_ORGANIZATIONS, Roles.VIEW_MEMBERS),
            active = false,
            emailVerified = true,
            currentOrganizationId = UUID.randomUUID(),
            organizationRoles = null
        )

        val actual = proto.decodeFromPayload(proto.encodeToPayload(expected), AuthenticationDetails::class)
        assertThat(actual).isEqualTo(expected)
    }
}
