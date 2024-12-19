package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.GenomicContext;
import uk.ac.ebi.spot.gwas.rest.api.repository.GenomicContextRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.GenomicContextService;

import java.util.List;

@Service
public class GenomicContextServiceImpl implements GenomicContextService {

    GenomicContextRepository genomicContextRepository;

    @Autowired
    public GenomicContextServiceImpl(GenomicContextRepository genomicContextRepository) {
        this.genomicContextRepository = genomicContextRepository;
    }

    public List<GenomicContext> findByRsid(String rsid) {
        return genomicContextRepository.findBySnpStudiesHousekeepingIsPublishedAndSnpStudiesHousekeepingCatalogPublishDateIsNotNullAndSnpRsId(true, rsid);
    }

    public GenomicContext findByGenomicContextId(String genomicContextId) {
        return genomicContextRepository.findById(Long.valueOf(genomicContextId)).orElse(null);
    }

}
