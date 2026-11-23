package com.example.mom.data.mapper

import com.example.mom.data.remote.localserver.entity.ServerChildEntity
import com.example.mom.domain.model.Child

fun ServerChildEntity.toDomain(): Child =
    Child(
        id = id,
        name = name,
    )

fun Child.toServerEntity(): ServerChildEntity =
    ServerChildEntity(
        id = id,
        name = name,
    )