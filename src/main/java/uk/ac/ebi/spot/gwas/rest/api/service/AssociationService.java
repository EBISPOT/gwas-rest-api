package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.model.Association;
import uk.ac.ebi.spot.gwas.model.DiseaseTrait;
import uk.ac.ebi.spot.gwas.rest.dto.SearchAssociationParams;

import java.util.List;
import java.util.Optional;

public interface AssociationService {

   Page<Association> getAssociations(Pageable pageable, SearchAssociationParams searchAssociationParams);

   List<DiseaseTrait> getDiseaseTraits(Long associationId);

   Optional<Association> getAssociation(Long associationId);
}
