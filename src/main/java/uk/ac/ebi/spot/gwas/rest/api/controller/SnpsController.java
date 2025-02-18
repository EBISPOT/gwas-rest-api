package uk.ac.ebi.spot.gwas.rest.api.controller;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.model.SingleNucleotidePolymorphism;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.SnpDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.SnpService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchSnpParams;
import uk.ac.ebi.spot.gwas.rest.dto.SingleNucleotidePolymorphismDTO;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_SNPS)
public class SnpsController {

    @Autowired
    SnpService snpService;

    @Autowired
    SnpDtoAssembler snpDtoAssembler;

    @Autowired
    PagedResourcesAssembler<SingleNucleotidePolymorphism> pagedResourcesAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<SingleNucleotidePolymorphismDTO> getSnps(@RequestParam SearchSnpParams searchParams, @ParameterObject Pageable pageable) {
       Page<SingleNucleotidePolymorphism> snps = snpService.getSnps(searchParams, pageable);
       return pagedResourcesAssembler.toModel(snps, snpDtoAssembler);
    }

    @GetMapping(value = "/{rsId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleNucleotidePolymorphismDTO getSingleNucleotidePolymorphism(@PathVariable String rsId) {
        SingleNucleotidePolymorphism singleNucleotidePolymorphism = snpService.getSnp(rsId);
        if(singleNucleotidePolymorphism != null) {
            return snpDtoAssembler.toModel(singleNucleotidePolymorphism);
        }
         throw new EntityNotFoundException(rsId);
    }
}
