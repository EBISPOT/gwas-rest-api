package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.spot.gwas.model.Locus;

import java.util.List;

public interface LocusRepository extends JpaRepository<Locus, Long> {

    List<Locus> findByAssociationStudyHousekeepingIsPublishedAndAssociationStudyHousekeepingCatalogPublishDateIsNotNullAndAssociationId(Boolean published, Long associationId);
}
