package uk.ac.ebi.spot.gwas.rest.api.service;

import uk.ac.ebi.spot.gwas.model.Locus;

import java.util.List;
import java.util.Optional;

public interface LocusService {

    List<Locus> findLociByAssociationId(Long associationId);

    Optional<Locus> findByLocusId(Long locusId);
}
