package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Service

import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord.DiscordWebhookMessage
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord.Embed
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord.Field
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.UUID

@Service
class NotifierService(
    @Value("\${discord.webhook.url}")
    private val discordWebhookURL: String,
    private val restTemplate: RestTemplate
) {

    fun sendCapacityAlert(centerId: UUID, centerName: String, maxCapacity: Int) {
        val message = DiscordWebhookMessage(
            username = "Alerta de Capacidade",
            embeds = listOf(
                Embed(
                    title = "🚨 CAPACIDADE MÁXIMA ATINGIDA! 🚨",
                    description = "O centro comunitário **${centerName}** atingiu ou excedeu sua capacidade máxima!",
                    color = 16711680,
                    fields = listOf(
                        Field(name = "ID do Centro", value = centerId.toString(), inline = true),
                        Field(name = "Capacidade Máxima", value = maxCapacity.toString(), inline = true)
                    ),
                    timestamp = Instant.now().toString()
                )
            )
        )
        sendWebhookMessage(message)
    }

    fun sendHighOccupancyWarning(centerId: UUID, centerName: String, currentOccupation: Int, maxCapacity: Int) {
        val message = DiscordWebhookMessage(
            username = "Aviso de Ocupação",
            embeds = listOf(
                Embed(
                    title = "⚠️ ALTA OCUPAÇÃO DETECTADA! ⚠️",
                    description = "O centro comunitário **${centerName}** está com alta ocupação.",
                    color = 16776960,
                    fields = listOf(
                        Field(name = "ID do Centro", value = centerId.toString(), inline = true),
                        Field(name = "Ocupação Atual", value = currentOccupation.toString(), inline = true),
                        Field(name = "Capacidade Máxima", value = maxCapacity.toString(), inline = true)
                    ),
                    timestamp = Instant.now().toString()
                )
            )
        )
        sendWebhookMessage(message)
    }

    private fun sendWebhookMessage(message: DiscordWebhookMessage) {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(message, headers)

        try {
            restTemplate.postForEntity(discordWebhookURL, entity, String::class.java)
            println("Notificação enviada com sucesso para o Discord.")
        } catch (e: Exception) {
            System.err.println("Erro ao enviar notificação para o Discord: ${e.message}")
            e.printStackTrace()
        }
    }
}