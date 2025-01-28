package uk.ac.ebi.spot.gwas.rest.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;


@Slf4j
@ControllerAdvice(annotations = RestController.class)
public class ExceptionHandlerAdvice {


    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {

        log.error("EntityNotFoundException :"+e.getLocalizedMessage(),e);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {

        log.error("Exception :"+e.getLocalizedMessage(),e);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.NOT_FOUND);
    }
}
