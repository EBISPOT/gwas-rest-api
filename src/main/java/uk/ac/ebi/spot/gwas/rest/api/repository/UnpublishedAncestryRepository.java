package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.spot.gwas.model.UnpublishedAncestry;

public interface UnpublishedAncestryRepository extends JpaRepository<UnpublishedAncestry, Long> {


}
