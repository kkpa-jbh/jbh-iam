package com.jbh.iam.api.domain

inline fun <reified M : DTOConverter<D, M>, D> List<M>.toDTOList(): List<D> {
    return this.map {
        it.toDTO()
    }
}

inline fun <reified M : DTOConverter<D, M>, D> List<M>.toDTOMutableList(): MutableList<D> {
    return this.map {
        it.toDTO()
    }.toMutableList()
}

inline fun <reified M : DTOConverter<D, M>, D> Set<M>.toDTOSet(): Set<D> {
    return this.map {
        it.toDTO()
    }.toSet()
}

inline fun <reified M : DTOConverter<D, M>, D> MutableSet<M>.toDTOMutableSet(): MutableSet<D> {
    return this.map {
        it.toDTO()
    }.toMutableSet()
}

interface DTOConverter<D, M : DTOConverter<D, M>> {

    fun toDTO(): D
}
