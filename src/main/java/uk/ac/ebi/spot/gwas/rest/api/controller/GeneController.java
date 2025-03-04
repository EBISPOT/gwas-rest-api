package uk.ac.ebi.spot.gwas.rest.api.controller;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.GeneSolrDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim.GeneSolrDto;
import uk.ac.ebi.spot.gwas.rest.api.service.GeneService;
import uk.ac.ebi.spot.gwas.rest.dto.GeneDTO;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_GENES)
public class GeneController {

    private final GeneService geneService;
    private final GeneSolrDtoAssembler geneSolrDtoAssembler;
    private final PagedResourcesAssembler<GeneSolrDto> pagedResourcesAssembler;

    public GeneController(GeneService geneService, GeneSolrDtoAssembler geneSolrDtoAssembler, PagedResourcesAssembler pagedResourcesAssembler) {
        this.geneService = geneService;
        this.geneSolrDtoAssembler = geneSolrDtoAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping
    public PagedModel<GeneDTO> getGenes(@ParameterObject Pageable pageable) {
        Page<GeneSolrDto> genes = geneService.getGenes(pageable);
        return pagedResourcesAssembler.toModel(genes, geneSolrDtoAssembler);
    }

    @GetMapping(value = "/{geneName}")
    public GeneDTO getGeneByName(@PathVariable String geneName) {
        GeneSolrDto geneSolrDto = geneService.getGeneByName(geneName);
        return geneSolrDtoAssembler.toModel(geneSolrDto);
    }
}
