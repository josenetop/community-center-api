package br.com.planejeisofwarehouse.communitycenterapi.Infrastructure.Config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Community Center API")
                    .version("v1")
                    .description("API de Centros Comunitários")
            )
    }
}