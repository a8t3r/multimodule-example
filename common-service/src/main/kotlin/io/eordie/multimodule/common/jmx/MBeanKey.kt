package io.eordie.multimodule.common.jmx

import org.babyfish.jimmer.sql.Embeddable

@Embeddable
interface MBeanKey {
    val name: String
    val property: String
}
