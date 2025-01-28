package uk.ac.ebi.spot.gwas.rest.api.service.impl;

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
import uk.ac.ebi.spot.gwas.model.QBodyOfWork;
import uk.ac.ebi.spot.gwas.model.QUnpublishedStudy;
import uk.ac.ebi.spot.gwas.model.UnpublishedStudy;
import uk.ac.ebi.spot.gwas.rest.api.repository.UnpublishedStudyRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.UnpublishedStudyService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchUnpublishedStudyParams;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Slf4j
@Service
public class UnpublishedStudyServiceImpl implements UnpublishedStudyService {

    UnpublishedStudyRepository unpublishedStudyRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public UnpublishedStudyServiceImpl(UnpublishedStudyRepository unpublishedStudyRepository) {
        this.unpublishedStudyRepository = unpublishedStudyRepository;
    }

    public Page<UnpublishedStudy> getUnpublishedStudies(SearchUnpublishedStudyParams searchUnpublishedStudyParams, Pageable pageable) {
        QUnpublishedStudy qUnpublishedStudy = QUnpublishedStudy.unpublishedStudy;
        QBodyOfWork qBodyOfWork = QBodyOfWork.bodyOfWork;
        Boolean isExpressionNotEmpty = false;
        Querydsl querydsl = new Querydsl(em , (new PathBuilderFactory()).create(UnpublishedStudy.class));
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);
        JPQLQuery<UnpublishedStudy> unpublishedStudyJPQLQuery = jpaQuery.select(qUnpublishedStudy).distinct()
                .from(qUnpublishedStudy);
        try {
            if(searchUnpublishedStudyParams.getFirstAuthor() != null || searchUnpublishedStudyParams.getTitle() != null) {
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery
                        .innerJoin(qUnpublishedStudy.bodiesOfWork, qBodyOfWork);
            }

            if(searchUnpublishedStudyParams.getAccessionId() != null) {
                isExpressionNotEmpty = true;
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qUnpublishedStudy.accession
                        .equalsIgnoreCase(searchUnpublishedStudyParams.getAccessionId()));
            }

            if(searchUnpublishedStudyParams.getDiseaseTrait() != null) {
                isExpressionNotEmpty = true;
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qUnpublishedStudy.trait
                        .equalsIgnoreCase(searchUnpublishedStudyParams.getDiseaseTrait()));
            }

            if(searchUnpublishedStudyParams.getTitle() != null) {
                isExpressionNotEmpty = true;
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qBodyOfWork.title
                        .containsIgnoreCase(searchUnpublishedStudyParams.getTitle()));
            }

            if(searchUnpublishedStudyParams.getFirstAuthor() != null) {
                isExpressionNotEmpty = true;
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qBodyOfWork.firstAuthor
                        .containsIgnoreCase(searchUnpublishedStudyParams.getFirstAuthor()));
            }

            if(searchUnpublishedStudyParams.getCohort() != null) {
                isExpressionNotEmpty = true;
                unpublishedStudyJPQLQuery = unpublishedStudyJPQLQuery.where(qUnpublishedStudy.cohort
                        .containsIgnoreCase(searchUnpublishedStudyParams.getCohort()));
            }
            if(isExpressionNotEmpty) {
                Long totalElements = unpublishedStudyJPQLQuery.fetchCount();
                List<UnpublishedStudy> results = querydsl.applyPagination(pageable, unpublishedStudyJPQLQuery).fetch();
                return new PageImpl<>(results, pageable, totalElements);
            }
        }catch( Exception ex) {
            log.info("Inside Exception in dsl query");
            log.error("Exception in dsl query"+ex.getMessage(),ex);
        }catch(Throwable ex) {
            log.info("Inside Throwable in dsl query");
            log.error("Throwable in dsl query"+ex.getMessage(),ex);
        }
        return unpublishedStudyRepository.findAll(pageable);

    }

    public UnpublishedStudy findByAccession(String accession) {
        return unpublishedStudyRepository.findByAccession(accession).orElse(null);
    }

    public Page<UnpublishedStudy> findByBodyOfWork(String bowId, Pageable pageable) {
        return unpublishedStudyRepository.findByBodiesOfWorkPublicationId(bowId, pageable);
    }
}
