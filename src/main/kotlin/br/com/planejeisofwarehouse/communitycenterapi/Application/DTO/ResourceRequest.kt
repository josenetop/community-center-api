package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO

import br.com.planejeisofwarehouse.communitycenterapi.Application.Enum.ResourceType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class ResourceRequest(
    @field:NotNull(message = "O tipo do recurso não pode ser nulo")
    val type: ResourceType,
    @field:Min(value = 1, message = "A quantidade do recurso deve ser positiva")
    val quantity: Int
)
