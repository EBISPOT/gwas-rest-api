package uk.ac.ebi.spot.gwas.rest.api.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.rest.api.config.ApiMetadataConfig;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_VERSION)
public class ApiMetadataController {

    @Autowired private ApiMetadataConfig apiMetadataDto;
    @Autowired private RestTemplate restTemplate;
    @Autowired private InfoEndpoint infoEndpoint;
    private ObjectMapper objectMapper;

    @Value("${stats.url}")
    private String statUrl;

    public ApiMetadataController(ApiMetadataConfig apiMetadataDto) {
        this.apiMetadataDto = apiMetadataDto;
    }

    @GetMapping()
    public ApiMetadataConfig getVersion() {
        apiMetadataDto.setVersion(apiMetadataDto.getVersion());
        ResponseEntity<DataRelease> response = restTemplate.getForEntity(statUrl, DataRelease.class);
        DataRelease dataReleaseMetric = response.getBody();

        apiMetadataDto.setEfoVersion(dataReleaseMetric.getEfoversion());
        apiMetadataDto.setEnsemblVersion(dataReleaseMetric.getEnsemblbuild());
        apiMetadataDto.setDataReleaseDate(dataReleaseMetric.getDate());
        apiMetadataDto.setDbsnpBuild(dataReleaseMetric.getDbsnpbuild());
        apiMetadataDto.setGeneBuild(dataReleaseMetric.getGenebuild());
        apiMetadataDto.setCommitHash(extractCommitId(infoEndpoint.info()));

       return apiMetadataDto;
    }

    @SuppressWarnings("unchecked")
    private String extractCommitId(Map<String, Object> info) {
        return Optional.ofNullable(info)
                .map(i -> i.get("git"))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(git -> git.get("commit"))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(commit -> commit.get("id"))
                .map(Object::toString)
                .orElse("UNKNOWN");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataRelease {
        private String date;
        private String associations;
        private String ensemblbuild;
        private String dbsnpbuild;
        private String studies;
        private String snps;
        private String genebuild;
        private String sumstats;
        private String efoversion;
    }

    

}
