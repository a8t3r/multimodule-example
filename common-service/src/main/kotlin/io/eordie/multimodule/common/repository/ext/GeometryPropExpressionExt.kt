package io.eordie.multimodule.common.repository.ext

import io.eordie.multimodule.common.utils.asPostgis
import io.eordie.multimodule.common.utils.jts
import io.eordie.multimodule.contracts.basic.geometry.SpatialReference
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.sql

infix fun <T : SpatialReference> KPropExpression<out T>.contains(point: TPoint): KNonNullExpression<Boolean> {
    val expression = this
    return sql(Boolean::class, "ST_Contains(%e, ${point.jts().asPostgis()})") {
        expression(expression)
    }
}
