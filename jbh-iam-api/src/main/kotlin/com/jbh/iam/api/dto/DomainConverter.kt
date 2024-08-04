package com.jbh.iam.api.dto

inline fun <reified D : DomainConverter<D, M>, M> MutableSet<D>.toDomainMutableSet(): MutableSet<M> {
    return this.map {
        it.toDomain()
    }.toMutableSet()
}

interface DomainConverter<D, M> {

    fun toDomain(): M
}
