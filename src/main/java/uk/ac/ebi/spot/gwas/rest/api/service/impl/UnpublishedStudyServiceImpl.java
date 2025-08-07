package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.QBodyOfWork;
import uk.ac.ebi.spot.gwas.model.QUnpublishedStudy;
import uk.ac.ebi.spot.gwas.model.UnpublishedStudy;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.repository.UnpublishedStudyRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.BodyOfWorkService;
import uk.ac.ebi.spot.gwas.rest.api.service.UnpublishedStudyService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchUnpublishedStudyParams;
import uk.ac.ebi.spot.gwas.rest.dto.UnpublishedStudiesSortParam;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UnpublishedStudyServiceImpl implements UnpublishedStudyService {

    UnpublishedStudyRepository unpublishedStudyRepository;
    BodyOfWorkService bodyOfWorkService;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public UnpublishedStudyServiceImpl(UnpublishedStudyRepository unpublishedStudyRepository, BodyOfWorkService bodyOfWorkService) {
        this.unpublishedStudyRepository = unpublishedStudyRepository;
        this.bodyOfWorkService = bodyOfWorkService;
    }

    public Page<UnpublishedStudy> getUnpublishedStudies(SearchUnpublishedStudyParams searchUnpublishedStudyParams, Pageable pageable, String sortParam , String direction) {
        QUnpublishedStudy qUnpublishedStudy = QUnpublishedStudy.unpublishedStudy;
        QBodyOfWork qBodyOfWork = QBodyOfWork.bodyOfWork;
        Long totalElements = 0L;
        List<UnpublishedStudy> results = null;
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);
        JPQLQuery<UnpublishedStudy> unpublishedStudyJPQLQuery = jpaQuery.select(qUnpublishedStudy).distinct()
                .from(qUnpublishedStudy);

        try {
            if (searchUnpublishedStudyParams.getFirstAuthor() != null || searchUnpublishedStudyParams.getTitle() != null) {
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery
                        .innerJoin(qUnpublishedStudy.bodiesOfWork, qBodyOfWork);
            }

            if (searchUnpublishedStudyParams.getAccessionId() != null) {
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qUnpublishedStudy.accession
                        .equalsIgnoreCase(searchUnpublishedStudyParams.getAccessionId()));
            }

            if (searchUnpublishedStudyParams.getDiseaseTrait() != null) {
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qUnpublishedStudy.trait
                        .containsIgnoreCase(searchUnpublishedStudyParams.getDiseaseTrait()));
            }

            if (searchUnpublishedStudyParams.getTitle() != null) {
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qBodyOfWork.title
                        .containsIgnoreCase(searchUnpublishedStudyParams.getTitle()));
            }

            if (searchUnpublishedStudyParams.getFirstAuthor() != null) {
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qBodyOfWork.firstAuthor
                        .containsIgnoreCase(searchUnpublishedStudyParams.getFirstAuthor()));
            }

            if (searchUnpublishedStudyParams.getCohort() != null) {
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qUnpublishedStudy.cohort
                        .containsIgnoreCase(searchUnpublishedStudyParams.getCohort()));
            }
            totalElements = unpublishedStudyJPQLQuery.fetchCount();
            //List<UnpublishedStudy> results = querydsl.applyPagination(pageable, unpublishedStudyJPQLQuery).fetch();
            results = unpublishedStudyJPQLQuery.orderBy(buildSortParams(sortParam).asc() ,
                            buildDirectionSpecifier(direction, sortParam))
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        } catch (Exception ex) {
            log.info("Inside Exception in dsl query");
            log.error("Exception in dsl query" + ex.getMessage(), ex);
        } catch (Throwable ex) {
            log.info("Inside Throwable in dsl query");
            log.error("Throwable in dsl query" + ex.getMessage(), ex);
        }
        return new PageImpl<>(results, pageable, totalElements);
    }

    public Optional<UnpublishedStudy> findByAccession(String accession) {
        return unpublishedStudyRepository.findByAccession(accession);
    }


    public Page<UnpublishedStudy> findByBodyOfWork(Long bowId, Pageable pageable) {
        return bodyOfWorkService.getBodyOfWork(bowId)
                .map(bodyOfWork -> unpublishedStudyRepository.findByBodiesOfWorkId(bowId, pageable))
                .orElseThrow(() -> new EntityNotFoundException(EntityType.BODY_OF_WORKS, "id", String.valueOf(bowId)));
    }

    private NumberExpression<Integer> buildSortParams(String sortParam) {
        QUnpublishedStudy qUnpublishedStudy = QUnpublishedStudy.unpublishedStudy;
        NumberExpression<Integer> sortColumOrder = null;
        if (sortParam != null) {
            if (sortParam.equals(UnpublishedStudiesSortParam.study_accession.name())){
                sortColumOrder = new CaseBuilder()
                        .when(qUnpublishedStudy.accession.isNull())
                        .then(1)
                        .otherwise(0);
            }
            if (sortParam.equals(UnpublishedStudiesSortParam.variant_count.name())){
                sortColumOrder = new CaseBuilder()
                        .when(qUnpublishedStudy.variantCount.isNull())
                        .then(1)
                        .otherwise(0);
            }
        } else {
            sortColumOrder = new CaseBuilder()
                    .when(qUnpublishedStudy.id.isNull())
                    .then(1)
                    .otherwise(0);
        }
        return  sortColumOrder;
    }


    private OrderSpecifier<?> buildDirectionSpecifier(String direction, String sortParam) {
        QUnpublishedStudy qUnpublishedStudy = QUnpublishedStudy.unpublishedStudy;
        OrderSpecifier<?> orderSpecifier = null;
        if(sortParam != null){
            if(direction != null) {
                if(sortParam.equals(UnpublishedStudiesSortParam.study_accession.name())) {
                    orderSpecifier = direction.equals("asc") ? qUnpublishedStudy.accession.asc() : qUnpublishedStudy.accession.desc();
                }
                if(sortParam.equals(UnpublishedStudiesSortParam.variant_count.name())) {
                    orderSpecifier = direction.equals("asc") ? qUnpublishedStudy.variantCount.asc() : qUnpublishedStudy.variantCount.desc();
                }
            } else {
                orderSpecifier = qUnpublishedStudy.id.desc();
            }
        } else {
            orderSpecifier =  qUnpublishedStudy.id.desc();
        }
        return orderSpecifier;
    }
}
