package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Service

import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord.DiscordWebhookMessage
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord.Embed
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord.Field
import org.slf4j.LoggerFactory
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
    @Value("\${discord.webhook.general-alerts}")
    val discordGeneralWebhookUrl: String,
    @Value("\${discord.webhook.exchange-alerts}")
    val discordExchangeWebhookUrl: String,
    private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(NotifierService::class.java)

    fun sendCapacityAlert(
        centerId: UUID,
        centerName: String,
        maxCapacity: Int
    ) {
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
        sendWebhookMessage(message, discordGeneralWebhookUrl)
        logger.info("Alerta de capacidade máxima enviado para Discord para o centro: $centerName (ID: $centerId)")
    }

    fun sendHighOccupancyWarning(
        centerId: UUID,
        centerName: String,
        currentOccupation: Int,
        maxCapacity: Int
    ) {
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
        sendWebhookMessage(message, discordGeneralWebhookUrl)
        logger.warn("Aviso de alta ocupação enviado para Discord para o centro: $centerName (ID: $centerId)")
    }

    fun sendExchangeHighOccupancyExemptionNotification(
        centerId: UUID,
        centerName: String,
        pointsOffered: Int,
        pointsReceived: Int,
        otherCenterName: String
    ) {
        val message = DiscordWebhookMessage(
            username = "Alerta de Intercâmbio",
            embeds = listOf(
                Embed(
                    title = "ℹ️ Exceção de Alta Ocupação no Intercâmbio",
                    description = "Uma exceção de alta ocupação foi aplicada durante um intercâmbio de recursos.\n" +
                            "**Centro:** ${centerName}\n" +
                            "**Outro Centro:** ${otherCenterName}",
                    color = 65535, // Azul claro
                    fields = listOf(
                        Field(name = "ID do Centro", value = centerId.toString(), inline = true),
                        Field(name = "Pontos Oferecidos", value = pointsOffered.toString(), inline = true),
                        Field(name = "Pontos Recebidos", value = pointsReceived.toString(), inline = true)
                    ),
                    timestamp = Instant.now().toString()
                )
            )
        )
        sendWebhookMessage(message, discordExchangeWebhookUrl)
        logger.info("Notificação de exceção de alta ocupação no intercâmbio enviada para " +
                "Discord para o centro: $centerName (ID: $centerId)"
        )
    }

    fun sendWebhookMessage(message: DiscordWebhookMessage, webhookUrl: String) {
        if (webhookUrl.isBlank() || webhookUrl.contains("ID_WEBHOOK")) {
            logger.warn("URL do webhook do Discord não configurada. Notificação não enviada.")
            return
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(message, headers)

        try {
            restTemplate.postForEntity(webhookUrl, entity, String::class.java)
        } catch (e: Exception) {
            logger.error("Erro ao enviar notificação para o Discord webhook $webhookUrl: ${e.message}", e)
        }
    }
}