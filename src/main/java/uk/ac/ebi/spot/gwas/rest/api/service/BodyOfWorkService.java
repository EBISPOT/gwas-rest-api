package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.model.BodyOfWork;

import java.util.Optional;

public interface BodyOfWorkService {

   Page<BodyOfWork> getBodyOfWork(String title, String firstAuthor, Pageable pageable);

   Optional<BodyOfWork> getBodyOfWork(String bowId);
}
