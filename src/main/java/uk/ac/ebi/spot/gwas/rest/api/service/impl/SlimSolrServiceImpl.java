package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
        String geneCallUrl = gwasUiUrl + "?q=title:" + geneName + " AND resourcename:gene";
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
}
