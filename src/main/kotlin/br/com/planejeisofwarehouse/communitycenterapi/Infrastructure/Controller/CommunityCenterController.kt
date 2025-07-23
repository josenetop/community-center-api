package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Controller

import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.BulkCreateCommunityCenterRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.CreateCommunityCenterRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.ExchangeResourcesRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.UpdateCommunityCenterOccupationRequest
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.CenterNotFoundException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.InvalidRequestException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.NotFoundHaveSufficientResourcesForTheExchangeException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.OccupationInvalidException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception.OccupationNotFoundException
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.CommunityCenter
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.ExchangeHistory
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Event.Listener.ErrorResponse
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Service.CommunityCenterService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import java.util.UUID

@RestController
@RequestMapping("/api/v1/community-centers")
@Tag(name = "Community Centers", description = "Operações relacionadas a centros comunitários")
class CommunityCenterController(
    private val service: CommunityCenterService
) {

    @Operation(summary = "Lista todos os centros comunitários",
        description = "Retorna uma lista de todos os centros comunitários registrados no sistema.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de centros comunitários retornada com sucesso"),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    ])
    @GetMapping("/")
    fun getAllCommunityCenters(): ResponseEntity<CommunityCenter> {
        return try {
            ResponseEntity(service.findAll(), HttpStatus.OK)
        } catch (ex: CenterNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Erro interno no servidor"))
        } as ResponseEntity<CommunityCenter>
    }

    @Operation(summary = "Busca um centro comunitário por ID",
        description = "Retorna os detalhes de um centro comunitário específico com base no ID fornecido.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Centro encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CommunityCenter::class))]),
        ApiResponse(responseCode = "404", description = "Centro não encontrado"),
        ApiResponse(responseCode = "400", description = "ID inválido"),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    ])
    @GetMapping("/{id}")
    fun getCommunityCenterById(@PathVariable @Valid id: String): ResponseEntity<out Any?> {
        return try {
            val uuid = UUID.fromString(id)
            ResponseEntity(service.findById(uuid), HttpStatus.OK)
        } catch (ex: CenterNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
        } catch (ex: InvalidRequestException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Erro interno no servidor"))
        }
    }

    @Operation(summary = "Cria um novo centro comunitário",
        description = "Recebe os dados necessários e cria um novo centro comunitário no sistema.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Centro criado com sucesso",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CommunityCenter::class))]),
        ApiResponse(responseCode = "400", description = "Dados inválidos"),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    ])
    @PostMapping("/")
    fun createCommunityCenter(
        @RequestBody @Valid request: CreateCommunityCenterRequest
    ): ResponseEntity<CommunityCenter> {
        return try {
            val createdCommunityCenter = service.createCommunityCenter(request)
            ResponseEntity(createdCommunityCenter, HttpStatus.CREATED)
        } catch (ex: OccupationNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Erro interno no servidor"))
        } as ResponseEntity<CommunityCenter>
    }

    @Operation(summary = "Atualiza a ocupação de um centro comunitário",
        description = "Atualiza a ocupação de um centro comunitário existente com base no ID e nos dados fornecidos.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Centro atualizado com sucesso",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CommunityCenter::class))]),
        ApiResponse(responseCode = "404", description = "Centro não encontrado"),
        ApiResponse(responseCode = "400", description = "Dados inválidos ou ID incorreto"),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    ])
    @PatchMapping("/{id}/occupation")
    fun updateCommunityCenterOccupation(
        @PathVariable id: String,
        @RequestBody @Valid request: UpdateCommunityCenterOccupationRequest
    ): ResponseEntity<CommunityCenter> {
        return try {
            val uuid = UUID.fromString(id)
            ResponseEntity(service.updateCommunityCenterOccupation(uuid, request), HttpStatus.OK)
        } catch (ex: CenterNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message))
        } catch (ex: OccupationInvalidException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
        } catch (ex: NotFoundHaveSufficientResourcesForTheExchangeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Erro interno no servidor"))
        } as ResponseEntity<CommunityCenter>
    }

    @Operation(summary = "Realiza um intercâmbio de recursos entre dois centros comunitários",
        description = "Permite a troca de recursos entre dois centros comunitários, validando as condições necessárias.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Intercâmbio realizado com sucesso",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ExchangeHistory::class))]),
        ApiResponse(responseCode = "400", description = "Requisição inválida (IDs duplicados, recursos insuficientes, pontos incompatíveis, etc.)"),
        ApiResponse(responseCode = "404", description = "Um ou ambos os centros não encontrados"),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    ])
    @PostMapping("/exchange")
    fun exchangeResources(
        @RequestBody @Valid request: ExchangeResourcesRequest
    ): ResponseEntity<CommunityCenter> {
        return try {
            ResponseEntity(service.exchangeResources(request), HttpStatus.OK)
        } catch (ex: CenterNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message))
        } catch (ex: NotFoundHaveSufficientResourcesForTheExchangeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Erro interno no servidor"))
        } as ResponseEntity<CommunityCenter>
    }

    @Operation(summary = "Cria centros comunitários em massa")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Centros comunitários criados com sucesso"),
            ApiResponse(responseCode = "400", description = "Dados da requisição inválidos (lista vazia ou erros em centros individuais)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
        ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Lista de centros comunitários a serem criados",
        required = true,
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = BulkCreateCommunityCenterRequest::class),
                examples = [
                    ExampleObject(
                        name = "Exemplo de Massa de Dados",
                        summary = "Exemplo de criação de 3 centros comunitários",
                        value = """
                            {
                              "centers": [
                                {
                                  "name": "Centro de Reunião Central",
                                  "address": "Av. Principal, 100",
                                  "latitude": -7.119,
                                  "longitude": -34.845,
                                  "maxCapacity": 200,
                                  "currentOccupation": 15,
                                  "resources": [
                                    { "type": "VOLUNTARIO   ", "quantity": 100 },
                                    { "type": "SUPRIMENTOS_MEDICOS", "quantity": 500 }
                                  ]
                                },
                                {
                                  "name": "Ponto de Apoio Leste",
                                  "address": "Rua do Sol, 50",
                                  "latitude": -7.125,
                                  "longitude": -34.830,
                                  "maxCapacity": 80,
                                  "currentOccupation": 5,
                                  "resources": [
                                    { "type": "MEDICO", "quantity": 2 },
                                    { "type": "SUPRIMENTOS_MEDICOS", "quantity": 30 }
                                  ]
                                },
                                {
                                  "name": "Centro de Distribuição Sul",
                                  "address": "Travessa dos Rios, 20",
                                  "latitude": -7.130,
                                  "longitude": -34.850,
                                  "maxCapacity": 300,
                                  "currentOccupation": 0,
                                  "resources": []
                                }
                              ]
                            }
                        """
                    )
                ]
            )
        ]
    )
    @PostMapping("/bulk")
    fun bulkCreateCommunityCenters(
        @RequestBody @Valid request: BulkCreateCommunityCenterRequest
    ): ResponseEntity<List<CommunityCenter>> {
        val createdCenters = service.bulkCreateCommunityCenters(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCenters)
    }
}