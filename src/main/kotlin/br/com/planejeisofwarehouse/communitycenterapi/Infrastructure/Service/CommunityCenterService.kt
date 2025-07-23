package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Service

import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.BulkCreateCommunityCenterRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.CreateCommunityCenterRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.ExchangeResourcesRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.UpdateCommunityCenterOccupationRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.Enum.ResourceType
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.CenterNotFoundException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.NotFoundHaveSufficientResourcesForTheExchangeException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.OccupationInvalidException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.OccupationNotFoundException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.CommunityCenter
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.ExchangeHistory
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.Resource
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Repository.CommunityCenterRepository
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Repository.ExchangeHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import kotlin.IllegalArgumentException

@Service
class CommunityCenterService(
    private val repository: CommunityCenterRepository,
    private val notifierService: NotifierService,
    private val exchangeHistoryRepository: ExchangeHistoryRepository
) {
    private val logger = LoggerFactory.getLogger(CommunityCenterService::class.java)

    fun findAll(): List<CommunityCenter?> {
        return repository.findAll()
    }

    fun findById(id: UUID): CommunityCenter {
        val center = repository.findById(id).orElseThrow{
            CenterNotFoundException("Centro comunitário não encontrado. ID: $id")
        }

        return center
    }

    fun createCommunityCenter(request: CreateCommunityCenterRequest): CommunityCenter {

        if (request.name.isEmpty()) {
            throw OccupationNotFoundException("O nome do centro não pode estar em branco")
        }

        if (request.maxCapacity != null && request.currentOccupation > request.maxCapacity)
            throw OccupationNotFoundException("A Ocupação não pode exceder a capacidade máxima")

        request.maxCapacity?.let { if (it < 1) throw OccupationNotFoundException("A Ocupação não pode ser menor que 1.") }

        val resource = request.resources.map { resourceRequest ->
            Resource(type = resourceRequest.type, quantity = resourceRequest.quantity)
        }.toMutableList()

        val newCenter = CommunityCenter(
            name = request.name,
            address = request.address,
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
            CenterNotFoundException("Centro comunitário não encontrado. ID: $id")
        }

        center.maxCapacity?.let {
            if (request.currentOccupation > it)
                throw OccupationInvalidException ("A nova ocupação (" +
                        "${request.currentOccupation}" +
                        ")) excede a capacidade máxima do centro (" +
                        "${center.name}" +
                        ")")
        }

        center.currentOccupation = request.currentOccupation
        center.updatedAt = LocalDateTime.now()

        val updateCenter = repository.save(center)

        updateCenter.maxCapacity?.let {
            if (updateCenter.currentOccupation >= it) {
                notifierService.sendCapacityAlert(
                    updateCenter.id,
                    updateCenter.name,
                    updateCenter.maxCapacity!!
                )
            } else if (updateCenter.isOccupancyHigh()) {
                notifierService.sendHighOccupancyWarning(
                    updateCenter.id,
                    updateCenter.name,
                    updateCenter.currentOccupation,
                    updateCenter.maxCapacity!!
                )
            }
        }

        return updateCenter
    }

    fun exchangeResources(request: ExchangeResourcesRequest): ExchangeHistory {
        if (request.centerOneId == request.centerTwoId) {
            throw IllegalArgumentException("Não é possível trocar recursos com o mesmo centro comunitário.")
        }

        val centerOne = findById(UUID.fromString(request.centerOneId))
        val centerTwo = findById(UUID.fromString(request.centerTwoId))

        if (centerOne == null || centerTwo == null) {
            throw CenterNotFoundException("Centro não encontrado.")
        }

        val resourcesOfferedByCenterOne = request.resourcesOfferedByCenterOne.map { resourceRequest ->
            if (resourceRequest.quantity <= 0) {
                throw IllegalArgumentException("O Centro não possui recursos suficientes para o intercâmbio.")
            }
            Resource(type = resourceRequest.type, quantity = resourceRequest.quantity)
        }

        val resourcesOfferedByCenterTwo = request.resourcesOfferedByCenterTwo.map { resourceRequest ->
            if (resourceRequest.quantity <= 0) {
                throw IllegalArgumentException("O Centro não possui recursos suficientes para o intercâmbio.")
            }
            Resource(type = resourceRequest.type, quantity = resourceRequest.quantity)
        }

        checkSufficientResources(centerOne, resourcesOfferedByCenterOne)
        checkSufficientResources(centerTwo, resourcesOfferedByCenterTwo)

        val pointsCenterOne = calculatePoints(resourcesOfferedByCenterOne)
        val pointsCenterTwo = calculatePoints(resourcesOfferedByCenterTwo)

        val isHighOccupancyExemptionApplied = isHighOccupancy(centerOne) || isHighOccupancy(centerTwo)

        if (!isHighOccupancyExemptionApplied && pointsCenterOne != pointsCenterTwo) {
            throw NotFoundHaveSufficientResourcesForTheExchangeException("A troca de recursos requer que os pontos sejam iguais, a menos que haja alta ocupação em um dos centros. Pontos Centro 1: $pointsCenterOne, Pontos Centro 2: $pointsCenterTwo")
        }

        updateCenterResources(centerOne, resourcesOfferedByCenterOne, resourcesOfferedByCenterTwo)
        updateCenterResources(centerTwo, resourcesOfferedByCenterTwo, resourcesOfferedByCenterOne)

        repository.save(centerOne)
        repository.save(centerTwo)

        val pointsExchanged = if (isHighOccupancyExemptionApplied) {
            maxOf(pointsCenterOne, pointsCenterTwo)
        } else {
            pointsCenterOne
        }

        val exchangeHistory = ExchangeHistory(
            centerOneId = centerOne.id!!,
            centerTwoId = centerTwo.id!!,
            offeredResourcesCenterOne = resourcesOfferedByCenterOne,
            receivedResourcesCenterOne = resourcesOfferedByCenterTwo,
            offeredResourcesCenterTwo = resourcesOfferedByCenterTwo,
            receivedResourcesCenterTwo = resourcesOfferedByCenterOne,
            pointsCenterOne = pointsCenterOne,
            pointsCenterTwo = pointsCenterTwo,
            isHighOccupancyExemptionApplied = isHighOccupancyExemptionApplied,
            exchangeDate = LocalDateTime.now(),
            pointsExchanged = pointsExchanged
        )

        return exchangeHistoryRepository.save(exchangeHistory)
    }

    private fun checkSufficientResources(center: CommunityCenter, offeredResources: List<Resource>) {
        offeredResources.forEach { offered ->
            val existingResource = center.resources.find { it.type == offered.type }
            if (existingResource == null || existingResource.quantity < offered.quantity) {
                throw NotFoundHaveSufficientResourcesForTheExchangeException("Centro ${center.name} não possui recursos suficientes do tipo ${offered.type.name}. Requer ${offered.quantity}, tem ${existingResource?.quantity ?: 0}.")
            }
        }
    }

    private fun updateCenterResources(
        center: CommunityCenter,
        offered: List<Resource>,
        received: List<Resource>
    ) {
        offered.forEach { offeredRes ->
            val existing = center.resources.find { it.type == offeredRes.type }
            existing?.let {
                it.quantity -= offeredRes.quantity
            }
        }

        received.forEach { receivedRes ->
            val existing = center.resources.find { it.type == receivedRes.type }
            if (existing != null) {
                existing.quantity += receivedRes.quantity
            } else {
                center.resources.add(Resource(receivedRes.type, receivedRes.quantity))
            }
        }

        center.resources.removeIf { it.quantity <= 0 }
    }

    private fun calculatePoints(resources: List<Resource>): Int {
        var totalPoints = 0
        resources.forEach { resource ->
            totalPoints += when (resource.type) {
                ResourceType.CESTA_BASICA -> 2 * resource.quantity
                ResourceType.MEDICO -> 4 * resource.quantity
                ResourceType.VOLUNTARIO -> 3 * resource.quantity
                ResourceType.SUPRIMENTOS_MEDICOS -> 2 * resource.quantity
                ResourceType.VEICULO_DE_TRANSPORTE -> 5 * resource.quantity
                else -> 0
            }
        }
        return totalPoints
    }

    private fun isHighOccupancy(center: CommunityCenter): Boolean {
        return center.maxCapacity?.let { center.currentOccupation.toDouble() / it }!! >= 0.90 // Exemplo: 90% ou mais
    }

    fun bulkCreateCommunityCenters(request: BulkCreateCommunityCenterRequest): List<CommunityCenter>
    {
        val createdCenters = mutableListOf<CommunityCenter>()
        for (centerRequest in request.centers) {
            try {
                val newCenter = createCommunityCenter(centerRequest)
                createdCenters.add(newCenter)
            } catch (e: Exception) {
                throw e
            }
        }
        return createdCenters
    }

}