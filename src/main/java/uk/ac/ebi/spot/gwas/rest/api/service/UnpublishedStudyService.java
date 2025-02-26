package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.model.UnpublishedStudy;
import uk.ac.ebi.spot.gwas.rest.dto.SearchUnpublishedStudyParams;

public interface UnpublishedStudyService {

  Page<UnpublishedStudy> getUnpublishedStudies(SearchUnpublishedStudyParams searchUnpublishedStudyParams, Pageable pageable);

  UnpublishedStudy findByAccession(String accession);

  Page<UnpublishedStudy> findByBodyOfWork(Long bowId, Pageable pageable);


}
