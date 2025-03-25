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
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.repository.AssociationRepository;
import uk.ac.ebi.spot.gwas.rest.api.repository.DiseaseTraitRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.AssociationService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchAssociationParams;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
    public  Page<Association> getAssociations(Pageable pageable, SearchAssociationParams searchAssociationParams) {
        QAssociation qAssociation = QAssociation.association;
        QLocus qLocus = QLocus.locus;
        QStudy qStudy = QStudy.study;
        QPublication qPublication = QPublication.publication1;
        QHousekeeping qHousekeeping = QHousekeeping.housekeeping;
        QRiskAllele qiskAllele = QRiskAllele.riskAllele;
        QEfoTrait qEfoTrait = QEfoTrait.efoTrait;
        QSingleNucleotidePolymorphism qSingleNucleotidePolymorphism = QSingleNucleotidePolymorphism.singleNucleotidePolymorphism;
        Boolean isExpressionNotEmpty = false;
        Querydsl querydsl = new Querydsl(em , (new PathBuilderFactory()).create(Association.class));
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
            /*if(searchAssociationParams.getAccessionId() != null || searchAssociationParams.getFullPvalueSet() != null) {
                associationJPQLQuery = associationJPQLQuery
                        .innerJoin(qAssociation.study, qStudy);
            }*/
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

            if(searchAssociationParams.getRsId() != null) {
                isExpressionNotEmpty = true;
                associationJPQLQuery = associationJPQLQuery
                        .where(qSingleNucleotidePolymorphism.rsId
                                .equalsIgnoreCase(searchAssociationParams.getRsId()));
            }
            if(searchAssociationParams.getPubmedId() != null) {
                isExpressionNotEmpty = true;
                associationJPQLQuery = associationJPQLQuery
                        .where(qPublication.pubmedId.eq(searchAssociationParams.getPubmedId()));
            }
            if(searchAssociationParams.getAccessionId() != null) {
                isExpressionNotEmpty = true;
                associationJPQLQuery = associationJPQLQuery
                        .where(qStudy.accessionId.eq(searchAssociationParams.getAccessionId()));
            }
            if(searchAssociationParams.getFullPvalueSet() != null) {
                isExpressionNotEmpty = true;
                associationJPQLQuery = associationJPQLQuery
                        .where(qStudy.fullPvalueSet.eq(searchAssociationParams.getFullPvalueSet()));

            }
            if(searchAssociationParams.getEfoTrait() != null) {
                isExpressionNotEmpty = true;
                associationJPQLQuery = associationJPQLQuery
                        .where(qEfoTrait.trait.equalsIgnoreCase(searchAssociationParams.getEfoTrait()));
            }
            if(searchAssociationParams.getShortForm() != null) {
                isExpressionNotEmpty = true;
                associationJPQLQuery = associationJPQLQuery
                        .where(qEfoTrait.shortForm.equalsIgnoreCase(searchAssociationParams.getShortForm()));
            }

            if(isExpressionNotEmpty) {
                associationJPQLQuery = associationJPQLQuery
                        .innerJoin(qAssociation.study, qStudy)
                        .innerJoin(qStudy.housekeeping, qHousekeeping)
                        .where(qHousekeeping.isPublished.eq(true))
                        .where(qHousekeeping.catalogPublishDate.isNotNull());
                Long totalElements = associationJPQLQuery.fetchCount();
                List<Association> results = querydsl.applyPagination(pageable, associationJPQLQuery).fetch();
                return new PageImpl<>(results, pageable, totalElements);
            }
        }catch( Exception ex) {
            log.info("Inside Exception in dsl query");
            log.error("Exception in dsl query"+ex.getMessage(),ex);
        }catch(Throwable ex) {
            log.info("Inside Throwable in dsl query");
            log.error("Throwable in dsl query"+ex.getMessage(),ex);
        }
        return associationRepository.findByStudyHousekeepingIsPublishedAndStudyHousekeepingCatalogPublishDateIsNotNull(true, pageable);
    }

    public Optional<Association> getAssociation(Long associationId) {
        return associationRepository.findById(associationId);
    }

    public List<DiseaseTrait> getDiseaseTraits(Long associationId) {
        return diseaseTraitRepository.findByStudiesAssociationsIdAndStudiesHousekeepingCatalogPublishDateIsNotNullAndStudiesHousekeepingCatalogUnpublishDateIsNull(associationId);
    }

}
