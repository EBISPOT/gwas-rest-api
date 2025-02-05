package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.http.ResponseEntity;
import uk.ac.ebi.spot.gwas.ensembl.Variant;

import java.util.Optional;

public interface APIService {

    Optional<ResponseEntity<Variant>> getRequestVariant(String uri);
}
