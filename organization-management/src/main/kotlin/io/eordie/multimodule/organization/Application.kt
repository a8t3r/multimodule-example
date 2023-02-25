package io.eordie.multimodule.organization

import io.dekorate.kubernetes.annotation.KubernetesApplication
import io.dekorate.kubernetes.annotation.Port
import io.dekorate.kubernetes.annotation.Protocol
import io.eordie.multimodule.example.runApplication

@KubernetesApplication(
    ports = [
        Port(name = "http", containerPort = 8080, protocol = Protocol.HTTP),
        Port(name = "rsocket", containerPort = 9000)
    ]
)
object Application

fun main(args: Array<String>) {
    runApplication(args)
}
