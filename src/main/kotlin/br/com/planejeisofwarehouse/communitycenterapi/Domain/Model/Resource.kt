package br.com.planejeisofwarehouse.communitycenterapi.Domain.Model

import br.com.planejeisofwarehouse.communitycenterapi.Application.Enum.ResourceType

data class Resource(
    val type: ResourceType,
    var quantity: Int
) {
    fun calculatePoints(): Int {
        return type.points * quantity
    }
}
