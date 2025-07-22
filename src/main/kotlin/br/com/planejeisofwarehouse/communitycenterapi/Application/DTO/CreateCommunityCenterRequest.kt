package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateCommunityCenterRequest(
    @field:NotBlank(message = "O nome do centro não pode estar em branco")
    @field:Schema(description = "Nome do centro", example = "Centro Cultural Zona Sul")
    val name: String,

    @field:NotBlank(message = "O endereço do centro não pode estar em branco")
    val address: String,

    @field:NotNull(message = "A latitude não pode ser nula")
    val latitude: Double,

    @field:NotNull(message = "A longitude não pode ser nula")
    val longitude: Double,

    @field:Min(value = 1, message = "A capacidade máxima deve ser pelo menos 1")
    @field:Schema(description = "Capacidade total", example = "91")
    val maxCapacity: Int,

    @field:Min(value = 0, message = "A ocupação atual não pode ser negativa")
    val currentOccupation: Int = 0, // Default para 0 se não for fornecido

    val resources: List<ResourceRequest> = emptyList() // Lista de recursos iniciais
)
