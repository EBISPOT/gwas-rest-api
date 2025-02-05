package uk.ac.ebi.spot.gwas.rest.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.UnknownHostException;

@SpringBootApplication(scanBasePackages = "uk.ac.ebi.spot.gwas")
@EnableJpaRepositories("uk.ac.ebi.spot.gwas.rest.api.repository")
@EntityScan("uk.ac.ebi.spot.gwas.model")
@Slf4j
@OpenAPIDefinition(info = @Info(title = "Rest API V2", version = "2.0", description = "Refactored Rest API"))
public class RestApiApplication {

    public static void main(String[] args) {
        log.info("Inside GWAS Rest API V2 Application");
        try {
            SpringApplication.run(RestApiApplication.class, args);
        } catch (Exception e) {
            log.error("Exception occurred in main class {}", e.getMessage(), e);
        }
    }
}
