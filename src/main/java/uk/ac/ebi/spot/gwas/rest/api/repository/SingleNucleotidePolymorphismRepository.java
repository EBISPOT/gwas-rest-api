package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.ac.ebi.spot.gwas.model.SingleNucleotidePolymorphism;
import uk.ac.ebi.spot.gwas.rest.projection.GeneProjection;

import java.util.List;
import java.util.Optional;

public interface SingleNucleotidePolymorphismRepository extends JpaRepository<SingleNucleotidePolymorphism, Long>, QuerydslPredicateExecutor<SingleNucleotidePolymorphism> {

   Page<SingleNucleotidePolymorphism> findByRiskAllelesLociAssociationStudyHousekeepingIsPublishedAndRiskAllelesLociAssociationStudyHousekeepingCatalogPublishDateIsNotNull(Boolean published , Pageable pageable);

   Page<SingleNucleotidePolymorphism> findDistinctByStudiesHousekeepingIsPublishedAndStudiesHousekeepingCatalogPublishDateIsNotNull(Boolean published, Pageable pageable);

   Optional<SingleNucleotidePolymorphism> findByRsId(String rsId);

   @Query(" select g.geneName as geneName, gc.isUpstream as isUpstream, gc.isDownstream as isDownstream, " +
           "gc.isIntergenic as isIntergenic, gc.isClosestGene as isClosestGene ,gc.distance as distance, " +
           " loc.chromosomeName as chromosomeName, loc.chromosomePosition as  chromosomePosition " +
           "from Gene as g "+
            "Join g.genomicContexts as gc "+
            "Join gc.location as loc "+
           "where gc.snp.id = :snpId "+
           "and  length(loc.chromosomeName) < 3 "+
           "and gc.source = :source")
   List<GeneProjection> findMatchingGenes(Long snpId, String source);


}
