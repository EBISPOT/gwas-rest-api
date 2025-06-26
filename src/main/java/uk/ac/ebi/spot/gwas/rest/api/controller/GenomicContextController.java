package uk.ac.ebi.spot.gwas.rest.api.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.GenomicContext;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.GenomicContextDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.GenomicContextService;
import uk.ac.ebi.spot.gwas.rest.dto.GenomicContextDTO;

import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_SNPS)
public class GenomicContextController {

    @Autowired
    GenomicContextService genomicContextService;

    @Autowired
    GenomicContextDtoAssembler genomicContextDtoAssembler;

    @GetMapping(value = "/{rs_id}" + RestAPIConstants.API_GENOMIC_CONTEXTS, produces = MediaType.APPLICATION_JSON_VALUE)
    public CollectionModel<GenomicContextDTO> getGenomicContexts(@PathVariable (name = "rs_id") String rsId,
                                                                 @RequestParam(required = false)  String sort,
                                                                 @RequestParam(required = false)   String direction) {
       List<GenomicContext> genomicContexts = genomicContextService.findByRsid(rsId, sort, direction);
       return genomicContextDtoAssembler.toCollectionModel(genomicContexts, rsId);
    }

    @GetMapping(value = "/{rs_id}" + RestAPIConstants.API_GENOMIC_CONTEXTS + "/{genomicContext_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenomicContextDTO> getGenomicContext(@PathVariable (name = "rs_id") String rsId ,@PathVariable (name = "genomicContext_id") String genomicContextId) {
        return genomicContextService.findByGenomicContextId(genomicContextId)
                .map(genomicContextDtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.GENOMIC_CONTEXT, "id", genomicContextId));

       // confirm rsid or genomicContextId GenomicContext genomicContext = genomicContextService.findByGenomicContextId(genomicContextId);
       // return genomicContextDtoAssembler.toModel(genomicContext, genomicContextId);
    }
}
