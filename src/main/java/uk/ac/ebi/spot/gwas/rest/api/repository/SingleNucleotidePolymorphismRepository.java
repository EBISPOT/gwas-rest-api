package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.ac.ebi.spot.gwas.model.SingleNucleotidePolymorphism;

import java.util.Optional;

public interface SingleNucleotidePolymorphismRepository extends JpaRepository<SingleNucleotidePolymorphism, Long>, QuerydslPredicateExecutor<SingleNucleotidePolymorphism> {

   Page<SingleNucleotidePolymorphism> findByRiskAllelesLociAssociationStudyHousekeepingIsPublishedAndRiskAllelesLociAssociationStudyHousekeepingCatalogPublishDateIsNotNull(Boolean published , Pageable pageable);

   Page<SingleNucleotidePolymorphism> findDistinctByStudiesHousekeepingIsPublishedAndStudiesHousekeepingCatalogPublishDateIsNotNull(Boolean published, Pageable pageable);

   Optional<SingleNucleotidePolymorphism> findByRsId(String rsId);

}
