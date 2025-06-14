package io.eordie.multimodule.common.filter

import io.eordie.multimodule.contracts.basic.filters.IntNumericFilter
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FilterExtTest {

    @Test
    fun `should support null value`() {
        val filter = StringLiteralFilter {
            nil = true
        }

        assertTrue(filter matches null)
        assertFalse(filter matches "foo")
        assertFalse(filter matches "bar")
    }

    @Test
    fun `string should match string filter`() {
        val filter = StringLiteralFilter {
            of = listOf("foo", "bar")
            nof = listOf("buzz", "fee")
            nil = false
        }

        assertTrue(filter matches "foo")
        assertTrue(filter matches "bar")
        assertFalse(filter matches "buzz")
        assertFalse(filter matches "fee")
        assertFalse(filter matches "foobar")
    }

    @Test
    fun `string list should match string filter`() {
        val filter = StringLiteralFilter {
            of = listOf("foo", "bar")
            nof = listOf("buzz", "fee")
            nil = false
        }

        assertTrue(filter anyMatches listOf("foo"))
        assertTrue(filter anyMatches listOf("bar"))
        assertTrue(filter anyMatches listOf("foo", "bar", "buzz"))
        assertFalse(filter anyMatches listOf("buzz", "fee", "foobar"))
    }

    @Test
    fun `int should match integer filter`() {
        val filter = IntNumericFilter {
            gt = 10
            lt = 20
        }

        assertTrue(filter matches 15)
        assertFalse(filter matches 1)
        assertFalse(filter matches 20)
    }

    @Test
    fun `int range should match integer filter`() {
        val filter = IntNumericFilter {
            gt = 10
            lt = 20
        }

        assertTrue(filter anyMatches (1..40).toList())
        assertFalse(filter anyMatches (1..10).toList())
        assertFalse(filter anyMatches (20..40).toList())
    }
}
