package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Repository

import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.CommunityCenter
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CommunityCenterRepository: MongoRepository<CommunityCenter, UUID> {
}