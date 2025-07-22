package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord

data class Field(
    val name: String,
    val value: String,
    val inline: Boolean? = false
)
