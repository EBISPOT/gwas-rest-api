package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.gwas.model.EnsemblRestcallHistory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface EnsemblRestcallHistoryRepository extends JpaRepository<EnsemblRestcallHistory, Long> {

    Optional<List<EnsemblRestcallHistory>> findByRequestTypeAndEnsemblParamAndEnsemblVersion(String requestType, String ensemblParam, String ensemblVersion);

}
