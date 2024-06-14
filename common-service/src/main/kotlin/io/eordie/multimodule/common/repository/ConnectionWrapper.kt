package io.eordie.multimodule.common.repository

import java.sql.Connection
import kotlin.coroutines.CoroutineContext

internal class ConnectionWrapper(delegate: Connection, val context: CoroutineContext) : Connection by delegate
