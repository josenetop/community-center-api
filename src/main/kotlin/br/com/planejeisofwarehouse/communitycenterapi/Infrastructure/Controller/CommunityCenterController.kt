package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Controller

import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.CreateCommunityCenterRequest
import br.com.planejeisofwarehouse.communitycenterapi.Application.DTO.UpdateCommunityCenterOccupationRequest
import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.CommunityCenter
import br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Service.CommunityCenterService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/community-centers")
@Tag(name = "Community Centers", description = "Operações relacionadas a centros comunitários")
class CommunityCenterController (
    private val service: CommunityCenterService
) {

    @Operation(summary = "Lista todos os centros comunitários", description = "Retorna uma lista de todos os centros comunitários registrados no sistema.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de centros comunitários retornada com sucesso"),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    ])
    @GetMapping("/")
    fun getAllCommunityCenters(): ResponseEntity<List<CommunityCenter>> {
        val centers = service.findAll()
        println(centers)
        return ResponseEntity.ok(centers)
    }

    @Operation(summary = "Busca um centro comunitário por ID")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Centro encontrado",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CommunityCenter::class))]),
        ApiResponse(responseCode = "404", description = "Centro não encontrado"),
        ApiResponse(responseCode = "400", description = "ID inválido"),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    ])
    @GetMapping("/{id}")
    fun getCommunityCenterById(@PathVariable id: String): ResponseEntity<out Any?> {
        try {
            val uuid = UUID.fromString(id)
            val center = service.findById(uuid)
            return ResponseEntity.ok(center)
        } catch (e: Exception) {
            throw e
        }
    }

    @Operation(summary = "Cria um novo centro comunitário")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Centro criado com sucesso",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CommunityCenter::class))]),
        ApiResponse(responseCode = "400", description = "Dados inválidos"),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    ])
    @PostMapping("/")
    fun createCommunityCenter(
        @RequestBody @Valid
        request: CreateCommunityCenterRequest
    ): ResponseEntity<CommunityCenter> {
        val createdCommunityCenter = service.createCommunityCenter(request)
        return ResponseEntity(createdCommunityCenter, HttpStatus.CREATED)
    }

    @Operation(summary = "Atualiza a ocupação de um centro comunitário")
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
        val uuid = UUID.fromString(id)
        val updateCenter = service.updateCommunityCenterOccupation(uuid, request)
        return ResponseEntity.ok(updateCenter)
    }
}