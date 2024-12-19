package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.ac.ebi.spot.gwas.model.Ancestry;

import java.util.List;

public interface AncestryRepository extends JpaRepository<Ancestry, Long> , QuerydslPredicateExecutor<Ancestry> {

    List<Ancestry> findByStudyHousekeepingIsPublishedAndStudyHousekeepingCatalogPublishDateIsNotNullAndStudyAccessionIdEquals(Boolean published, String accessionId);



}
