package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord

data class Embed(
    val title: String? = null,
    val description: String? = null,
    val color: Int? = null, // Cor em formato decimal (ex: 16711680 para vermelho)
    val fields: List<Field>? = null,
    val timestamp: String? = null
)
