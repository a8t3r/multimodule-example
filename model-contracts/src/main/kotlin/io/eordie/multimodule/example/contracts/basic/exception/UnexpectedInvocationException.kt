package io.eordie.multimodule.example.contracts.basic.exception

class UnexpectedInvocationException(val definition: ExceptionDefinition) :
    RuntimeException(definition.message)
