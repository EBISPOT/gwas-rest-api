package uk.ac.ebi.spot.gwas.rest.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.spot.gwas.model.Study;
import uk.ac.ebi.spot.gwas.rest.projection.StudyProjection;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface StudyRepository extends JpaRepository<Study, Long>, QuerydslPredicateExecutor<Study> {
//public interface StudyRepository extends JpaRepository<Study, Long> {


    //@QueryHints(@QueryHint(name = "hibernate.query.passDistinctThrough",value = "true"))
    @Query(" select DISTINCT s.id as studyId , s.initialSampleSize as initialSampleSize , s.replicateSampleSize as replicateSampleSize" +
            " , s.gxe as gxe , s.gxg as gxg , s.snpCount as snpCount ,s.qualifier as qualifier, s.imputed as imputed, s.pooled as pooled" +
            " , s.studyDesignComment as studyDesignComment, s.accessionId as accessionId, s.fullPvalueSet as fullPvalueSet, " +
            "s.userRequested as userRequested, p.pubmedId as pubmedId, " +
            "pl as platforms, d as diseaseTrait, gt as genotypingTechnologies  " +
           // "e as efoTraits, a as ancestries, m as mappedBackgroundTraits " +
            " from Study s " +
            "left join s.publicationId p " +
            "left join s.efoTraits e " +
           "left join s.diseaseTrait d " +
            "left join s.ancestries a " +
             "left join s.platforms pl " +
           "left join s.genotypingTechnologies gt where  " +
            "(s.accessionId = :accessionId OR :accessionId = '*') " +
            " AND (s.fullPvalueSet = :fullPvalueSet OR  :fullPvalueSet IS NULL) " +
            " AND ((lower(e.shortForm) = lower(:shortForm)) OR :shortForm = '*')" +
            " AND ((lower(e.trait) = lower(:efoTrait)) OR :efoTrait = '*') " +
            " AND ((lower(d.trait) = lower(:diseaseTrait)) OR :diseaseTrait = '*') " +
            " AND (p.pubmedId = :pubmedId OR :pubmedId = '*') " +
            " AND (s.userRequested = :userRequested OR :userRequested IS NULL)")
    Page<StudyProjection> findByCustomQuery(@Param("accessionId") String accessionId,
                                            @Param("fullPvalueSet") Boolean fullPvalueSet,
                                            @Param("shortForm") String shortForm,
                                            @Param("efoTrait") String efoTrait,
                                            @Param("diseaseTrait") String diseaseTrait,
                                            @Param("pubmedId") String pubmedId,
                                            @Param("userRequested") Boolean userRequested,
                                            Pageable pageable);



}
