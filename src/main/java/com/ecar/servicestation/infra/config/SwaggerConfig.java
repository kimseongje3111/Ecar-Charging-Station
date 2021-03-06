package com.ecar.servicestation.infra.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket swaggerApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);

        return docket
                .apiInfo(swaggerInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ecar.servicestation.modules"))
                .apis(Predicates.not(RequestHandlerSelectors.basePackage("com.ecar.servicestation.modules.main.exception")))
                .paths(PathSelectors.any())
                .build()
                .useDefaultResponseMessages(false);
    }

    private ApiInfo swaggerInfo() {
        return new ApiInfoBuilder()
                .title("Spring API Documentation")
                .description("본 애플리케이션이 제공하는 서비스에 대한 Srping 서버 API 명세")
                .license("kimseongje3111")
                .licenseUrl("https://github.com/kimseongje3111/Ecar-Charging-Station")
                .version("V_1.0")
                .build();
    }

}
