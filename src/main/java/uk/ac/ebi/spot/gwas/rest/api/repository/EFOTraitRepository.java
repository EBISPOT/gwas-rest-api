package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.ac.ebi.spot.gwas.model.EfoTrait;

import java.util.Optional;

public interface EFOTraitRepository extends JpaRepository<EfoTrait, Long>, QuerydslPredicateExecutor<EfoTrait> {

    Page<EfoTrait> findDistinctByStudiesHousekeepingIsPublishedAndStudiesHousekeepingCatalogPublishDateIsNotNull(Boolean flag, Pageable pageable);

    Optional<EfoTrait> findByShortForm(String shortForm);
}
