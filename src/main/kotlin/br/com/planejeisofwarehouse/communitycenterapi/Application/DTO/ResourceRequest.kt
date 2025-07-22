package br.com.planejeisofwarehouse.communitycenterapi.Application.DTO

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class ResourceRequest(
    @field:NotBlank(message = "O tipo de recurso não pode estar em branco")
    val type: String,

    @field:Min(value = 1, message = "A quantidade do recurso deve ser pelo menos 1")
    val quantity: Int
)
