package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.ac.ebi.spot.gwas.model.Association;

public interface AssociationRepository extends JpaRepository<Association, Long>, QuerydslPredicateExecutor<Association> {


   public Page<Association> findByStudyHousekeepingIsPublishedAndStudyHousekeepingCatalogPublishDateIsNotNull(Boolean published, Pageable pageable);
}
