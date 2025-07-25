package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.repository.AssociationRepository;
import uk.ac.ebi.spot.gwas.rest.api.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.AssociationService;
import uk.ac.ebi.spot.gwas.rest.dto.AssociationSortParam;
import uk.ac.ebi.spot.gwas.rest.dto.SearchAssociationParams;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AssociationServiceImpl implements AssociationService {

    private final AssociationRepository associationRepository;
    private final DiseaseTraitRepository diseaseTraitRepository;
    @PersistenceContext
    private EntityManager em;

    public AssociationServiceImpl(AssociationRepository associationRepository,
                                  DiseaseTraitRepository diseaseTraitRepository) {
        this.associationRepository = associationRepository;
        this.diseaseTraitRepository = diseaseTraitRepository;
    }

    @Transactional(readOnly = true)
    public  Page<Association> getAssociations(Pageable pageable, SearchAssociationParams searchAssociationParams, String sortParam, String direction) {
        QAssociation qAssociation = QAssociation.association;
        QLocus qLocus = QLocus.locus;
        QStudy qStudy = QStudy.study;
        QPublication qPublication = QPublication.publication1;
        QHousekeeping qHousekeeping = QHousekeeping.housekeeping;
        QRiskAllele qiskAllele = QRiskAllele.riskAllele;
        QEfoTrait qEfoTrait = QEfoTrait.efoTrait;
        QSingleNucleotidePolymorphism qSingleNucleotidePolymorphism = QSingleNucleotidePolymorphism.singleNucleotidePolymorphism;
        QGene qGene = QGene.gene;
        Long totalElements = 0L;
        List<Association> results= new ArrayList<>();
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);
        JPQLQuery<Association> associationJPQLQuery = jpaQuery.select(qAssociation).distinct()
                .from(qAssociation);
        log.info("searchAssociationParams {}", searchAssociationParams);
        log.info("Inside searchAssociationParams not null block");
        try {

            if(searchAssociationParams.getRsId() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .innerJoin(qAssociation.loci, qLocus)
                        .innerJoin(qLocus.strongestRiskAlleles, qiskAllele)
                        .innerJoin(qiskAllele.snp, qSingleNucleotidePolymorphism);
            }
            if(searchAssociationParams.getPubmedId() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .innerJoin(qAssociation.study, qStudy)
                        .innerJoin(qStudy.publicationId, qPublication);
            }
            if(searchAssociationParams.getShowChildTrait() != null && (searchAssociationParams.getEfoTrait() != null || searchAssociationParams.getShortForm() != null)) {
                if(searchAssociationParams.getShowChildTrait()) {
                    associationJPQLQuery = associationJPQLQuery
                            .innerJoin(qAssociation.parentEfoTraits, qEfoTrait);
                } else {
                    associationJPQLQuery = associationJPQLQuery
                            .innerJoin(qAssociation.efoTraits, qEfoTrait);
                }
            }
            if(searchAssociationParams.getShowChildTrait() == null &&  (searchAssociationParams.getEfoTrait() != null || searchAssociationParams.getShortForm() != null)) {
                associationJPQLQuery = associationJPQLQuery
                        .innerJoin(qAssociation.efoTraits, qEfoTrait);
            }

            if(searchAssociationParams.getMappedGene() != null) {
                if(searchAssociationParams.getExtendedGeneSet() != null && searchAssociationParams.getExtendedGeneSet()){
                    associationJPQLQuery = associationJPQLQuery
                            .innerJoin(qAssociation.snps, qSingleNucleotidePolymorphism)
                            .innerJoin(qSingleNucleotidePolymorphism.genes , qGene);
                } else {
                    associationJPQLQuery = associationJPQLQuery
                            .innerJoin(qAssociation.snps, qSingleNucleotidePolymorphism)
                            .innerJoin(qSingleNucleotidePolymorphism.mappedSnpGenes , qGene);
                }
            }

            if(searchAssociationParams.getRsId() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .where(qSingleNucleotidePolymorphism.rsId
                                .equalsIgnoreCase(searchAssociationParams.getRsId()));
            }
            if(searchAssociationParams.getPubmedId() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .where(qPublication.pubmedId.eq(searchAssociationParams.getPubmedId()));
            }
            if(searchAssociationParams.getAccessionId() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .where(qStudy.accessionId.eq(searchAssociationParams.getAccessionId()));
            }
            if(searchAssociationParams.getFullPvalueSet() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .where(qStudy.fullPvalueSet.eq(searchAssociationParams.getFullPvalueSet()));

            }
            if(searchAssociationParams.getEfoTrait() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .where(qEfoTrait.trait.equalsIgnoreCase(searchAssociationParams.getEfoTrait()));
            }
            if(searchAssociationParams.getShortForm() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .where(qEfoTrait.shortForm.equalsIgnoreCase(searchAssociationParams.getShortForm()));
            }

            if(searchAssociationParams.getMappedGene() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .where(qGene.geneName.equalsIgnoreCase(searchAssociationParams.getMappedGene()));
            }

            associationJPQLQuery = associationJPQLQuery
                    .innerJoin(qAssociation.study, qStudy)
                    .innerJoin(qStudy.housekeeping, qHousekeeping)
                    .where(qHousekeeping.isPublished.eq(true))
                    .where(qHousekeeping.catalogPublishDate.isNotNull());
            totalElements = associationJPQLQuery.fetchCount();
            //List<Association> results = querydsl.applyPagination(pageable, associationJPQLQuery).fetch();
            if(sortParam != null && sortParam.equals(AssociationSortParam.p_value.name())) {
                results =  associationJPQLQuery.orderBy(buildSortParams(sortParam).asc(),
                                buildDirectionSpecifier(direction, AssociationSortParam.p_value_exponent.name()),
                                buildDirectionSpecifier(direction, sortParam))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();
            } else{
                results =  associationJPQLQuery.orderBy(buildSortParams(sortParam).asc(),
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
        //return associationRepository.findByStudyHousekeepingIsPublishedAndStudyHousekeepingCatalogPublishDateIsNotNull(true, pageable);
        return new PageImpl<>(results, pageable, totalElements);
    }

    public Optional<Association> getAssociation(Long associationId) {
        return associationRepository.findById(associationId);
    }

    public List<DiseaseTrait> getDiseaseTraits(Long associationId) {
        return diseaseTraitRepository.findByStudiesAssociationsIdAndStudiesHousekeepingCatalogPublishDateIsNotNullAndStudiesHousekeepingCatalogUnpublishDateIsNull(associationId);
    }

    private NumberExpression<Integer> buildSortParams(String sortParam) {
        QAssociation qAssociation = QAssociation.association;
        NumberExpression<Integer> sortColumOrder = null;

        if(sortParam != null) {
            if (sortParam.equals(AssociationSortParam.p_value.name())) {
                sortColumOrder = new CaseBuilder()
                        .when(qAssociation.pvalueMantissa.isNull())
                        .then(1)
                        .otherwise(0);
            }
            if (sortParam.equals(AssociationSortParam.risk_frequency.name())) {
                sortColumOrder = new CaseBuilder()
                        .when(qAssociation.riskFrequency.isNull())
                        .then(1)
                        .otherwise(0);
            }
            if (sortParam.equals(AssociationSortParam.or_value.name())) {
                sortColumOrder = new CaseBuilder()
                        .when(qAssociation.orPerCopyNum.isNull())
                        .then(1)
                        .otherwise(0);
            }
            if (sortParam.equals(AssociationSortParam.beta_num.name())) {
                sortColumOrder = new CaseBuilder()
                        .when(qAssociation.betaNum.isNull())
                        .then(1)
                        .otherwise(0);
            }
        } else {
            sortColumOrder = new CaseBuilder()
                    .when(qAssociation.id.isNull())
                    .then(1)
                    .otherwise(0);
        }

        return sortColumOrder;
    }


    private OrderSpecifier<?> buildDirectionSpecifier(String direction, String sortParam) {
        QAssociation qAssociation = QAssociation.association;
        OrderSpecifier<?> orderSpecifier = null;
        if(sortParam != null && direction != null) {
            if (sortParam.equals(AssociationSortParam.p_value.name())) {
                orderSpecifier = direction.equals("asc") ? qAssociation.pvalueMantissa.asc() : qAssociation.pvalueMantissa.desc();
            }
            if (sortParam.equals(AssociationSortParam.p_value_exponent.name())) {
                orderSpecifier = direction.equals("asc") ? qAssociation.pvalueExponent.asc() : qAssociation.pvalueExponent.desc();
            }
            if (sortParam.equals(AssociationSortParam.risk_frequency.name())) {
                orderSpecifier = direction.equals("asc") ? qAssociation.riskFrequency.asc() : qAssociation.riskFrequency.desc();
            }
            if (sortParam.equals(AssociationSortParam.or_value.name())) {
                orderSpecifier = direction.equals("asc") ? qAssociation.orPerCopyNum.asc() : qAssociation.orPerCopyNum.desc();
            }
            if (sortParam.equals(AssociationSortParam.beta_num.name())) {
                orderSpecifier = direction.equals("asc") ? qAssociation.betaNum.asc() : qAssociation.betaNum.desc();
            }
        } else {
            orderSpecifier = qAssociation.id.desc();
        }
        return orderSpecifier;
    }

}
