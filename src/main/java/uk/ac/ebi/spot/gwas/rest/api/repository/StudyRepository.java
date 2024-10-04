package uk.ac.ebi.spot.gwas.rest.api.repository;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.ac.ebi.spot.gwas.model.Study;

public interface StudyRepository extends JpaRepository<Study, Long>, QuerydslPredicateExecutor<Study> {


    Page<Study> findAll(Predicate predicate, Pageable pageable);
}
