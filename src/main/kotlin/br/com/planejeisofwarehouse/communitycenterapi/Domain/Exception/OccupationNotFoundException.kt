package br.com.planejeisofwarehouse.communitycenterapi.Domain.Exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class OccupationNotFoundException(message: String?) :
    ResponseStatusException(HttpStatus.NOT_FOUND, message)  {
}