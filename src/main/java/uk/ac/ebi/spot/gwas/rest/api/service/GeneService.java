package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim.GeneSolrDto;

public interface GeneService {

    GeneSolrDto getGeneByName(String geneName);
    Page<GeneSolrDto> getGenes(Pageable pageable);
}
