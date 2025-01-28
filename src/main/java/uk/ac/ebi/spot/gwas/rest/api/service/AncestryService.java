package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.model.Ancestry;

import java.util.List;

public interface AncestryService {

    List<Ancestry> getAncestriesForStudy(String accessionId);

    Ancestry getAncestry(Long ancestryId);
}
