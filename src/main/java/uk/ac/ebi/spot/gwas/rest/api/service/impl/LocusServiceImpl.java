package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.Locus;
import uk.ac.ebi.spot.gwas.rest.api.repository.LocusRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.LocusService;

import java.util.List;

@Service
public class LocusServiceImpl implements LocusService {

    LocusRepository locusRepository;

    @Autowired
    public LocusServiceImpl(LocusRepository locusRepository) {
        this.locusRepository = locusRepository;
    }

    public List<Locus> findLociByAssociationId(Long associationId) {
        return locusRepository.findByAssociationStudyHousekeepingIsPublishedAndAssociationStudyHousekeepingCatalogPublishDateIsNotNullAndAssociationId(true, associationId);
    }


    public Locus findByLocusId(Long locusId) {
        return locusRepository.findById(locusId).orElse(null);
    }
}
