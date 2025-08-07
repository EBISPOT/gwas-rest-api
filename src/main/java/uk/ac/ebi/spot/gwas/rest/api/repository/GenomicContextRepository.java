package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.spot.gwas.model.GenomicContext;

import java.util.List;

public interface GenomicContextRepository extends JpaRepository<GenomicContext, Long> {

   List<GenomicContext> findBySnpStudiesHousekeepingIsPublishedAndSnpStudiesHousekeepingCatalogPublishDateIsNotNullAndSnpRsIdOrderByDistance(Boolean published, String rsId);

   List<GenomicContext> findBySnpStudiesHousekeepingIsPublishedAndSnpStudiesHousekeepingCatalogPublishDateIsNotNullAndSnpRsIdOrderByDistanceDesc(Boolean published, String rsId);

   List<GenomicContext> findBySnpStudiesHousekeepingIsPublishedAndSnpStudiesHousekeepingCatalogPublishDateIsNotNullAndSnpRsId(Boolean published, String rsId);
}
