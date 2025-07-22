package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Service

import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.CreateCommunityCenterRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.UpdateCommunityCenterOccupationRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.Enum.ResourceType
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.CommunityCenter
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.Resource
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Repository.CommunityCenterRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class CommunityCenterService(
    private val repository: CommunityCenterRepository,
    private val notifierService: NotifierService
) {

    fun findAll(): List<CommunityCenter> {
        return repository.findAll()
    }

    fun findById(id: UUID): CommunityCenter? {
        return repository.findById(id).orElse(null)
    }

    fun createCommunityCenter(request: CreateCommunityCenterRequest): CommunityCenter {
        if (request.currentOccupation > request.maxCapacity)
            throw RuntimeException("A Ocupação não pode exceder a capacidade máxima")

        val resource = request.resources.map { resourceRequest ->
            val resourceType = ResourceType.fromString(resourceRequest.type)
                ?: throw RuntimeException("Tipo de Recurso ' ${resourceRequest} ' inválido ")
            Resource(type = resourceType, quantity = resourceRequest.quantity)
        }.toMutableList()

        val newCenter = CommunityCenter(
            name = request.name,
            andrees = request.address,
            maxCapacity = request.maxCapacity,
            currentOccupation = request.currentOccupation,
            resources = resource,
            createdAt = LocalDateTime.now(),
            latitude = request.latitude,
            longitude = request.longitude,
            updatedAt = null
        )

        return repository.save(newCenter)
    }

    fun updateCommunityCenterOccupation(
        id: UUID, request: UpdateCommunityCenterOccupationRequest
    ): CommunityCenter {
        val center = repository.findById(id).orElseThrow{
            Exception("Centro comunitário não encontrado. ID: $id")
        }

        if (request.currentOccupation > center.maxCapacity)
            throw Exception ("A nova ocupação (" +
                    "${request.currentOccupation}" +
                    ")) excede a capacidade máxima do centro (" +
                    "${center.name}" +
                    ")")

        center.currentOccupation = request.currentOccupation
        center.updatedAt = LocalDateTime.now()

        val updateCenter = repository.save(center)

        if (updateCenter.currentOccupation >= updateCenter.maxCapacity) {
            notifierService.sendCapacityAlert(
                updateCenter.id,
                updateCenter.name,
                updateCenter.maxCapacity
            )
        } else if (updateCenter.isOccupancyHigh()) {
            notifierService.sendHighOccupancyWarning(
                updateCenter.id,
                updateCenter.name,
                updateCenter.currentOccupation,
                updateCenter.maxCapacity
            )
        }

        return updateCenter
    }

}