package uk.ac.ebi.spot.gwas.rest.api.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApiMetadataConfig {

    private String title;
    private String version;
    private String dataReleaseDate;
    private String apiReleaseDate;
    private String dbsnpBuild;
    private String geneBuild;
    private String efoVersion;
    private String ensemblVersion;
    private String termsOfService;
    private String commitHash;
    private License license;
    private Contact contact;
    private String apiReference;
    private String apiDocumentation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class License {
        private String name;
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private String name;
        private String email;
        private String url;
    }
}
