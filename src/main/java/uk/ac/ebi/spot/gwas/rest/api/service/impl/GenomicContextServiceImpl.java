package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.GenomicContext;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.repository.GenomicContextRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.GenomicContextService;

import java.util.List;
import java.util.Optional;

@Service
public class GenomicContextServiceImpl implements GenomicContextService {

    GenomicContextRepository genomicContextRepository;

    @Autowired
    public GenomicContextServiceImpl(GenomicContextRepository genomicContextRepository) {
        this.genomicContextRepository = genomicContextRepository;
    }

    public List<GenomicContext> findByRsid(String rsid) {
        List<GenomicContext> genomicContexts = genomicContextRepository.findBySnpStudiesHousekeepingIsPublishedAndSnpStudiesHousekeepingCatalogPublishDateIsNotNullAndSnpRsId(true, rsid);
        if (genomicContexts.isEmpty()) {
            throw new EntityNotFoundException(EntityType.GENOMIC_CONTEXT, "rs_id", rsid);
        }
        return genomicContexts;
    }

    public Optional<GenomicContext> findByGenomicContextId(String genomicContextId) {
        return genomicContextRepository.findById(Long.valueOf(genomicContextId));
    }

}
