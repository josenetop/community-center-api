package br.com.planejeisofwarehouse.communitycenterapi.Domain.Model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.UUID

@Document(collection = "communityCenters")
data class CommunityCenter(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val andrees: String,
    val latitude: Double,
    val longitude: Double,
    var maxCapacity: Int,
    var currentOccupation: Int,
    var resources: MutableList<Resource> = mutableListOf(),
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null
) {
    init {
        require(maxCapacity >= 0) { "Capacidade máxima não pode ser nagativa" }
        require(currentOccupation >= 0 ) { "Ocupação não atual não pode ser negativa" }
        require(currentOccupation <= maxCapacity) { "Ocupação atual não pode ser maior que a capacidade  máxima" }
    }

//    Verifica se a ocupação do centro é maior que 90%
    fun isOccupancyHigh(): Boolean {
        return currentOccupation.toDouble() / maxCapacity > 0.90
    }
}
