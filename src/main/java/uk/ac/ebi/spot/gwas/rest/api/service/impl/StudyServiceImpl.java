package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.repository.StudyRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.StudyService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchStudyParams;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class StudyServiceImpl implements StudyService {

    @PersistenceContext
    private EntityManager em;

    StudyRepository studyRepository;

    @Autowired
    public StudyServiceImpl(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

   public  Page<Study> getStudies(Pageable pageable, SearchStudyParams searchStudyParams) {
        QStudy qStudy = QStudy.study;
        QEfoTrait qEfoTrait = QEfoTrait.efoTrait;
        QDiseaseTrait qDiseaseTrait =  QDiseaseTrait.diseaseTrait;
        QPublication qPublication = QPublication.publication1;
        Boolean isExpressionNotEmpty = false;
        BooleanExpression finalExpression = null;
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);

        if(searchStudyParams != null) {
            if (searchStudyParams.getAccessionId() != null) {
                if (!isExpressionNotEmpty) {
                    finalExpression = qStudy.accessionId.eq(searchStudyParams.getAccessionId());
                    isExpressionNotEmpty = true;
                } else {
                    finalExpression = finalExpression.and(qStudy.accessionId.eq(searchStudyParams.getAccessionId()));
                }
            }
            if (searchStudyParams.getFullPvalueSet() != null) {
                if (!isExpressionNotEmpty) {
                    finalExpression = qStudy.fullPvalueSet.eq(Boolean.getBoolean(searchStudyParams.getFullPvalueSet()));
                    isExpressionNotEmpty = true;
                } else
                    finalExpression = finalExpression.and(qStudy.fullPvalueSet.eq(Boolean.getBoolean(searchStudyParams.getFullPvalueSet())));
            }

            if (searchStudyParams.getShortForm() != null) {
                QEfoTrait qEfoTrait1 = (QEfoTrait) jpaQuery.from(qEfoTrait).where
                                (qEfoTrait.shortForm.eq(searchStudyParams.getShortForm()))
                        .fetchOne();
                if (!isExpressionNotEmpty) {
                    finalExpression = qStudy.efoTraits.contains(qEfoTrait1);
                    isExpressionNotEmpty = true;
                } else {
                    finalExpression = finalExpression.and(qStudy.efoTraits.contains(qEfoTrait1));
                }
            }

            if (searchStudyParams.getEfoTrait() != null) {
                QEfoTrait qEfoTrait1 = (QEfoTrait) jpaQuery.from(qEfoTrait).where
                                (qEfoTrait.trait.eq(searchStudyParams.getEfoTrait()))
                        .fetchOne();
                if (!isExpressionNotEmpty) {
                    finalExpression = qStudy.efoTraits.contains(qEfoTrait1);
                    isExpressionNotEmpty = true;
                } else {
                    finalExpression = finalExpression.and(qStudy.efoTraits.contains(qEfoTrait1));
                }
            }


            if (searchStudyParams.getDiseaseTrait() != null) {
                QDiseaseTrait qDiseaseTrait1 = (QDiseaseTrait) jpaQuery.from(qDiseaseTrait).where
                                (qDiseaseTrait.trait.eq(searchStudyParams.getDiseaseTrait()))
                        .fetchOne();
                if (!isExpressionNotEmpty) {
                    finalExpression = qStudy.diseaseTrait.eq(qDiseaseTrait1);
                    isExpressionNotEmpty = true;
                } else {
                    finalExpression = finalExpression.and(qStudy.diseaseTrait.eq(qDiseaseTrait1));
                }

            }

            if (searchStudyParams.getPubmedId() != null) {
                QPublication qPublication1 = (QPublication) jpaQuery.from(qPublication).where
                                (qPublication.pubmedId.eq(searchStudyParams.getPubmedId()))
                        .fetchOne();
                if (!isExpressionNotEmpty) {
                    finalExpression = qStudy.publicationId.eq(qPublication1);
                    isExpressionNotEmpty = true;
                } else {
                    finalExpression = finalExpression.and(qStudy.publicationId.eq(qPublication1));
                }
            }

            if (searchStudyParams.getUserRequested() != null) {
                if (!isExpressionNotEmpty) {
                    finalExpression = qStudy.userRequested.eq(Boolean.getBoolean(searchStudyParams.getUserRequested()));
                    isExpressionNotEmpty = true;
                } else {
                    finalExpression = finalExpression.and(qStudy.userRequested.eq(Boolean.getBoolean(searchStudyParams.getUserRequested())));
                }
            }
        } else {
            studyRepository.findAll(pageable);
        }


          return  studyRepository.findAll(finalExpression, pageable);


    }



}


