package uk.ac.ebi.spot.gwas.rest.api.service;

import uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim.GeneSolrDto;

public interface GeneService {

    GeneSolrDto getGeneByName(String geneName);
}
