package br.com.planejeisofwarehouse.communitycenterapi.Domain.Model

import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.UUID

data class ExchangeHistory(
    @Id
    var id: String? = null,
    val centerOneId: UUID, // ID do primeiro centro envolvido
    val centerTwoId: UUID, // ID do segundo centro envolvido
    val offeredResourcesCenterOne: List<Resource>, // Recursos oferecidos pelo centro 1 (snapshot)
    val receivedResourcesCenterOne: List<Resource>, // Recursos recebidos pelo centro 1 (snapshot)
    val offeredResourcesCenterTwo: List<Resource>, // Recursos oferecidos pelo centro 2 (snapshot)
    val receivedResourcesCenterTwo: List<Resource>, // Recursos recebidos pelo centro 2 (snapshot)
    val pointsCenterOne: Int, // Pontos totais oferecidos pelo centro 1
    val pointsCenterTwo: Int, // Pontos totais oferecidos pelo centro 2
    val isHighOccupancyExemptionApplied: Boolean, // Indica se a regra de 90%+ foi aplicada
    val exchangeDate: LocalDateTime = LocalDateTime.now(), // Data e hora da negociação
    val pointsExchanged: Int // Total de pontos da negociação (para ambos os lados)
)
