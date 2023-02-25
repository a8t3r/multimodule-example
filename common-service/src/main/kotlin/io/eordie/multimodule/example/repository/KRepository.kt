package io.eordie.multimodule.example.repository

import io.micronaut.aop.Introduction

@Introduction
@Retention(AnnotationRetention.RUNTIME)
annotation class KRepository
