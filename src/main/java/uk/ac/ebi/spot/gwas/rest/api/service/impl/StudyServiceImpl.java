package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
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
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.StudyService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchStudyParams;
import uk.ac.ebi.spot.gwas.rest.projection.StudyProjection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

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
    public  Page<Study> getStudies(Pageable pageable, SearchStudyParams searchStudyParams) {

        Page<StudyProjection> studyProjections = null;
        QStudy qStudy = QStudy.study;
        QEfoTrait qEfoTrait = QEfoTrait.efoTrait;
        QDiseaseTrait qDiseaseTrait =  QDiseaseTrait.diseaseTrait;
        QPublication qPublication = QPublication.publication1;
        Boolean isExpressionNotEmpty = false;
        BooleanExpression finalExpression = null;
        Querydsl querydsl = new Querydsl(em , (new PathBuilderFactory()).create(Study.class));
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);
        JPQLQuery<Study> studyJPQLQuery = jpaQuery.select(qStudy).distinct()
                .from(qStudy);
        log.info("searchStudyParams {}", searchStudyParams);
        log.info("Inside searchStudyParams not null block");
        try {


            if (searchStudyParams.getEfoTrait() != null || searchStudyParams.getShortForm() != null) {
                studyJPQLQuery =  studyJPQLQuery
                        .innerJoin(qStudy.efoTraits, qEfoTrait)
                        .fetchJoin();
            }

            if (searchStudyParams.getDiseaseTrait() != null) {
                studyJPQLQuery = studyJPQLQuery.
                        innerJoin(qStudy.diseaseTrait, qDiseaseTrait)
                        .fetchJoin();
            }

            if (searchStudyParams.getPubmedId() != null) {
                isExpressionNotEmpty = true;
                studyJPQLQuery = studyJPQLQuery.
                        innerJoin(qStudy.publicationId, qPublication)
                        .fetchJoin();
            }

            if (searchStudyParams.getShortForm() != null) {
                isExpressionNotEmpty = true;
                studyJPQLQuery = studyJPQLQuery.where(qEfoTrait.shortForm.equalsIgnoreCase(searchStudyParams.getShortForm()));
            }

            if (searchStudyParams.getEfoTrait() != null) {
                isExpressionNotEmpty = true;
                studyJPQLQuery = studyJPQLQuery.where(qEfoTrait.trait.equalsIgnoreCase(searchStudyParams.getEfoTrait()));
            }

            if (searchStudyParams.getDiseaseTrait() != null) {
                isExpressionNotEmpty = true;
                studyJPQLQuery  =  studyJPQLQuery.where(qDiseaseTrait.trait.equalsIgnoreCase(searchStudyParams.getDiseaseTrait()));
            }


            if (searchStudyParams.getPubmedId() != null) {
                isExpressionNotEmpty = true;
                studyJPQLQuery = studyJPQLQuery.where(qPublication.pubmedId.eq(searchStudyParams.getPubmedId()));
            }

            if (searchStudyParams.getAccessionId() != null) {
                isExpressionNotEmpty = true;
                studyJPQLQuery = studyJPQLQuery.where(qStudy.accessionId.equalsIgnoreCase(searchStudyParams.getAccessionId()));
            }

            if (searchStudyParams.getFullPvalueSet() != null) {
                isExpressionNotEmpty = true;
                studyJPQLQuery = studyJPQLQuery.where(qStudy.fullPvalueSet.eq(searchStudyParams.getFullPvalueSet()));
            }

            if (searchStudyParams.getUserRequested() != null) {
                isExpressionNotEmpty = true;
                studyJPQLQuery = studyJPQLQuery.where(qStudy.userRequested.eq(searchStudyParams.getUserRequested()));
            }

            if (isExpressionNotEmpty) {
                Long totalElements = studyJPQLQuery.fetchCount();
                List<Study> results = querydsl.applyPagination(pageable, studyJPQLQuery).fetch();

                return new PageImpl<>(results, pageable, totalElements);
               // return studyRepository.findAll(finalExpression, pageable);
            }
        }catch( Exception ex) {
            log.info("Inside Exception in dsl query");
            log.error("Exception in dsl query"+ex.getMessage(),ex);
        }catch(Throwable ex) {
            log.info("Inside Throwable in dsl query");
            log.error("Throwable in dsl query"+ex.getMessage(),ex);
        }

        return studyRepository.findAll(pageable);

      /*List<Study> studies = studyRepository.findByCustomQuery(searchStudyParams.getAccessionId(),
                searchStudyParams.getFullPvalueSet(),
                searchStudyParams.getShortForm(),
                searchStudyParams.getEfoTrait(),
                searchStudyParams.getDiseaseTrait(),
                searchStudyParams.getPubmedId(),
                searchStudyParams.getUserRequested());
      log.info("Studies size {}", studies.size()); */
       /* try {
            log.info("Inside getStudies()");
            studyProjections = studyRepository.findByCustomQuery(searchStudyParams.getAccessionId(),
                    searchStudyParams.getFullPvalueSet(),
                    searchStudyParams.getShortForm(),
                    searchStudyParams.getEfoTrait(),
                    searchStudyParams.getDiseaseTrait(),
                    searchStudyParams.getPubmedId(),
                    searchStudyParams.getUserRequested(),
                    pageable);

        } catch(Exception ex) {
            log.info("Inside getStudies() exception");
            log.error("Exception in custom query"+ex.getMessage(),ex);
        } catch(Throwable ex) {
            log.info("Inside getStudies() Throwable");
            log.error("Error in custom query"+ex.getMessage(),ex);
        }*/

    }


    public Study getStudy(Long studyId) {
        return studyRepository.findById(studyId).orElse(null);
    }



}


