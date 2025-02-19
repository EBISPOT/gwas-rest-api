package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.UnpublishedAncestry;
import uk.ac.ebi.spot.gwas.model.UnpublishedStudy;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.repository.UnpublishedAncestryRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.UnpublishedAncestryService;
import uk.ac.ebi.spot.gwas.rest.api.service.UnpublishedStudyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UnpublishedAncestryServiceImpl implements UnpublishedAncestryService {

    @Autowired
    UnpublishedStudyService unpublishedStudyService;

    @Autowired
    UnpublishedAncestryRepository unpublishedAncestryRepository;

    public List<UnpublishedAncestry> getAllUnpublishedAncestry(String accessionId) {
        UnpublishedStudy unpublishedStudy = unpublishedStudyService.findByAccession(accessionId)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.UNPUBLISHED_STUDY, "Accession Id", accessionId));
        return new ArrayList<>(unpublishedStudy.getAncestries());
    }

    public Optional<UnpublishedAncestry> getAncestry(Long id) {
        return unpublishedAncestryRepository.findById(id);
    }
}
