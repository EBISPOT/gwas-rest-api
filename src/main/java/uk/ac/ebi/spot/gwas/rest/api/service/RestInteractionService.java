package uk.ac.ebi.spot.gwas.rest.api.service;

import uk.ac.ebi.spot.gwas.ensembl.Variant;

public interface RestInteractionService {

    Variant getEnsemblResponse(String rsId);
}
