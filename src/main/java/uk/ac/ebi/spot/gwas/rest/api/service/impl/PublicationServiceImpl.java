package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.Publication;
import uk.ac.ebi.spot.gwas.model.QHousekeeping;
import uk.ac.ebi.spot.gwas.model.QPublication;
import uk.ac.ebi.spot.gwas.model.QStudy;
import uk.ac.ebi.spot.gwas.rest.api.repository.PublicationRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.PublicationService;
import uk.ac.ebi.spot.gwas.rest.dto.PublicationSortParam;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PublicationServiceImpl implements PublicationService {

    PublicationRepository publicationRepository;

    @PersistenceContext
    private EntityManager em;

    public PublicationServiceImpl(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    @Override
    public Page<Publication> findPublications(String pubmedId, String title,String firstAuthor, Pageable pageable, String sortParam, String direction) {

        QPublication qPublication = QPublication.publication1;
        QStudy qStudy = QStudy.study;
        QHousekeeping qHousekeeping = QHousekeeping.housekeeping;
        Long totalElements = 0L;
        List<Publication> results = new ArrayList<>();
        JPAQueryFactory jpaQuery = new JPAQueryFactory(em);
        JPQLQuery<Publication> publicationJPQLQuery = jpaQuery.select(qPublication).distinct()
                .from(qPublication);
        try {
            if (pubmedId != null) {
                publicationJPQLQuery.where(qPublication.pubmedId.eq(pubmedId));
            }
            if (firstAuthor != null) {
                publicationJPQLQuery.where(qPublication.firstAuthor.fullname.containsIgnoreCase(firstAuthor));
            }
            if (title != null) {
                publicationJPQLQuery.where(qPublication.title.containsIgnoreCase(title));
            }

            publicationJPQLQuery = publicationJPQLQuery
                    .innerJoin(qPublication.studies, qStudy)
                    .innerJoin(qStudy.housekeeping, qHousekeeping)
                    .where(qHousekeeping.isPublished.eq(true))
                    .where(qHousekeeping.catalogPublishDate.isNotNull());

            totalElements = publicationJPQLQuery.fetchCount();

            results = publicationJPQLQuery.orderBy(buildDirectionSpecifier(direction, sortParam))
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


    private OrderSpecifier<?> buildDirectionSpecifier(String direction, String sortParam) {
        QPublication qPublication = QPublication.publication1;
        OrderSpecifier<?>  orderSpecifier = null;

        if(sortParam != null) {
            if (direction != null ) {
                if(sortParam.equals(PublicationSortParam.pubmed_id.name())) {
                    orderSpecifier = direction.equals("asc") ? Expressions.stringTemplate("function('TO_NUMBER', {0})", qPublication.pubmedId).asc()
                            : Expressions.stringTemplate("function('TO_NUMBER', {0})", qPublication.pubmedId).desc();
                }
                if(sortParam.equals(PublicationSortParam.publication_date.name())) {
                    orderSpecifier = direction.equals("asc") ? qPublication.publicationDate.asc() : qPublication.publicationDate.desc();
                }
            } else {
                orderSpecifier = qPublication.id.desc();

            }
        } else {
            orderSpecifier = qPublication.id.desc();
        }
        return orderSpecifier;

    }

    public Optional<Publication> findPublicationByPmid(String pmid) {
       return publicationRepository.findByPubmedIdEquals(pmid);
    }


}
