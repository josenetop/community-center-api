package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Service

import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.BulkCreateCommunityCenterRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.ExchangeResourcesRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.ResourceRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.Enum.ResourceType
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.CenterNotFoundException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.CommunityCenter
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.ExchangeHistory
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.Resource
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Repository.CommunityCenterRepository
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Repository.ExchangeHistoryRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.`mock`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommunityCenterServiceTest() {
    @Mock
    private lateinit var respository: CommunityCenterRepository
    @Mock
    private lateinit var exchangeHistoryRepository: ExchangeHistoryRepository
    @Mock
    private lateinit var notifierService: NotifierService
    private lateinit var communityCenterService: CommunityCenterService
    @Captor
    private lateinit var communityCenterCaptor: ArgumentCaptor<CommunityCenter>

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)

        communityCenterService = CommunityCenterService(
            respository,
            notifierService,
            exchangeHistoryRepository
        )
    }

    @Test
    @DisplayName("Deve realizar intercâmbio com pontos iguais com sucesso")
    fun `Realiza intercâmbio com pontos iguais com sucesso`() {
        val centerOneId = UUID.randomUUID()
        val centerTwoId = UUID.randomUUID()

        val centerOneInitialResources = mutableListOf(
            Resource(ResourceType.CESTA_BASICA, 10), // 2 pontos * 10 = 20
            Resource(ResourceType.SUPRIMENTOS_MEDICOS, 2) // 7 pontos * 2 = 14
        )
        val centerTwoInitialResources = mutableListOf(
            Resource(ResourceType.VOLUNTARIO, 5), // 3 pontos * 5 = 15
            Resource(ResourceType.CESTA_BASICA, 2) // 2 pontos * 2 = 4
        )

        val centerOne = CommunityCenter(
            id = centerOneId, name = "Centro A", address = "Rua A", latitude = 1.0, longitude = 1.0,
            maxCapacity = 100, currentOccupation = 50, resources = centerOneInitialResources
        )
        val centerTwo = CommunityCenter(
            id = centerTwoId, name = "Centro B", address = "Rua B", latitude = 2.0, longitude = 2.0,
            maxCapacity = 100, currentOccupation = 50, resources = centerTwoInitialResources
        )

        val offeredByCenterOne = listOf(ResourceRequest(ResourceType.CESTA_BASICA, 5))
        val offeredByCenterTwo = listOf(
            ResourceRequest(ResourceType.VOLUNTARIO, 2),
            ResourceRequest(ResourceType.CESTA_BASICA, 2)
        )

        val request = ExchangeResourcesRequest(
            centerOneId = centerOneId.toString(),
            centerTwoId = centerTwoId.toString(),
            resourcesOfferedByCenterOne = offeredByCenterOne,
            resourcesOfferedByCenterTwo = offeredByCenterTwo
        )

        `when`(respository.findById(centerOneId)).thenReturn(Optional.of(centerOne.copy()))
        `when`(respository.findById(centerTwoId)).thenReturn(Optional.of(centerTwo.copy()))
        `when`(respository.save(any<CommunityCenter>())).thenAnswer { invocation ->
            invocation.getArgument(0) // Retorna o próprio objeto que foi passado para save
        }
        `when`(exchangeHistoryRepository.save(any<ExchangeHistory>())).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        val result = communityCenterService.exchangeResources(request)

        assertNotNull(result)
        assertEquals(centerOneId, result.centerOneId)
        assertEquals(centerTwoId, result.centerTwoId)
        assertFalse(result.isHighOccupancyExemptionApplied)

        verify(respository, times(1)).findById(centerOneId)
        verify(respository, times(1)).findById(centerTwoId)
        verify(respository, times(2)).save(communityCenterCaptor.capture())
        verify(exchangeHistoryRepository, times(1)).save(any<ExchangeHistory>())
        verify(notifierService, never()).sendExchangeHighOccupancyExemptionNotification(any(),any(),any(),any(),any())

        val savedCenters = communityCenterCaptor.allValues
        val savedCenterOne = savedCenters.first { it.id == centerOneId }
        val savedCenterTwo = savedCenters.first { it.id == centerTwoId }

        // O centro 1 tinha (10 CESTA BASICA, 2 SUPRIMENTO MEDICO)
        // Cedeu 5 CESTA BASICA, Recebeu (2 VOLUNTARIO, 2 CESTA BASICA)
        // CESTA BASICA: 10 - 5 + 2 = 7
        // SUPRIMENTO MEDICO: 2 (não alterado)
        // VOLUNTARIO: 2 (novo)
        assertTrue(savedCenterOne.resources.any { it.type == ResourceType.CESTA_BASICA && it.quantity == 7 })
        assertTrue(savedCenterOne.resources.any { it.type == ResourceType.SUPRIMENTOS_MEDICOS && it.quantity == 2 })
        assertTrue(savedCenterOne.resources.any { it.type == ResourceType.VOLUNTARIO && it.quantity == 2 })

        // O centro 2 tinha (5 VOLUNTARIO, 2 CESTA BASICA)
        // Cedeu (2 VOLUNTARIO, 2 CESTA BASICA), Recebeu 5 CESTA BASICA
        // VOLUNTARIO: 5 - 2 = 3
        // CESTA BASICA: 2 - 2 + 5 = 5
        assertTrue(savedCenterTwo.resources.any { it.type == ResourceType.VOLUNTARIO && it.quantity == 3 })
        assertTrue(savedCenterTwo.resources.any { it.type == ResourceType.CESTA_BASICA && it.quantity == 5 })
    }

    @Test
    @DisplayName("Deve lançar CenterNotFoundException se Centro um não for encontrado")
    fun `Lança CenterNotFoundException se Centro um não for encontrado`() {
        val centerOneId = UUID.randomUUID()
        val centerTwoId = UUID.randomUUID()

        val request = ExchangeResourcesRequest(
            centerOneId = centerOneId.toString(),
            centerTwoId = centerTwoId.toString(),
            resourcesOfferedByCenterOne = emptyList(),
            resourcesOfferedByCenterTwo = emptyList()
        )

        `when`(respository.findById(centerOneId)).thenReturn(Optional.empty())
        `when`(respository.findById(centerTwoId)).thenReturn(Optional.of(mock(CommunityCenter::class.java)))

        val exception = assertThrows<CenterNotFoundException> {
            communityCenterService.exchangeResources(request)
        }

        verify(respository, never()).save(any())
        verify(exchangeHistoryRepository, never()).save(any())
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se o Centro não tiver recursos")
    fun `Lança IllegalArgumentException se o Centro não tiver recursos`() {
        val centerOneId = UUID.randomUUID()
        val centerTwoId = UUID.randomUUID()

        val centerOne = CommunityCenter(
            id = centerOneId, name = "Centro A", address = "Rua A", latitude = 1.0, longitude = 1.0,
            maxCapacity = 100, currentOccupation = 50, resources = mutableListOf(Resource(ResourceType.CESTA_BASICA, 2))
        )
        val centerTwo = CommunityCenter(
            id = centerTwoId, name = "Centro B", address = "Rua B", latitude = 2.0, longitude = 2.0,
            maxCapacity = 100, currentOccupation = 50, resources = mutableListOf(Resource(ResourceType.MEDICO, 5))
        )

        val offeredByCenterOne = listOf(ResourceRequest(ResourceType.CESTA_BASICA, 0))
        val offeredByCenterTwo = listOf(ResourceRequest(ResourceType.MEDICO, 0))

        val request = ExchangeResourcesRequest(
            centerOneId = centerOneId.toString(),
            centerTwoId = centerTwoId.toString(),
            resourcesOfferedByCenterOne = offeredByCenterOne,
            resourcesOfferedByCenterTwo = offeredByCenterTwo
        )

        `when`(respository.findById(centerOneId)).thenReturn(Optional.of(centerOne))
        `when`(respository.findById(centerTwoId)).thenReturn(Optional.of(centerTwo))

        val exception = assertThrows<IllegalArgumentException> {
            communityCenterService.exchangeResources(request)
        }
        assertEquals("O Centro não possui recursos suficientes para o intercâmbio.", exception.message)

        verify(respository, never()).save(any())
        verify(exchangeHistoryRepository, never()).save(any())
    }

    @Test
    @DisplayName("Deve realizar intercâmbio com exceção de alta ocupação para Centro um com sucesso")
    fun `Realiza intercâmbio com exceção de alta ocupação para Centro um com sucesso`() {
        val centerOneId = UUID.randomUUID()
        val centerTwoId = UUID.randomUUID()

        val centerOneInitialResources = mutableListOf(Resource(ResourceType.CESTA_BASICA, 10))
        val centerTwoInitialResources = mutableListOf(Resource(ResourceType.MEDICO, 5))

        val centerOne = CommunityCenter(
            id = centerOneId, name = "Centro A Ocupado", address = "Rua A", latitude = 1.0, longitude = 1.0,
            maxCapacity = 100, currentOccupation = 95,
            resources = centerOneInitialResources
        )
        val centerTwo = CommunityCenter(
            id = centerTwoId, name = "Centro B Normal", address = "Rua B", latitude = 2.0, longitude = 2.0,
            maxCapacity = 100, currentOccupation = 50,
            resources = centerTwoInitialResources
        )

        val offeredByCenterOne = listOf(ResourceRequest(ResourceType.CESTA_BASICA, 1)) // 2 pontos
        val offeredByCenterTwo = listOf(ResourceRequest(ResourceType.MEDICO, 1)) // 4 pontos

        val request = ExchangeResourcesRequest(
            centerOneId = centerOneId.toString(),
            centerTwoId = centerTwoId.toString(),
            resourcesOfferedByCenterOne = offeredByCenterOne,
            resourcesOfferedByCenterTwo = offeredByCenterTwo
        )

        `when`(respository.findById(centerOneId)).thenReturn(Optional.of(centerOne.copy()))
        `when`(respository.findById(centerTwoId)).thenReturn(Optional.of(centerTwo.copy()))
        `when`(respository.save(any<CommunityCenter>())).thenAnswer { invocation ->
            invocation.getArgument(0)
        }
        `when`(exchangeHistoryRepository.save(any<ExchangeHistory>())).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        val result = communityCenterService.exchangeResources(request)

        assertNotNull(result)
        assertTrue(result.isHighOccupancyExemptionApplied)
        assertEquals(2, result.pointsCenterOne)
        assertEquals(4, result.pointsCenterTwo)

        verify(respository, times(2)).save(communityCenterCaptor.capture())
        verify(exchangeHistoryRepository, times(1)).save(any<ExchangeHistory>())
        verify(notifierService, times(1)).sendExchangeHighOccupancyExemptionNotification(
            any(),
            eq(centerOne.name),
            eq(2),
            eq(4),
            eq(centerTwo.name),
        )

        val savedCenters = communityCenterCaptor.allValues
        val savedCenterOne = savedCenters.first { it.id == centerOneId }
        val savedCenterTwo = savedCenters.first { it.id == centerTwoId }

        assertTrue(savedCenterOne.resources.any { it.type == ResourceType.CESTA_BASICA && it.quantity == 10 - 1 + 1 })
        assertTrue(savedCenterOne.resources.any { it.type == ResourceType.MEDICO && it.quantity == 1 })
        assertTrue(savedCenterTwo.resources.any { it.type == ResourceType.MEDICO && it.quantity == 5 - 1 + 1 })
        assertTrue(savedCenterTwo.resources.any { it.type == ResourceType.CESTA_BASICA && it.quantity == 1 })
    }

    @Test
    @DisplayName("Deve retornar todos os centros comunitários")
    fun `Retorna todos os centros comunitários`() {
        val centers = listOf(
            CommunityCenter(name = "C1", address = "A1", latitude = 1.0, longitude = 1.0, maxCapacity = 100, currentOccupation = 50),
            CommunityCenter(name = "C2", address = "A2", latitude = 2.0, longitude = 2.0, maxCapacity = 200, currentOccupation = 100)
        )
        `when`(respository.findAll()).thenReturn(centers)

        val result = communityCenterService.findAll()

        assertEquals(2, result.size)
        assertEquals("C1", result[0]?.name ?: "")
        assertEquals("C2", result[1]?.name ?: "")
        verify(respository, times(1)).findAll()
    }

    @Test
    @DisplayName("Deve retornar centro comunitário por ID")
    fun `Retorna centro comunitário por ID`() {
        val centerId = UUID.randomUUID()
        val center = CommunityCenter(id = centerId, name = "C1", address = "A1", latitude = 1.0, longitude = 1.0, maxCapacity = 100, currentOccupation = 50)
        `when`(respository.findById(centerId)).thenReturn(Optional.of(center))

        val result = communityCenterService.findById(centerId)

        assertNotNull(result)
        assertEquals(centerId, result?.id)
        verify(respository, times(1)).findById(centerId)
    }
}