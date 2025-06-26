package uk.ac.ebi.spot.gwas.rest.api.service;

import uk.ac.ebi.spot.gwas.model.GenomicContext;

import java.util.List;
import java.util.Optional;

public interface GenomicContextService {

    List<GenomicContext> findByRsid(String rsid, String sortParam, String direction);

    Optional<GenomicContext> findByGenomicContextId(String genomicContextId);

}
