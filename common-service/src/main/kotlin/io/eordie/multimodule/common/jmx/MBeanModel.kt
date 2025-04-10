package io.eordie.multimodule.common.jmx

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "mbeans")
interface MBeanModel {
    @Id
    val id: MBeanKey
    val value: String?
    val actual: Boolean
}
