package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class AppConfig {

    @Bean
    fun restTemplate (): RestTemplate {
        return RestTemplate()
    }
}