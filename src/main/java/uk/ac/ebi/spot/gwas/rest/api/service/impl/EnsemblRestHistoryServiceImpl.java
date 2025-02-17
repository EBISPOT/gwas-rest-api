package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.EnsemblRestcallHistory;
import uk.ac.ebi.spot.gwas.rest.api.repository.EnsemblRestcallHistoryRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.EnsemblRestHistoryService;
import uk.ac.ebi.spot.gwas.rest.dto.RestResponseResult;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EnsemblRestHistoryServiceImpl implements EnsemblRestHistoryService {

    EnsemblRestcallHistoryRepository ensemblRestcallHistoryRepository;

    @Autowired
    public EnsemblRestHistoryServiceImpl(EnsemblRestcallHistoryRepository ensemblRestcallHistoryRepository) {
        this.ensemblRestcallHistoryRepository = ensemblRestcallHistoryRepository;
    }

    @Override
    public RestResponseResult getHistoryByTypeParamAndVersion(String type, String param, String eRelease) {
        RestResponseResult restResponseResult = null;
        try {
            Optional<List<EnsemblRestcallHistory>> historyList = ensemblRestcallHistoryRepository.findByRequestTypeAndEnsemblParamAndEnsemblVersion(type, param, eRelease);
            Optional<EnsemblRestcallHistory> optional = historyList.map(hist -> hist.stream().findFirst()).orElse(Optional.empty());
            if (optional.isPresent()) {
                log.info("The restHistory exists");
                EnsemblRestcallHistory result = optional.get();
                restResponseResult = new RestResponseResult();
                restResponseResult.setUrl(result.getEnsemblUrl());
                String restApiError = result.getEnsemblError();
                if (restApiError != null && !restApiError.isEmpty()) {
                    restResponseResult.setError(restApiError);
                } else {
                    restResponseResult.setRestResult(result.getEnsemblResponse());
                }

            }
        } catch (Exception ex) {
            log.error("Exception in fetching Ensembl history" + ex.getMessage(), ex);
        }
        return restResponseResult;
    }
}
