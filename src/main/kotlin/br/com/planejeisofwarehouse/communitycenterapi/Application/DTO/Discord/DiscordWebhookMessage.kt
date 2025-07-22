package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord

data class DiscordWebhookMessage(
    val content: String? = null,
    val username: String? = "CommunityCenterAPI",
    val avatar_url: String? = null,
    val embeds: List<Embed>? = null
)