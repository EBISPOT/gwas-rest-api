package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.GenomicContext;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.repository.GenomicContextRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.GenomicContextService;
import uk.ac.ebi.spot.gwas.rest.dto.GenomicContextParam;

import java.util.List;
import java.util.Optional;

@Service
public class GenomicContextServiceImpl implements GenomicContextService {

    GenomicContextRepository genomicContextRepository;

    @Autowired
    public GenomicContextServiceImpl(GenomicContextRepository genomicContextRepository) {
        this.genomicContextRepository = genomicContextRepository;
    }

    public List<GenomicContext> findByRsid(String rsid, String sortParam, String direction) {
        List<GenomicContext> genomicContexts =  null;
        if(sortParam != null ){
            if(direction != null ){
                if(sortParam.equals(GenomicContextParam.distance.name())) {
                    genomicContexts =  direction.equals("asc")  ? genomicContextRepository.findBySnpStudiesHousekeepingIsPublishedAndSnpStudiesHousekeepingCatalogPublishDateIsNotNullAndSnpRsIdOrderByDistance(true, rsid) :
                            genomicContextRepository.findBySnpStudiesHousekeepingIsPublishedAndSnpStudiesHousekeepingCatalogPublishDateIsNotNullAndSnpRsIdOrderByDistanceDesc(true, rsid);
                }
            } else {
                genomicContexts =  genomicContextRepository.findBySnpStudiesHousekeepingIsPublishedAndSnpStudiesHousekeepingCatalogPublishDateIsNotNullAndSnpRsId(true, rsid);
            }
        } else {
            genomicContexts =  genomicContextRepository.findBySnpStudiesHousekeepingIsPublishedAndSnpStudiesHousekeepingCatalogPublishDateIsNotNullAndSnpRsId(true, rsid);
        }

        if (genomicContexts.isEmpty()) {
            throw new EntityNotFoundException(EntityType.GENOMIC_CONTEXT, "rs_id", rsid);
        }
        return genomicContexts;
    }

    public Optional<GenomicContext> findByGenomicContextId(String genomicContextId) {
        return genomicContextRepository.findById(Long.valueOf(genomicContextId));
    }

}
