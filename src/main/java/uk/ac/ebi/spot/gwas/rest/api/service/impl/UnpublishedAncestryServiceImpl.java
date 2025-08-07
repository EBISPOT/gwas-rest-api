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
import uk.ac.ebi.spot.gwas.rest.dto.UnpublishedAncestrySortParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class UnpublishedAncestryServiceImpl implements UnpublishedAncestryService {

    @Autowired
    UnpublishedStudyService unpublishedStudyService;

    @Autowired
    UnpublishedAncestryRepository unpublishedAncestryRepository;

    public List<UnpublishedAncestry> getAllUnpublishedAncestry(String accessionId, String sortParam , String direction) {
        UnpublishedStudy unpublishedStudy = unpublishedStudyService.findByAccession(accessionId)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.UNPUBLISHED_STUDY, "Accession Id", accessionId));

        List<UnpublishedAncestry> unpublishedAncestries = new ArrayList<>(unpublishedStudy.getAncestries());
        if(sortParam != null) {
            if(direction != null) {
                if(sortParam.equals(UnpublishedAncestrySortParam.sample_size.name())) {
                    if(direction.equals("asc")) {
                        unpublishedAncestries.sort(Comparator.comparing(UnpublishedAncestry::getSampleSize));
                    } else {
                        unpublishedAncestries.sort(Comparator.comparing(UnpublishedAncestry::getSampleSize).reversed());
                    }
                }
                if(sortParam.equals(UnpublishedAncestrySortParam.cases.name())) {
                    if(direction.equals("asc")) {
                        unpublishedAncestries.sort(Comparator.comparing(UnpublishedAncestry::getCases));
                    } else {
                        unpublishedAncestries.sort(Comparator.comparing(UnpublishedAncestry::getCases).reversed());
                    }
                }
                if(sortParam.equals(UnpublishedAncestrySortParam.controls.name())) {
                    if(direction.equals("asc")) {
                        unpublishedAncestries.sort(Comparator.comparing(UnpublishedAncestry::getControls));
                    } else {
                        unpublishedAncestries.sort(Comparator.comparing(UnpublishedAncestry::getControls).reversed());
                    }
                }
            }else {
                unpublishedAncestries.sort(Comparator.comparing(UnpublishedAncestry::getId).reversed());
            }
        }
        return unpublishedAncestries;
    }

    public Optional<UnpublishedAncestry> getAncestry(Long id) {
        return unpublishedAncestryRepository.findById(id);
    }
}
