package br.com.planejeisofwarehouse.communitycenterapi.Domain.Model

import jakarta.annotation.Resource
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class ExchangeHistory(
    @Id
    var id: String? = null,
    val centerOneId: String, // ID do primeiro centro envolvido
    val centerTwoId: String, // ID do segundo centro envolvido
    val offeredResourcesCenterOne: List<Resource>, // Recursos oferecidos pelo centro 1 (snapshot)
    val receivedResourcesCenterOne: List<Resource>, // Recursos recebidos pelo centro 1 (snapshot)
    val offeredResourcesCenterTwo: List<Resource>, // Recursos oferecidos pelo centro 2 (snapshot)
    val receivedResourcesCenterTwo: List<Resource>, // Recursos recebidos pelo centro 2 (snapshot)
    val exchangeDate: LocalDateTime = LocalDateTime.now(), // Data e hora da negociação
    val pointsExchanged: Int // Total de pontos da negociação (para ambos os lados)
)
