package uk.ac.ebi.spot.gwas.rest.api.service;

import uk.ac.ebi.spot.gwas.model.GenomicContext;

import java.util.List;

public interface GenomicContextService {

    List<GenomicContext> findByRsid(String rsid);

    GenomicContext findByGenomicContextId(String genomicContextId);

}
