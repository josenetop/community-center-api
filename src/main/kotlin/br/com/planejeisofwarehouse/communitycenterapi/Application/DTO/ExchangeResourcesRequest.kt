package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class ExchangeResourcesRequest(
    @field:NotBlank(message = "O ID do Centro 1 é obrigatório.")
    val centerOneId: String,

    @field:NotBlank(message = "O ID do Centro 2 é obrigatório.")
    val centerTwoId: String,

    @field:Valid
    @field:NotEmpty(message = "Os recursos oferecidos pelo Centro 1 não podem ser vazios.")
    val resourcesOfferedByCenterOne: List<ResourceRequest>,

    @field:Valid
    @field:NotEmpty(message = "Os recursos oferecidos pelo Centro 2 não podem ser vazios.")
    val resourcesOfferedByCenterTwo: List<ResourceRequest>
)
