package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.repository.EFOTraitRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.EFOTraitService;
import uk.ac.ebi.spot.gwas.rest.dto.EfoSortParam;
import uk.ac.ebi.spot.gwas.rest.dto.SearchEfoParams;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EFOTraitServiceImpl implements EFOTraitService {

    @PersistenceContext
    private EntityManager em;

    EFOTraitRepository efoTraitRepository;


    public EFOTraitServiceImpl(EFOTraitRepository efoTraitRepository) {
        this.efoTraitRepository = efoTraitRepository;
    }

    public Page<EfoTrait> getEFOTraits(SearchEfoParams searchEfoParams, Pageable pageable, String sort , String direction) {
        QEfoTrait qEfoTrait  = QEfoTrait.efoTrait;
        QStudy qStudy = QStudy.study;
        QPublication qPublication = QPublication.publication1;
        QHousekeeping qHousekeeping = QHousekeeping.housekeeping;
        List<EfoTrait> efoTraits = null;
        Long totalElements = 0L;
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);
        JPQLQuery<EfoTrait> efoTraitJPQLQuery = jpaQuery.select(qEfoTrait).distinct()
                .from(qEfoTrait);
        try {
            if(searchEfoParams.getPubmedId() != null) {
                efoTraitJPQLQuery = efoTraitJPQLQuery
                        .innerJoin(qEfoTrait.studies, qStudy)
                        .innerJoin(qStudy.publicationId, qPublication);
            }
            if(searchEfoParams.getTrait() != null) {
                efoTraitJPQLQuery = efoTraitJPQLQuery
                        .where(qEfoTrait.trait.equalsIgnoreCase(searchEfoParams.getTrait()));
            }
            if(searchEfoParams.getShortForm() != null) {
                efoTraitJPQLQuery = efoTraitJPQLQuery
                        .where(qEfoTrait.shortForm.equalsIgnoreCase(searchEfoParams.getShortForm()));
            }
            if(searchEfoParams.getUri() != null) {
                efoTraitJPQLQuery = efoTraitJPQLQuery
                        .where(qEfoTrait.uri.equalsIgnoreCase(searchEfoParams.getUri()));
            }
            if(searchEfoParams.getPubmedId() != null) {
                efoTraitJPQLQuery = efoTraitJPQLQuery
                        .where(qPublication.pubmedId.equalsIgnoreCase(searchEfoParams.getPubmedId()));
            }
            efoTraitJPQLQuery = efoTraitJPQLQuery
                    .innerJoin(qEfoTrait.studies, qStudy)
                    .innerJoin(qStudy.housekeeping, qHousekeeping)
                    .where(qHousekeeping.isPublished.eq(true))
                    .where(qHousekeeping.catalogPublishDate.isNotNull());
            totalElements = efoTraitJPQLQuery.fetchCount();
            efoTraits = efoTraitJPQLQuery
                    .orderBy(buildOrderSpecifier(direction, sort))
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }catch(Exception ex) {
            log.info("Inside Exception in dsl query");
            log.error("Exception in dsl query"+ex.getMessage(),ex);
        }catch(Throwable ex) {
            log.info("Inside Throwable in dsl query");
            log.error("Throwable in dsl query"+ex.getMessage(),ex);
        }
        log.info("Outside the QueryDSL condition");
        return new PageImpl<>(efoTraits, pageable, totalElements);
    }

    public Optional<EfoTrait> getEFOTrait(String shortForm) {
        return efoTraitRepository.findByShortForm(shortForm);
    }

    private OrderSpecifier<?> buildOrderSpecifier(String direction, String sortParam) {
        QEfoTrait qEfoTrait = QEfoTrait.efoTrait;
        OrderSpecifier<?> orderSpecifier = null;
        if(sortParam != null) {
            if(direction != null) {
                if(sortParam.equals(EfoSortParam.efo_id.name())) {
                    orderSpecifier = direction.equals("asc") ? qEfoTrait.shortForm.asc() : qEfoTrait.shortForm.desc();
                }
                if(sortParam.equals(EfoSortParam.efo_trait.name())) {
                    orderSpecifier = direction.equals("asc") ? qEfoTrait.trait.asc() : qEfoTrait.trait.desc();
                }
            } else {
                orderSpecifier = qEfoTrait.id.desc();
            }
        }  else {
            orderSpecifier = qEfoTrait.id.desc();
        }
        return orderSpecifier;
    }
}
