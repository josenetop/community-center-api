package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateCommunityCenterRequest(
    @field:Schema(description = "Nome do centro", example = "Centro Cultural Zona Sul")
    val name: String,

    @field:NotBlank(message = "O endereço do centro não pode estar em branco")
    val address: String,

    @field:NotNull(message = "A latitude não pode ser nula")
    val latitude: Double,

    @field:NotNull(message = "A longitude não pode ser nula")
    val longitude: Double,

    val maxCapacity: Int = 0,

    val currentOccupation: Int = 0, // Default para 0 se não for fornecido

    val resources: List<ResourceRequest> = emptyList() // Lista de recursos iniciais
)
