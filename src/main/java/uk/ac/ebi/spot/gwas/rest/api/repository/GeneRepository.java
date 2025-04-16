package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.spot.gwas.model.Gene;

import java.util.List;
import java.util.Set;

public interface GeneRepository extends JpaRepository<Gene, Long> {

  List<Gene> findByGeneNameIn(Set<String> geneNames);

}
