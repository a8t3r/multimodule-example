package io.eordie.multimodule.library

import io.dekorate.annotation.Dekorate
import io.eordie.multimodule.common.GENERATED_RESOURCE
import io.eordie.multimodule.common.runApplication

@Dekorate(resources = [ GENERATED_RESOURCE ])
object Application

fun main(args: Array<String>) {
    runApplication(args)
}
