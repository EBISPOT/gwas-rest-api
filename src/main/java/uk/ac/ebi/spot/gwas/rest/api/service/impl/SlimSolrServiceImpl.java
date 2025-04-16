package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.spot.gwas.rest.api.config.RestAPIConfiguration;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.SolrApiResponse;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim.GeneSolrDto;
import uk.ac.ebi.spot.gwas.rest.api.service.SlimSolrService;

@Service
@Slf4j
public class SlimSolrServiceImpl implements SlimSolrService {

    private final RestTemplate restTemplate;
    private final RestAPIConfiguration restAPIConfiguration;

    public SlimSolrServiceImpl(RestTemplate restTemplate, RestAPIConfiguration restAPIConfiguration) {
        this.restTemplate = restTemplate;
        this.restAPIConfiguration = restAPIConfiguration;
    }

    @Override
    public SolrApiResponse<GeneSolrDto> fetchGeneData(String geneName) {
        String gwasUiUrl = restAPIConfiguration.getGwasUiUrl();
        String geneCallUrl = gwasUiUrl + "/api/search?q=title:" + geneName + " AND resourcename:gene";
        log.info("Fetching gene data from URL: {}", geneCallUrl);
        try {
            ResponseEntity<SolrApiResponse<GeneSolrDto>> response = restTemplate.exchange(
                    geneCallUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<SolrApiResponse<GeneSolrDto>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching gene data for '{}': {}", geneName, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch gene data", e);
        }
    }

    @Override
    public SolrApiResponse<GeneSolrDto> fetchGenes(Pageable pageable) {
        String gwasUiUrl = restAPIConfiguration.getGwasUiUrl();
        String gwasGenesUrl = UriComponentsBuilder.fromHttpUrl(gwasUiUrl)
                .path("/api/search")
                .queryParam("q", "resourcename:gene")
                .queryParam("page", pageable.getPageNumber() + 1) // +1 because gwas-ui api  paging starts with 1, this api starts with 0
                .queryParam("max", pageable.getPageSize())
                .toUriString();
        try {
            ResponseEntity<SolrApiResponse<GeneSolrDto>> response = restTemplate.exchange(
                    gwasGenesUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<SolrApiResponse<GeneSolrDto>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching genes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch genes", e);
        }
    }
}
