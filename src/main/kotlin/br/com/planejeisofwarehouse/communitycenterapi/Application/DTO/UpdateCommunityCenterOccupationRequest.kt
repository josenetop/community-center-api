package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class UpdateCommunityCenterOccupationRequest(
    @field:NotNull(message = "A ocupação atual não pode ser nula")
    @field:Min(value = 0, message = "A ocupação atual não pode ser negativa")
    val currentOccupation: Int
) {
}
