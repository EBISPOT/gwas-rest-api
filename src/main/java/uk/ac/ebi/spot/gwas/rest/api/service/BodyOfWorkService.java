package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.model.BodyOfWork;

public interface BodyOfWorkService {

   Page<BodyOfWork> getBodyOfWork(String title, String firstAuthor, Pageable pageable);

   BodyOfWork getBodyOfWork(String bowId);
}
