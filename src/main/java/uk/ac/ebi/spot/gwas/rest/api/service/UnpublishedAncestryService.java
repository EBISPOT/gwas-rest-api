package uk.ac.ebi.spot.gwas.rest.api.service;

import uk.ac.ebi.spot.gwas.model.Ancestry;
import uk.ac.ebi.spot.gwas.model.UnpublishedAncestry;

import java.util.List;

public interface UnpublishedAncestryService {

    List<UnpublishedAncestry> getAllUnpublishedAncestry(String accessionId);

    UnpublishedAncestry getAncestry(Long id);
}
