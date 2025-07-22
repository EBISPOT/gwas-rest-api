package uk.ac.ebi.spot.gwas.rest.api.service.impl;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
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
import uk.ac.ebi.spot.gwas.rest.dto.SnpSortParam;
import uk.ac.ebi.spot.gwas.rest.projection.GeneProjection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SnpServiceImpl implements SnpService {

    @PersistenceContext
    private EntityManager em;

    private SingleNucleotidePolymorphismRepository singleNucleotidePolymorphismRepository;

    private GeneRepository geneRepository;

    public SnpServiceImpl(SingleNucleotidePolymorphismRepository singleNucleotidePolymorphismRepository, GeneRepository geneRepository) {
        this.singleNucleotidePolymorphismRepository = singleNucleotidePolymorphismRepository;
        this.geneRepository = geneRepository;
    }

    public Page<SingleNucleotidePolymorphism> getSnps(SearchSnpParams searchSnpParams, Pageable pageable, String sortParam, String direction) {
        QSingleNucleotidePolymorphism qSingleNucleotidePolymorphism = QSingleNucleotidePolymorphism.singleNucleotidePolymorphism;
        QLocation qLocation = QLocation.location;
        QGenomicContext qGenomicContext = QGenomicContext.genomicContext;
        QGene qGene = QGene.gene;
        QStudy qStudy = QStudy.study;
        QPublication qPublication =  QPublication.publication1;
        QHousekeeping qHousekeeping =  QHousekeeping.housekeeping;
        QAssociation qAssociation =  QAssociation.association;
        List<Tuple> tuples = null;
        Long totalElements = 0L;
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);
        /*JPQLQuery<SingleNucleotidePolymorphism> snpJPQLQuery = jpaQuery.select(qSingleNucleotidePolymorphism).leftJoin(qSingleNucleotidePolymorphism.locations, qLocation).distinct()
                .from(qSingleNucleotidePolymorphism); */
        JPQLQuery<Tuple> snpJPQLQuery = jpaQuery.select(qSingleNucleotidePolymorphism, qLocation).from(qSingleNucleotidePolymorphism)
                        .leftJoin(qSingleNucleotidePolymorphism.locations, qLocation).distinct();

        log.info("searchSnpParams {}", searchSnpParams);
        try {

/*            if(searchSnpParams.getBpLocation() != null || searchSnpParams.getBpStart() != null ||
                    searchSnpParams.getBpEnd() != null || searchSnpParams.getChromosome() != null) {*/
/*                snpJPQLQuery = snpJPQLQuery
                        .innerJoin(qSingleNucleotidePolymorphism.locations, qLocation);*/
            //}
            if(searchSnpParams.getMappedGene() != null) {
                if(searchSnpParams.getExtendedGeneSet() != null && searchSnpParams.getExtendedGeneSet()) {
                    snpJPQLQuery = snpJPQLQuery
                            .innerJoin(qSingleNucleotidePolymorphism.genes, qGene);
                } else {
                    snpJPQLQuery = snpJPQLQuery
                            .innerJoin(qSingleNucleotidePolymorphism.associations, qAssociation)
                            .innerJoin(qAssociation.mappedGenes , qGene);
                }
            }
            if(searchSnpParams.getPubmedId() != null) {
                snpJPQLQuery =  snpJPQLQuery
                        .innerJoin(qSingleNucleotidePolymorphism.studies, qStudy)
                        .innerJoin(qStudy.publicationId, qPublication);
            }
            if(searchSnpParams.getRsId() != null) {
                snpJPQLQuery = snpJPQLQuery
                        .where(qSingleNucleotidePolymorphism.rsId.equalsIgnoreCase(searchSnpParams.getRsId()));
            }
            if(searchSnpParams.getBpLocation() != null) {
                snpJPQLQuery = snpJPQLQuery
                        .where(qLocation.chromosomePosition.eq(searchSnpParams.getBpLocation()));
            }
            if(searchSnpParams.getChromosome() != null && searchSnpParams.getBpStart() != null
                    && searchSnpParams.getBpEnd() != null) {
                snpJPQLQuery = snpJPQLQuery
                        .where(qLocation.chromosomeName.eq(searchSnpParams.getChromosome()))
                        .where(qLocation.chromosomePosition.between(searchSnpParams.getBpStart(), searchSnpParams.getBpEnd()));
            }
            if(searchSnpParams.getMappedGene() != null) {
                snpJPQLQuery = snpJPQLQuery
                        .where(qGene.geneName.equalsIgnoreCase(searchSnpParams.getMappedGene()));
            }
            if(searchSnpParams.getPubmedId() != null) {
                snpJPQLQuery = snpJPQLQuery
                        .where(qPublication.pubmedId.equalsIgnoreCase(searchSnpParams.getPubmedId()));
            }
            snpJPQLQuery = snpJPQLQuery
                    .innerJoin(qSingleNucleotidePolymorphism.studies, qStudy)
                    .innerJoin(qStudy.housekeeping, qHousekeeping)
                    .where(qHousekeeping.isPublished.eq(true))
                    .where(qHousekeeping.catalogPublishDate.isNotNull());
            totalElements = snpJPQLQuery.fetchCount();
            if(sortParam != null && direction != null && sortParam.equals(SnpSortParam.location.name())) {
                tuples = snpJPQLQuery.orderBy(buildSortParams(sortParam).asc(),
                                buildDirectionSpecifier(direction, SnpSortParam.chromosome_name.name()),
                                buildDirectionSpecifier(direction, sortParam))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();
            } else {
                tuples = snpJPQLQuery.orderBy(buildSortParams(sortParam).asc(),
                                buildDirectionSpecifier(direction, sortParam))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();
            }
        }catch( Exception ex) {
            log.info("Inside Exception in dsl query");
            log.error("Exception in dsl query"+ex.getMessage(),ex);
        }catch(Throwable ex) {
            log.info("Inside Throwable in dsl query");
            log.error("Throwable in dsl query"+ex.getMessage(),ex);
        }
        log.info("Outside the QueryDSL condition");
        List<SingleNucleotidePolymorphism> snps = new ArrayList<>();
        for(Tuple tuple : tuples) {
            SingleNucleotidePolymorphism snp = tuple.get(qSingleNucleotidePolymorphism);
            snps.add(snp);
        }
        return new PageImpl<>(snps, pageable, totalElements);
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
                    .filter(GeneProjection::getIsDownstream)
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

    private NumberExpression<Integer> buildSortParams(String sortParam) {
        QSingleNucleotidePolymorphism  qSingleNucleotidePolymorphism = QSingleNucleotidePolymorphism.singleNucleotidePolymorphism;
        QLocation qLocation = QLocation.location;
        NumberExpression<Integer> sortColumOrder = null;
        if(sortParam != null) {
            if (sortParam.equals(SnpSortParam.location.name())) {
                sortColumOrder = new CaseBuilder()
                        .when(qSingleNucleotidePolymorphism.locations.isEmpty())
                        .then(1)
                        .otherwise(0);
            }
            if (sortParam.equals(SnpSortParam.rs_id.name())) {
                sortColumOrder = new CaseBuilder()
                        .when(qSingleNucleotidePolymorphism.rsId.isNull())
                        .then(1)
                        .otherwise(0);
            }
        } else {
            sortColumOrder = new CaseBuilder()
                    .when(qSingleNucleotidePolymorphism.id.isNull())
                    .then(1)
                    .otherwise(0);

        }
        return sortColumOrder;
    }

    private OrderSpecifier<?> buildDirectionSpecifier(String direction, String sortParam) {
        QSingleNucleotidePolymorphism  qSingleNucleotidePolymorphism = QSingleNucleotidePolymorphism.singleNucleotidePolymorphism;
        QLocation qLocation = QLocation.location;
        OrderSpecifier<?> orderSpecifier = null;
        if(sortParam != null) {
            if(direction != null) {
                if (sortParam.equals(SnpSortParam.location.name())) {
                    orderSpecifier = direction.equals("asc") ? qLocation.chromosomePosition.asc() : qLocation.chromosomePosition.desc();
                }
                if (sortParam.equals(SnpSortParam.chromosome_name.name())) {
                    orderSpecifier = direction.equals("asc") ? qLocation.chromosomeName.asc() : qLocation.chromosomeName.desc();
                }
                if (sortParam.equals(SnpSortParam.rs_id.name())) {
                    orderSpecifier = direction.equals("asc") ? qSingleNucleotidePolymorphism.rsId.asc() : qSingleNucleotidePolymorphism.rsId.desc();
                }
            } else {
                orderSpecifier = qSingleNucleotidePolymorphism.id.desc();
            }
        } else {
            orderSpecifier = qSingleNucleotidePolymorphism.id.desc();
        }
        return orderSpecifier;
    }
}
