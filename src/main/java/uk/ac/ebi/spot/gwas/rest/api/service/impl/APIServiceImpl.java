package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.gwas.ensembl.Variant;
import uk.ac.ebi.spot.gwas.rest.api.service.APIService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class APIServiceImpl implements APIService {

    @Autowired
    RestTemplate restTemplate;

    public Optional<ResponseEntity<Variant>> getRequestVariant(String uri) {
        log.info("Calling: {}", uri);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Variant> out = null;
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.TEXT_HTML);
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.ALL);
        headers.setAccept(mediaTypes);
        ResponseEntity<String> response = null;
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        try {
            //response = restTemplate.getForEntity(uri, Object.class);
            out = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference
                    <Variant>() {
            });
            log.info("Ressponse body in getRequest() {}",out.getBody());
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                log.warn("warning: too many request {} retrying ...", uri);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
                return this.getRequestVariant(uri);
            }
            else if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                log.debug("Inside Bad request block");
                log.debug("Variant error is {}", e.getResponseBodyAsString());
                out = new ResponseEntity<>(new Variant(e.getResponseBodyAsString()), e.getStatusCode());
            }
            else{
                out = new ResponseEntity<>(new Variant(e.getResponseBodyAsString()), e.getStatusCode());
            }
        }
        return Optional.of(out);
    }

}
