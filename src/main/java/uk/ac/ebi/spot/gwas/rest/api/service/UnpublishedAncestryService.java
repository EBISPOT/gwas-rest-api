package uk.ac.ebi.spot.gwas.rest.api.service;

import uk.ac.ebi.spot.gwas.model.Ancestry;
import uk.ac.ebi.spot.gwas.model.UnpublishedAncestry;

import java.util.List;
import java.util.Optional;

public interface UnpublishedAncestryService {

    List<UnpublishedAncestry> getAllUnpublishedAncestry(String accessionId);

    Optional<UnpublishedAncestry> getAncestry(Long id);
}
