package uk.ac.ebi.spot.gwas.rest.api.service;

import uk.ac.ebi.spot.gwas.model.Locus;

import java.util.List;

public interface LocusService {

    List<Locus> findLociByAssociationId(Long associationId);

    Locus findByLocusId(Long locusId);
}
