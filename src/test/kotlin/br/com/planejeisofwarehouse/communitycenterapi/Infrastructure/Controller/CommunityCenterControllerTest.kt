package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Controller

import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.CreateCommunityCenterRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.ExchangeResourcesRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.ResourceRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.UpdateCommunityCenterOccupationRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.Enum.ResourceType
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.CommunityCenter
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.Resource
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Repository.CommunityCenterRepository
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Repository.ExchangeHistoryRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("Testes de Integração - CommunityCenterController")
class CommunityCenterControllerTest
{
    @Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @Autowired
    private lateinit var repository: CommunityCenterRepository
    @Autowired
    private lateinit var exchangeHistoryRepository: ExchangeHistoryRepository
    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
        repository.deleteAll()
        exchangeHistoryRepository.deleteAll()
    }

    @Nested
    @DisplayName("GET /api/v1/community-centers/")
    inner class GetAllCommunityCentersTests {

        @Test
        @DisplayName("Deve retornar lista vazia quando não há centros cadastrados")
        fun `should return empty list when no centers exist`() {
            mockMvc.perform(get("/api/v1/community-centers/"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(0))
        }

        @Test
        @DisplayName("Deve retornar lista com centros quando existem centros cadastrados")
        fun `should return list of centers when centers exist`() {
            val centerOne = createTestCommunityCenter("Centro um")
            val centerTwo = createTestCommunityCenter("Centro dois")
            repository.saveAll(listOf(centerOne, centerTwo))

            mockMvc.perform(get("/api/v1/community-centers/"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/community-centers/{id}")
    inner class GetCommunityCenterByIdTests {

        @Test
        @DisplayName("Deve retornar centro quando ID válido é fornecido")
        fun `should return center when valid ID is provided`() {
            val center = createTestCommunityCenter("Centro Teste")
            val savedCenter = repository.save(center)

            val uri = "/api/v1/community-centers/${savedCenter.id}"
            mockMvc.perform(get(uri))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        }

        @Test
        @DisplayName("Deve retornar 400 quando ID inválido é fornecido")
        fun `should return 400 when invalid ID is provided`() {
            mockMvc.perform(get("/api/v1/community-centers/invalid-uuid"))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Deve retornar 404 quando centro não existe")
        fun `should return 404 when center does not exist`() {
            val nonExistentId = UUID.randomUUID()
            mockMvc.perform(get("/api/v1/community-centers/$nonExistentId"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/community-centers/")
    inner class CreateCommunityCenterTests {

        @Test
        @DisplayName("Deve criar centro com sucesso quando dados válidos são fornecidos")
        fun `should create center successfully when valid data is provided`() {
            val request = CreateCommunityCenterRequest(
                name = "Centro Novo",
                address = "Rua Nova, 123",
                latitude = -7.1195,
                longitude = -34.8450,
                maxCapacity = 200,
                currentOccupation = 0,
                resources = listOf(
                    ResourceRequest(ResourceType.MEDICO, 5), // Usando ResourceType direto no DTO
                    ResourceRequest(ResourceType.VOLUNTARIO, 10)
                )
            )

            mockMvc.perform(
                post("/api/v1/community-centers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Centro Novo"))
                .andExpect(jsonPath("$.address").value("Rua Nova, 123"))
                .andExpect(jsonPath("$.maxCapacity").value(200))
                .andExpect(jsonPath("$.currentOccupation").value(0))
                .andExpect(jsonPath("$.resources").isArray)
                .andExpect(jsonPath("$.resources.length()").value(2))
                .andExpect(jsonPath("$.resources[0].type").value(ResourceType.MEDICO.name)) // Ajuste aqui
                .andExpect(jsonPath("$.resources[0].quantity").value(5))
                .andExpect(jsonPath("$.resources[1].type").value(ResourceType.VOLUNTARIO.name)) // Ajuste aqui
                .andExpect(jsonPath("$.resources[1].quantity").value(10))
        }

        @Test
        @DisplayName("Deve retornar 400 quando nome está em branco")
        fun `should return 400 when name is blank`() {
            val request = CreateCommunityCenterRequest(
                name = "",
                address = "Rua Teste",
                latitude = -7.1195,
                longitude = -34.8450,
                maxCapacity = 100,
                currentOccupation = 0
            )
            mockMvc.perform(
                post("/api/v1/community-centers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Deve retornar 400 quando ocupacao excede capacidade maxima")
        fun `should return 400 when occupation exceeds max capacity`() {
            val request = CreateCommunityCenterRequest(
                name = "Centro Teste",
                address = "Rua Teste",
                latitude = -7.1195,
                longitude = -34.8450,
                maxCapacity = 50,
                currentOccupation = 100
            )

            mockMvc.perform(
                post("/api/v1/community-centers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Deve retornar 400 quando capacidade maxima é menor que 1")
        fun `should return 400 when max capacity is less than 1`() {
            val request = CreateCommunityCenterRequest(
                name = "Centro Teste",
                address = "Rua Teste",
                latitude = -7.1195,
                longitude = -34.8450,
                maxCapacity = 0,
                currentOccupation = 0
            )

            mockMvc.perform(
                post("/api/v1/community-centers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/community-centers/{id}/occupation")
    inner class UpdateCommunityCenterOccupationTests {

        @Test
        @DisplayName("Deve atualizar ocupacao com sucesso")
        fun `should update occupation successfully`() {
            val center = createTestCommunityCenter("Centro Teste")
            val savedCenter = repository.save(center)
            val request = UpdateCommunityCenterOccupationRequest(currentOccupation = 75)

            mockMvc.perform(
                patch("/api/v1/community-centers/${savedCenter.id}/occupation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("Deve retornar 400 quando nova ocupacao excede capacidade maxima")
        fun `should return 400 when new occupation exceeds max capacity`() {
            val center = createTestCommunityCenter("Centro Teste", maxCapacity = 100)
            val savedCenter = repository.save(center)
            val request = UpdateCommunityCenterOccupationRequest(currentOccupation = 150)

            mockMvc.perform(
                patch("/api/v1/community-centers/${savedCenter.id}/occupation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/community-centers/exchange")
    inner class ExchangeResourcesTests {

        @Test
        @DisplayName("Deve realizar intercambio com sucesso quando pontos sao iguais")
        fun `should exchange resources successfully when points are equal`() {
            val centerOne = createTestCommunityCenter(
                "Centro um",
                resources = mutableListOf(
                    Resource(ResourceType.MEDICO, 10),
                    Resource(ResourceType.VOLUNTARIO, 15)
                )
            )
            val centerTwo = createTestCommunityCenter(
                "Centro dois",
                resources = mutableListOf(
                    Resource(ResourceType.SUPRIMENTOS_MEDICOS, 2),
                    Resource(ResourceType.CESTA_BASICA, 10)
                )
            )

            val savedCenterOne = repository.save(centerOne)
            val savedCenterTwo = repository.save(centerTwo)

            val request = ExchangeResourcesRequest(
                centerOneId = savedCenterOne.id.toString(),
                centerTwoId = savedCenterTwo.id.toString(),
                resourcesOfferedByCenterOne = listOf(ResourceRequest(ResourceType.MEDICO, 2)), // 8 pontos (exemplo)
                resourcesOfferedByCenterTwo = listOf(ResourceRequest(ResourceType.CESTA_BASICA, 4)) // 8 pontos (exemplo)
            )

            mockMvc.perform(
                post("/api/v1/community-centers/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.centerOneId").value(savedCenterOne.id.toString()))
                .andExpect(jsonPath("$.centerTwoId").value(savedCenterTwo.id.toString()))
                .andExpect(jsonPath("$.pointsCenterOne").value(8)) // Verifique se essa lógica de pontos está no seu serviço
                .andExpect(jsonPath("$.pointsCenterTwo").value(8)) // Verifique se essa lógica de pontos está no seu serviço
                .andExpect(jsonPath("$.isHighOccupancyExemptionApplied").value(false))
                .andExpect(jsonPath("$.pointsExchanged").value(8)) // Total de pontos trocados (se ambos deram 8, total é 8)

            val updatedCenterOne = repository.findById(savedCenterOne.id!!).orElse(null)
            val updatedCenterTwo = repository.findById(savedCenterTwo.id!!).orElse(null)

            assertNotNull(updatedCenterOne)
            assertNotNull(updatedCenterTwo)

            assertTrue(updatedCenterOne.resources.any { it.type == ResourceType.MEDICO && it.quantity == 8 })
            assertTrue(updatedCenterOne.resources.any { it.type == ResourceType.VOLUNTARIO && it.quantity == 15 })
            assertTrue(updatedCenterOne.resources.any { it.type == ResourceType.CESTA_BASICA && it.quantity == 4 })

            assertTrue(updatedCenterTwo.resources.any { it.type == ResourceType.SUPRIMENTOS_MEDICOS && it.quantity == 2 })
            assertTrue(updatedCenterTwo.resources.any { it.type == ResourceType.CESTA_BASICA && it.quantity == 6 })
            assertTrue(updatedCenterTwo.resources.any { it.type == ResourceType.MEDICO && it.quantity == 2 })
        }

        @Test
        @DisplayName("Deve realizar intercambio com alta ocupacao")
        fun `should exchange resources with high occupancy exemption`() {
            val highOccupancyCenter = createTestCommunityCenter(
                "Centro Alta Ocupação",
                maxCapacity = 100,
                currentOccupation = 95, // 95% de ocupação
                resources = mutableListOf(Resource(ResourceType.CESTA_BASICA, 10))
            )

            val normalCenter = createTestCommunityCenter(
                "Centro Normal",
                maxCapacity = 100,
                currentOccupation = 50,
                resources = mutableListOf(Resource(ResourceType.MEDICO, 5))
            )

            val savedHighOccupancy = repository.save(highOccupancyCenter)
            val savedNormal = repository.save(normalCenter)

            val request = ExchangeResourcesRequest(
                centerOneId = savedHighOccupancy.id.toString(),
                centerTwoId = savedNormal.id.toString(),
                resourcesOfferedByCenterOne = listOf(ResourceRequest(ResourceType.CESTA_BASICA, 2)), // 4 pontos
                resourcesOfferedByCenterTwo = listOf(ResourceRequest(ResourceType.MEDICO, 2)) // 8 pontos
            )

            mockMvc.perform(
                post("/api/v1/community-centers/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.isHighOccupancyExemptionApplied").value(true))
                .andExpect(jsonPath("$.pointsCenterOne").value(4))
                .andExpect(jsonPath("$.pointsCenterTwo").value(8))
                .andExpect(jsonPath("$.pointsExchanged").value(8))

            // VERIFICAÇÃO PÓS-TROCA DOS RECURSOS NOS CENTROS
            val updatedHighOccupancy = repository.findById(savedHighOccupancy.id!!).orElse(null)
            val updatedNormal = repository.findById(savedNormal.id!!).orElse(null)

            assertNotNull(updatedHighOccupancy)
            assertNotNull(updatedNormal)

            // Centro Alta Ocupação (oferta CESTA_BASICA 2, recebe MEDICO 2)
            // Antes: CESTA_BASICA 10
            // Depois esperado: CESTA_BASICA 8, MEDICO 2
            assertTrue(updatedHighOccupancy.resources.any { it.type == ResourceType.CESTA_BASICA && it.quantity == 8 })
            assertTrue(updatedHighOccupancy.resources.any { it.type == ResourceType.MEDICO && it.quantity == 2 })

            // Centro Normal (oferta MEDICO 2, recebe CESTA_BASICA 2)
            // Antes: MEDICO 5
            // Depois esperado: MEDICO 3, CESTA_BASICA 2
            assertTrue(updatedNormal.resources.any { it.type == ResourceType.MEDICO && it.quantity == 3 })
            assertTrue(updatedNormal.resources.any { it.type == ResourceType.CESTA_BASICA && it.quantity == 2 })
        }


        @Test
        @DisplayName("Deve retornar 400 quando centros sao iguais")
        fun `should return 400 when centers are the same`() {
            val center = createTestCommunityCenter("Centro Teste")
            val savedCenter = repository.save(center)

            val request = ExchangeResourcesRequest(
                centerOneId = savedCenter.id.toString(),
                centerTwoId = savedCenter.id.toString(),
                resourcesOfferedByCenterOne = listOf(ResourceRequest(ResourceType.MEDICO, 1)),
                resourcesOfferedByCenterTwo = listOf(ResourceRequest(ResourceType.VOLUNTARIO, 1))
            )

            mockMvc.perform(
                post("/api/v1/community-centers/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Deve retornar 404 quando um dos centros nao existe")
        fun `should return 404 when one of the centers does not exist`() {
            val centerOne = createTestCommunityCenter("Centro um")
            val savedCenterOne = repository.save(centerOne)
            val nonExistentId = UUID.randomUUID()

            val request = ExchangeResourcesRequest(
                centerOneId = savedCenterOne.id.toString(),
                centerTwoId = nonExistentId.toString(),
                resourcesOfferedByCenterOne = listOf(ResourceRequest(ResourceType.MEDICO, 1)),
                resourcesOfferedByCenterTwo = listOf(ResourceRequest(ResourceType.VOLUNTARIO, 1))
            )

            mockMvc.perform(
                post("/api/v1/community-centers/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("Deve retornar 400 quando centro nao tem recursos suficientes")
        fun `should return 400 when center does not have sufficient resources`() {
            val centerOne = createTestCommunityCenter(
                "Centro um",
                resources = mutableListOf(Resource(ResourceType.MEDICO, 1))
            )
            val centerTwo = createTestCommunityCenter(
                "Centro dois",
                resources = mutableListOf(Resource(ResourceType.VOLUNTARIO, 5))
            )

            val savedCenterOne = repository.save(centerOne)
            val savedCenterTwo = repository.save(centerTwo)

            val request = ExchangeResourcesRequest(
                centerOneId = savedCenterOne.id.toString(),
                centerTwoId = savedCenterTwo.id.toString(),
                resourcesOfferedByCenterOne = listOf(ResourceRequest(ResourceType.MEDICO, 5)), // Tentando oferecer 5, mas só tem 1
                resourcesOfferedByCenterTwo = listOf(ResourceRequest(ResourceType.VOLUNTARIO, 1))
            )

            mockMvc.perform(
                post("/api/v1/community-centers/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Deve retornar 400 quando pontos sao diferentes sem alta ocupacao")
        fun `should return 400 when points are different without high occupancy`() {
            val centerOne = createTestCommunityCenter(
                "Centro um",
                maxCapacity = 100,
                currentOccupation = 50,
                resources = mutableListOf(Resource(ResourceType.MEDICO, 5))
            )
            val centerTwo = createTestCommunityCenter(
                "Centro dois",
                maxCapacity = 100,
                currentOccupation = 50,
                resources = mutableListOf(Resource(ResourceType.VOLUNTARIO, 5))
            )

            val savedCenterOne = repository.save(centerOne)
            val savedCenterTwo = repository.save(centerTwo)

            val request = ExchangeResourcesRequest(
                centerOneId = savedCenterOne.id.toString(),
                centerTwoId = savedCenterTwo.id.toString(),
                resourcesOfferedByCenterOne = listOf(ResourceRequest(ResourceType.MEDICO, 2)),
                resourcesOfferedByCenterTwo = listOf(ResourceRequest(ResourceType.VOLUNTARIO, 1))
            )

            mockMvc.perform(
                post("/api/v1/community-centers/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    private fun createTestCommunityCenter(
        name: String,
        maxCapacity: Int = 100,
        currentOccupation: Int = 50,
        resources: MutableList<Resource> = mutableListOf(Resource(ResourceType.CESTA_BASICA, 10)) // Default para ter algum recurso
    ): CommunityCenter {
        return CommunityCenter(
            name = name,
            address = "Endereço Teste",
            latitude = -7.1195,
            longitude = -34.8450,
            maxCapacity = maxCapacity,
            currentOccupation = currentOccupation,
            resources = resources,
            createdAt = LocalDateTime.now()
        )
    }
}

data class ResourceRequest(
    val type: ResourceType,
    val quantity: Int
)