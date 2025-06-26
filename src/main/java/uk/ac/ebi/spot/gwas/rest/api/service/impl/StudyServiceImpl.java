package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.StudyService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchStudyParams;
import uk.ac.ebi.spot.gwas.rest.dto.StudiesSortParam;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StudyServiceImpl implements StudyService {

    @PersistenceContext
    private EntityManager em;

    StudyRepository studyRepository;

    @Autowired
    public StudyServiceImpl(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    @Transactional(readOnly = true)
    public  Page<Study> getStudies(Pageable pageable, SearchStudyParams searchStudyParams, String sortParam, String direction) {
        QStudy qStudy = QStudy.study;
        QStudyExtension qStudyExtension = QStudyExtension.studyExtension;
        QEfoTrait qEfoTrait = QEfoTrait.efoTrait;
        QDiseaseTrait qDiseaseTrait =  QDiseaseTrait.diseaseTrait;
        QPublication qPublication = QPublication.publication1;
        QHousekeeping qHousekeeping = QHousekeeping.housekeeping;
        QAncestry qAncestry  = QAncestry.ancestry;
        QAncestralGroup qAncestralGroup = QAncestralGroup.ancestralGroup1;
       // Boolean isExpressionNotEmpty = false;
        //Querydsl querydsl = new Querydsl(em , (new PathBuilderFactory()).create(Study.class));
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);
        JPQLQuery<Study> studyJPQLQuery = jpaQuery.select(qStudy).distinct()
                .from(qStudy);
        JPQLQuery<Long> studySubQuery = jpaQuery.select(qStudy.id).from(qStudy)
                .innerJoin(qStudy.ancestries, qAncestry).where(qAncestry.type.eq("initial"));
        List<Study> results = null;
        Long totalElements = null;
        log.info("searchStudyParams {}", searchStudyParams);
        log.info("Inside searchStudyParams not null block");
        try {

            if (searchStudyParams.getShowChildTrait() != null && (searchStudyParams.getEfoTrait() != null || searchStudyParams.getShortForm() != null)) {
                if(searchStudyParams.getShowChildTrait()) {
                    studyJPQLQuery =  studyJPQLQuery
                            .innerJoin(qStudy.parentStudyEfoTraits, qEfoTrait);
                } else {
                    studyJPQLQuery =  studyJPQLQuery
                            .innerJoin(qStudy.efoTraits, qEfoTrait);
                }
            }
            if (searchStudyParams.getShowChildTrait() == null && (searchStudyParams.getEfoTrait() != null || searchStudyParams.getShortForm() != null)) {
                studyJPQLQuery =  studyJPQLQuery
                        .innerJoin(qStudy.efoTraits, qEfoTrait);
            }
            if (searchStudyParams.getDiseaseTrait() != null) {
                studyJPQLQuery = studyJPQLQuery.
                        innerJoin(qStudy.diseaseTrait, qDiseaseTrait);
            }
            if (searchStudyParams.getPubmedId() != null) {
                studyJPQLQuery = studyJPQLQuery.
                        innerJoin(qStudy.publicationId, qPublication);
            }
            if(searchStudyParams.getCohort() != null) {
                studyJPQLQuery = studyJPQLQuery
                        .innerJoin(qStudy.studyExtension, qStudyExtension);
            }

            if(searchStudyParams.getAncestralGroup() != null) {
                studyJPQLQuery = studyJPQLQuery
                        .innerJoin(qStudy.ancestries, qAncestry)
                        .innerJoin(qAncestry.ancestralGroups, qAncestralGroup);
            }
            if(searchStudyParams.getNoOfIndividuals() != null) {
                studyJPQLQuery = studyJPQLQuery
                        .innerJoin(qStudy.ancestries, qAncestry);

            }
            if (searchStudyParams.getShortForm() != null) {
                studyJPQLQuery = studyJPQLQuery.where(qEfoTrait.shortForm.equalsIgnoreCase(searchStudyParams.getShortForm()));
            }
            if (searchStudyParams.getEfoTrait() != null) {
                studyJPQLQuery = studyJPQLQuery.where(qEfoTrait.trait.equalsIgnoreCase(searchStudyParams.getEfoTrait()));
            }
            if (searchStudyParams.getDiseaseTrait() != null) {
                studyJPQLQuery  =  studyJPQLQuery.where(qDiseaseTrait.trait.equalsIgnoreCase(searchStudyParams.getDiseaseTrait()));
            }
            if (searchStudyParams.getPubmedId() != null) {
                studyJPQLQuery = studyJPQLQuery.where(qPublication.pubmedId.eq(searchStudyParams.getPubmedId()));
            }
            if (searchStudyParams.getAccessionId() != null) {
                studyJPQLQuery = studyJPQLQuery.where(qStudy.accessionId.equalsIgnoreCase(searchStudyParams.getAccessionId()));
            }
            if (searchStudyParams.getFullPvalueSet() != null) {
                studyJPQLQuery = studyJPQLQuery.where(qStudy.fullPvalueSet.eq(searchStudyParams.getFullPvalueSet()));
            }
            if(searchStudyParams.getCohort() != null) {
                studyJPQLQuery = studyJPQLQuery.where(qStudyExtension.cohort.containsIgnoreCase(searchStudyParams.getCohort()));
            }
            if(searchStudyParams.getGxe() != null) {
                studyJPQLQuery = studyJPQLQuery.where(qStudy.gxe.eq(searchStudyParams.getGxe()));
            }

            if(searchStudyParams.getAncestralGroup() != null) {
                studyJPQLQuery = studyJPQLQuery.where(qAncestry.type.eq("initial"))
                        .where(qAncestralGroup.ancestralGroup
                        .containsIgnoreCase(searchStudyParams.getAncestralGroup()));
            }
            if(searchStudyParams.getNoOfIndividuals() != null) {
                studySubQuery  =  studySubQuery
                        .groupBy(qStudy.id)
                        .having(qAncestry.numberOfIndividuals.sum().goe(searchStudyParams.getNoOfIndividuals()));
                studyJPQLQuery = studyJPQLQuery.where(qStudy.id.in(studySubQuery));
            }

            //if (isExpressionNotEmpty) {
            studyJPQLQuery = studyJPQLQuery.innerJoin(qStudy.housekeeping, qHousekeeping)
                    .where(qHousekeeping.isPublished.eq(true))
                    .where(qHousekeeping.catalogPublishDate.isNotNull());

            results = studyJPQLQuery.orderBy(buildSortParams(sortParam).asc(),
                            buildDirectionSpecifier(direction, sortParam))
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
            totalElements = studyJPQLQuery.fetchCount();
           //}
        }catch( Exception ex) {
            log.info("Inside Exception in dsl query");
            log.error("Exception in dsl query"+ex.getMessage(),ex);
        }catch(Throwable ex) {
            log.info("Inside Throwable in dsl query");
            log.error("Throwable in dsl query"+ex.getMessage(),ex);
        }
        return new PageImpl<>(results, pageable, totalElements);
       // return studyRepository.findByHousekeepingIsPublishedAndHousekeepingCatalogPublishDateIsNotNull(true, pageable);
    }


    public Study getStudy(Long studyId) {
        return studyRepository.findById(studyId).orElse(null);
    }

    public Optional<Study> getStudy(String accessionId) {
        log.info("Find study by accessionId {}", accessionId);
        return studyRepository.findByAccessionId(accessionId);
    }

    private NumberExpression<Integer> buildSortParams(String sortParam) {
        QStudy qStudy = QStudy.study;
        NumberExpression<Integer> sortColumOrder = null;

        if(sortParam != null) {
            if (sortParam.equals(StudiesSortParam.snp_count.name())) {
                sortColumOrder = new CaseBuilder()
                        .when(qStudy.snpCount.isNull())
                        .then(1)
                        .otherwise(0);
            }
            if (sortParam.equals(StudiesSortParam.accession_Id.name())) {
                sortColumOrder = new CaseBuilder()
                        .when(qStudy.accessionId.isNull())
                        .then(1)
                        .otherwise(0);
            }
        } else {
            sortColumOrder = new CaseBuilder()
                    .when(qStudy.id.isNull())
                    .then(1)
                    .otherwise(0);
        }
        return sortColumOrder;
    }

    private OrderSpecifier<?> buildDirectionSpecifier(String direction, String sortParam) {
        QStudy qStudy = QStudy.study;
        OrderSpecifier<?> orderSpecifier = null;
        if(sortParam != null && direction != null) {
            if (sortParam.equals(StudiesSortParam.snp_count.name())) {
                orderSpecifier = direction.equals("asc") ? qStudy.snpCount.asc() : qStudy.snpCount.desc();
            }
            if (sortParam.equals(StudiesSortParam.accession_Id.name())) {
                orderSpecifier = direction.equals("asc") ? qStudy.accessionId.asc() : qStudy.accessionId.desc();
            }
        } else {
            orderSpecifier = qStudy.id.desc();
        }
        return orderSpecifier;
    }

}


