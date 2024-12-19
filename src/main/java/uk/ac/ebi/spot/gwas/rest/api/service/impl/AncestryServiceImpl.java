package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.Ancestry;
import uk.ac.ebi.spot.gwas.rest.api.repository.AncestryRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.AncestryService;

import java.util.List;

@Service
public class AncestryServiceImpl implements AncestryService {

    AncestryRepository ancestryRepository;

    @Autowired
    public AncestryServiceImpl(AncestryRepository ancestryRepository) {
        this.ancestryRepository = ancestryRepository;
    }

    public List<Ancestry> getAncestriesForStudy(String accessionId) {
        return ancestryRepository.findByStudyHousekeepingIsPublishedAndStudyHousekeepingCatalogPublishDateIsNotNullAndStudyAccessionIdEquals(true  , accessionId);
    }


   public Ancestry getAncestry(Long ancestryId) {
        return ancestryRepository.findById(ancestryId).orElse(null);
   }

}
