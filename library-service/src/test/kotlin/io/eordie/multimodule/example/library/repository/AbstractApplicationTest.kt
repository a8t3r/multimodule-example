package io.eordie.multimodule.example.library.repository

import io.micronaut.context.DefaultApplicationContextBuilder
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest(contextBuilder = [AbstractApplicationTest.CustomContextBuilder::class])
abstract class AbstractApplicationTest {

    class CustomContextBuilder : DefaultApplicationContextBuilder() {
        init {
            eagerInitSingletons(true)
            eagerInitConfiguration(true)
        }
    }
}
