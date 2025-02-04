package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.SolrApiResponse;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim.GeneSolrDto;
import uk.ac.ebi.spot.gwas.rest.api.service.SlimSolrService;

@Service
@Slf4j
public class SlimSolrServiceImpl implements SlimSolrService {

    private final RestTemplate restTemplate;
    private final String slimSolrUrl = "http://ves-pg-7f:8983/solr/gwas_slim";

    public SlimSolrServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public SolrApiResponse<GeneSolrDto> fetchGeneData(String geneName) {
        String url = slimSolrUrl + "/select/?q=title:" + geneName + " AND resourcename:gene&wt=json";
        log.info("Fetching gene data from URL: {}", url);
        try {
            ResponseEntity<SolrApiResponse<GeneSolrDto>> response = restTemplate.exchange(
                    url,
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
