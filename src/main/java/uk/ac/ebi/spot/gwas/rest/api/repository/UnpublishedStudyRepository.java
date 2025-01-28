package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.ac.ebi.spot.gwas.model.UnpublishedAncestry;
import uk.ac.ebi.spot.gwas.model.UnpublishedStudy;

import java.util.List;
import java.util.Optional;

public interface UnpublishedStudyRepository extends JpaRepository<UnpublishedStudy, Long>, QuerydslPredicateExecutor<UnpublishedStudy> {

   Optional<UnpublishedStudy> findByAccession(String accession);

  Page<UnpublishedStudy> findByBodiesOfWorkPublicationId(String bowId, Pageable pageable);

}
