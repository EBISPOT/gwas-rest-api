package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.model.SingleNucleotidePolymorphism;
import uk.ac.ebi.spot.gwas.rest.dto.SearchSnpParams;

public interface SnpService {

   Page<SingleNucleotidePolymorphism> getSnps(SearchSnpParams searchSnpParams, Pageable pageable);

   SingleNucleotidePolymorphism getSnp(String rsId);
}
