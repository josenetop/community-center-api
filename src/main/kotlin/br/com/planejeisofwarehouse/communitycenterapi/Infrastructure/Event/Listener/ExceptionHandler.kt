package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Event.Listener

import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord.DiscordWebhookMessage
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord.Embed
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.Discord.Field
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.CenterNotFoundException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.InvalidRequestException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.NotFoundHaveSufficientResourcesForTheExchangeException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.OccupationInvalidException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.OccupationNotFoundException
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Service.NotifierService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.PathMatcher
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.Instant
import java.time.LocalDateTime

@ControllerAdvice
class ExceptionHandler(
    private val notifierService: NotifierService,
    private val pathMatcher: PathMatcher
) {

    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleGenericException (
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {

        val path = request.getDescription(false).removePrefix("uri=")
        val message = ex.message ?: "Erro desconhecido"
        val httpStatus = getHttpStatus(ex)
        val statusCode = httpStatus.value()

        val error = ErrorResponse (
            timestamp = LocalDateTime.now(),
            status = statusCode,
            error = "Erro interno no servidor",
            message = message,
            path = path
        )

        try {
            val discordMessage = DiscordWebhookMessage(
                username = "⚠️ ALERTA DE ERRO NA API! ⚠️",
                embeds = listOf(
                    Embed(
                        title = "🚨 ERRO CRÍTICO NO SERVIDOR 🚨",
                        description = "Ocorreu um erro **não tratado** na aplicação. Por favor, verifique os logs.",
                        color = 16711680,
                        fields = listOf(
                            Field(name = "Endpoint da Requisição", value = path, inline = false),
                            Field(name = "Mensagem do Erro", value = message, inline = false),
                            Field(name = "Tipo de Exceção", value = ex.javaClass.simpleName, inline = true),
                            Field(name = "Status HTTP", value = statusCode.toString(), inline = true)
                        ),
                        timestamp = Instant.now().toString()
                    )
                )
            )

            notifierService.sendWebhookMessage(discordMessage, notifierService.discordExchangeWebhookUrl)
            logger.error("Alerta de erro crítico enviado para o Discord: ${message}")
        } catch (discordEx: Exception) {
            logger.error("Erro ao tentar enviar alerta de erro para o Discord: ${discordEx.message}", discordEx)
        }
        logger.error("Erro interno no servidor no endpoint $pathMatcher: $", ex)

        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun getHttpStatus(ex: Exception): HttpStatus {
        return when (ex) {
            is OccupationNotFoundException -> HttpStatus.NOT_FOUND
            is CenterNotFoundException -> HttpStatus.NOT_FOUND
            is NotFoundHaveSufficientResourcesForTheExchangeException -> HttpStatus.NOT_FOUND
            is IllegalArgumentException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }
}