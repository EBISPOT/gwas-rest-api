package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.Locus;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.repository.LocusRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.LocusService;

import java.util.List;
import java.util.Optional;

@Service
public class LocusServiceImpl implements LocusService {

    LocusRepository locusRepository;

    @Autowired
    public LocusServiceImpl(LocusRepository locusRepository) {
        this.locusRepository = locusRepository;
    }

    public List<Locus> findLociByAssociationId(Long associationId) {
        List<Locus> loci = locusRepository.findByAssociationStudyHousekeepingIsPublishedAndAssociationStudyHousekeepingCatalogPublishDateIsNotNullAndAssociationId(true, associationId);
        if (loci.isEmpty()) {
            throw new EntityNotFoundException(EntityType.LOCUS, "Association Id", String.valueOf(associationId));
        }
        return loci;
    }


    public Optional<Locus> findByLocusId(Long locusId) {
        return locusRepository.findById(locusId);
    }
}
