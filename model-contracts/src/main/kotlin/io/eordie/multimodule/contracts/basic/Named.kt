package io.eordie.multimodule.contracts.basic

import io.eordie.multimodule.contracts.utils.UuidStr

interface Named {
    val id: UuidStr
    val name: String
}
