package uk.ac.ebi.spot.gwas.rest.api.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.model.Study;
import uk.ac.ebi.spot.gwas.rest.dto.SearchStudyParams;
import uk.ac.ebi.spot.gwas.rest.projection.StudyProjection;

public interface StudyService {

    Page<Study> getStudies(Pageable pageable, SearchStudyParams searchStudyParams);

    Study getStudy(Long studyId);

    Study getStudy(String accessionId);
}
