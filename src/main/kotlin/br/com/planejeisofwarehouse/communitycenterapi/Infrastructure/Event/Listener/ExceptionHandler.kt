package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Event.Listener

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleGenericException (
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse (
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Erro interno no servidor",
            message = ex.message ?: "Erro Desconhecido",
            path = request.getDescription(false).removePrefix("URI=")

        )

        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}