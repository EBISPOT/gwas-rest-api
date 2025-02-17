package uk.ac.ebi.spot.gwas.rest.api.service;

import uk.ac.ebi.spot.gwas.rest.dto.RestResponseResult;

public interface EnsemblRestHistoryService {

    RestResponseResult getHistoryByTypeParamAndVersion(String type, String param, String eRelease);
}
