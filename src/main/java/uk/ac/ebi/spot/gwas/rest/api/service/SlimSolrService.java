package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.SolrApiResponse;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim.GeneSolrDto;

public interface SlimSolrService {
    SolrApiResponse<GeneSolrDto> fetchGeneData(String geneName);
    SolrApiResponse<GeneSolrDto> fetchGenes(Pageable pageable);
}
