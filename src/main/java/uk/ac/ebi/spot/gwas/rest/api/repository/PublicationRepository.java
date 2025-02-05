package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.spot.gwas.model.Publication;

import java.util.Optional;

public interface PublicationRepository extends JpaRepository<Publication, Long> {

  Page<Publication> findByPubmedIdEquals(String pubmedId, Pageable pageable);

  Optional<Publication> findByPubmedIdEquals(String pubmedId);

  Page<Publication> findByPubmedIdEqualsAndTitleContainingIgnoreCaseAndFirstAuthorFullnameContainingIgnoreCase(String pubmedId, String title, String firstAuthor, Pageable pageable);

  Page<Publication> findByPubmedIdEqualsAndTitleContainingIgnoreCase(String pubmedId, String title, Pageable pageable);

  Page<Publication> findByPubmedIdEqualsAndFirstAuthorFullnameContainingIgnoreCase(String pubmedId, String firstAuthor, Pageable pageable);

  Page<Publication> findByTitleContainingIgnoreCaseAndFirstAuthorFullnameContainingIgnoreCase(String title, String firstAuthor, Pageable pageable);

  Page<Publication> findByTitleContainingIgnoreCase(String title, Pageable pageable);

  Page<Publication> findByFirstAuthorFullnameContainingIgnoreCase(String firstAuthor, Pageable pageable);

  Page<Publication> findByPublicationContainingIgnoreCase(String journal, Pageable pageable);

}
