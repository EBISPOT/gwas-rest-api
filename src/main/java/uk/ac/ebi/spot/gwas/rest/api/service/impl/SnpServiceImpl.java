package uk.ac.ebi.spot.gwas.rest.api.service.impl;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.repository.GeneRepository;
import uk.ac.ebi.spot.gwas.rest.api.repository.SingleNucleotidePolymorphismRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.SnpService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchSnpParams;
import uk.ac.ebi.spot.gwas.rest.projection.GeneProjection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class
SnpServiceImpl implements SnpService {

    @PersistenceContext
    private EntityManager em;

    private SingleNucleotidePolymorphismRepository singleNucleotidePolymorphismRepository;

    private GeneRepository geneRepository;

    public SnpServiceImpl(SingleNucleotidePolymorphismRepository singleNucleotidePolymorphismRepository, GeneRepository geneRepository) {
        this.singleNucleotidePolymorphismRepository = singleNucleotidePolymorphismRepository;
        this.geneRepository = geneRepository;
    }

    public Page<SingleNucleotidePolymorphism> getSnps(SearchSnpParams searchSnpParams, Pageable pageable) {
        QSingleNucleotidePolymorphism qSingleNucleotidePolymorphism = QSingleNucleotidePolymorphism.singleNucleotidePolymorphism;
        QLocation qLocation = QLocation.location;
        QGenomicContext qGenomicContext = QGenomicContext.genomicContext;
        QGene qGene = QGene.gene;
        QStudy qStudy = QStudy.study;
        QPublication qPublication =  QPublication.publication1;
        QHousekeeping qHousekeeping =  QHousekeeping.housekeeping;
        Boolean isExpressionNotEmpty = false;
        Querydsl querydsl = new Querydsl(em , (new PathBuilderFactory()).create(SingleNucleotidePolymorphism.class));
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);
        JPQLQuery<SingleNucleotidePolymorphism> snpJPQLQuery = jpaQuery.select(qSingleNucleotidePolymorphism).distinct()
                .from(qSingleNucleotidePolymorphism);
        log.info("searchSnpParams {}", searchSnpParams);
        try {

            if(searchSnpParams.getBpLocation() != null || searchSnpParams.getBpStart() != null ||
                    searchSnpParams.getBpEnd() != null || searchSnpParams.getChromosome() != null) {
                snpJPQLQuery = snpJPQLQuery
                        .innerJoin(qSingleNucleotidePolymorphism.locations, qLocation);
            }
            if(searchSnpParams.getGene() != null) {
                snpJPQLQuery = snpJPQLQuery
                        .innerJoin(qSingleNucleotidePolymorphism.genes, qGene);
            }
            if(searchSnpParams.getPubmedId() != null) {
                snpJPQLQuery =  snpJPQLQuery
                        .innerJoin(qSingleNucleotidePolymorphism.studies, qStudy)
                        .innerJoin(qStudy.publicationId, qPublication);
            }
            if(searchSnpParams.getRsId() != null) {
                isExpressionNotEmpty = true;
                snpJPQLQuery = snpJPQLQuery
                        .where(qSingleNucleotidePolymorphism.rsId.equalsIgnoreCase(searchSnpParams.getRsId()));
            }
            if(searchSnpParams.getBpLocation() != null) {
                isExpressionNotEmpty = true;
                snpJPQLQuery = snpJPQLQuery
                        .where(qLocation.chromosomePosition.eq(searchSnpParams.getBpLocation()));
            }
            if(searchSnpParams.getChromosome() != null && searchSnpParams.getBpStart() != null
                    && searchSnpParams.getBpEnd() != null) {
                isExpressionNotEmpty = true;
                snpJPQLQuery = snpJPQLQuery
                        .where(qLocation.chromosomeName.eq(searchSnpParams.getChromosome()))
                        .where(qLocation.chromosomePosition.between(searchSnpParams.getBpStart(), searchSnpParams.getBpEnd()));
            }
            if(searchSnpParams.getGene() != null) {
                isExpressionNotEmpty = true;
                snpJPQLQuery = snpJPQLQuery
                        .where(qGene.geneName.equalsIgnoreCase(searchSnpParams.getGene()));
            }
            if(searchSnpParams.getPubmedId() != null) {
                isExpressionNotEmpty = true;
                snpJPQLQuery = snpJPQLQuery
                        .where(qPublication.pubmedId.equalsIgnoreCase(searchSnpParams.getPubmedId()));
            }
            if(isExpressionNotEmpty) {
                snpJPQLQuery = snpJPQLQuery
                        .innerJoin(qSingleNucleotidePolymorphism.studies, qStudy)
                        .innerJoin(qStudy.housekeeping, qHousekeeping)
                        .where(qHousekeeping.isPublished.eq(true))
                        .where(qHousekeeping.catalogPublishDate.isNotNull());
                Long totalElements = snpJPQLQuery.fetchCount();
                List<SingleNucleotidePolymorphism> snps = querydsl.applyPagination(pageable, snpJPQLQuery).fetch();
                return new PageImpl<>(snps, pageable, totalElements);
            }
        }catch( Exception ex) {
            log.info("Inside Exception in dsl query");
            log.error("Exception in dsl query"+ex.getMessage(),ex);
        }catch(Throwable ex) {
            log.info("Inside Throwable in dsl query");
            log.error("Throwable in dsl query"+ex.getMessage(),ex);
        }
        log.info("Outside the QueryDSL condition");
        return singleNucleotidePolymorphismRepository.findDistinctByStudiesHousekeepingIsPublishedAndStudiesHousekeepingCatalogPublishDateIsNotNull(true, pageable);

    }

    public Optional<SingleNucleotidePolymorphism> getSnp(String rsId) {
        return  singleNucleotidePolymorphismRepository.findByRsId(rsId);
    }

    public List<String> findMatchingGenes(Long snpId) {
        List<GeneProjection> geneProjections = singleNucleotidePolymorphismRepository.findMatchingGenes(snpId, "Ensembl");
        List<String> mappedGenes = geneProjections.stream()
                .filter(geneProjection -> !geneProjection.getIsIntergenic())
                .map(GeneProjection::getGeneName)
                .distinct()
                .collect(Collectors.toList());

        if( mappedGenes.isEmpty()) {
            Long minUpStreamDistance = findMinDistance(geneProjections, "up");
            Long minDownStreamDistance = findMinDistance(geneProjections, "down");
            List<String>  mappedUpstreamGenes =  geneProjections.stream()
                    .filter(GeneProjection::getIsUpstream)
                    .filter(geneProjection -> ( minUpStreamDistance.longValue() == geneProjection.getDistance().longValue()))
                    .map(GeneProjection::getGeneName)
                    .distinct()
                    .collect(Collectors.toList());

            List<String>  mappedDownStreamGenes =  geneProjections.stream()
                    .filter(GeneProjection::getIsUpstream)
                    .filter(geneProjection -> ( minDownStreamDistance.longValue() == geneProjection.getDistance().longValue()))
                    .map(GeneProjection::getGeneName)
                    .distinct()
                    .collect(Collectors.toList());
            mappedGenes.addAll(mappedUpstreamGenes);
            mappedGenes.addAll(mappedDownStreamGenes);
            mappedGenes  =  mappedGenes.stream().distinct().collect(Collectors.toList());
        }

        if( mappedGenes.isEmpty()) {
            mappedGenes.add("intergenic");
        }

        return mappedGenes;
    }

    private Long findMinDistance(List<GeneProjection>  geneProjections, String whichStream) {
        return geneProjections.stream()
                .filter(whichStream.equals("up") ? GeneProjection::getIsUpstream : GeneProjection::getIsDownstream)
                .map(GeneProjection::getDistance)
                .filter(Objects::nonNull)
                .min(Long::compareTo)
                .orElse(null);
    }
}
