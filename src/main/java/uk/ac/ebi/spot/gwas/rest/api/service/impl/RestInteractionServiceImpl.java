package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.ensembl.Variant;
import uk.ac.ebi.spot.gwas.rest.api.config.RestAPIConfiguration;
import uk.ac.ebi.spot.gwas.rest.api.service.APIService;
import uk.ac.ebi.spot.gwas.rest.api.service.RestInteractionService;

import java.util.Optional;

@Slf4j
@Service
public class RestInteractionServiceImpl implements RestInteractionService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    APIService apiService;

    @Autowired
    RestAPIConfiguration restAPIConfiguration;

    public Variant getEnsemblResponse(String rsId) {
        String uri  = String.format("%s/%s", restAPIConfiguration.getEnsemblServerUrl(), rsId );
        log.debug("Variation url is {}", uri);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Optional<ResponseEntity<Variant>> optionalEntity = apiService.getRequestVariant(uri);
        Variant variant = optionalEntity
                .map(response -> mapper.convertValue(response.getBody(), Variant.class))
                .orElseGet(Variant::new);
        return variant;
    }
}
