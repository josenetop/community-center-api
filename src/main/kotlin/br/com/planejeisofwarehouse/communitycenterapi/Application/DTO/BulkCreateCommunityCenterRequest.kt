package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

@Schema(description = "Requisição para criar múltiplos centros comunitários em massa")
data class BulkCreateCommunityCenterRequest(
    @Schema(description = "Lista de centros comunitários a serem criados")
    @field:Valid
    val centers: List<CreateCommunityCenterRequest>
)
