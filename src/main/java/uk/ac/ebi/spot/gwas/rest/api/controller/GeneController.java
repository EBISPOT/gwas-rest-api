package uk.ac.ebi.spot.gwas.rest.api.controller;

import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.Gene;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.GeneDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.GeneSolrDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim.GeneSolrDto;
import uk.ac.ebi.spot.gwas.rest.api.service.GeneService;
import uk.ac.ebi.spot.gwas.rest.dto.GeneDTO;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_GENES)
public class GeneController {

    private final GeneService geneService;
    private final GeneSolrDtoAssembler geneSolrDtoAssembler;

    public GeneController(GeneService geneService, GeneSolrDtoAssembler geneSolrDtoAssembler) {
        this.geneService = geneService;
        this.geneSolrDtoAssembler = geneSolrDtoAssembler;
    }

    @GetMapping(value = "/{geneName}")
    public GeneDTO getGeneByName(@PathVariable String geneName) {
        GeneSolrDto geneSolrDto = geneService.getGeneByName(geneName);
        return geneSolrDtoAssembler.toModel(geneSolrDto);
    }

    @GetMapping
    public Object searchGenes(@RequestParam String query) {
        return null;
    }
}
