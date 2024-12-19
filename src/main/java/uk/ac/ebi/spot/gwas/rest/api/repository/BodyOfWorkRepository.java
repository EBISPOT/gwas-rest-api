package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.spot.gwas.model.BodyOfWork;

import java.util.Optional;

public interface BodyOfWorkRepository extends JpaRepository<BodyOfWork, Long> {

    Page<BodyOfWork> findByTitleContainingIgnoreCaseAndFirstAuthorContainingIgnoreCase(String title, String firstAuthor, Pageable pageable);

    Page<BodyOfWork> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<BodyOfWork> findByFirstAuthorContainingIgnoreCase(String firstAuthor, Pageable pageable);

    Optional<BodyOfWork> findByPublicationId(String bowId);




}
