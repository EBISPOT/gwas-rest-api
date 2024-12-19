package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.UnpublishedAncestry;
import uk.ac.ebi.spot.gwas.model.UnpublishedStudy;
import uk.ac.ebi.spot.gwas.rest.api.repository.UnpublishedAncestryRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.UnpublishedAncestryService;
import uk.ac.ebi.spot.gwas.rest.api.service.UnpublishedStudyService;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnpublishedAncestryServiceImpl implements UnpublishedAncestryService {

    @Autowired
    UnpublishedStudyService unpublishedStudyService;

    @Autowired
    UnpublishedAncestryRepository unpublishedAncestryRepository;

    public List<UnpublishedAncestry> getAllUnpublishedAncestry(String accessionId) {
       UnpublishedStudy unpublishedStudy = unpublishedStudyService.findByAccession(accessionId);
       return new ArrayList<>(unpublishedStudy.getAncestries());
    }

    public UnpublishedAncestry getAncestry(Long id) {
        return unpublishedAncestryRepository.findById(id).orElse(null);
    }
}
