package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Repository

import br.com.planejeisofwarehouse.communitycenterapi.Domain.Model.ExchangeHistory
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ExchangeHistoryRepository: MongoRepository<ExchangeHistory, String> {
}