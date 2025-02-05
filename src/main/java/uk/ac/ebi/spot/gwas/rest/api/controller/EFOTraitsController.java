package uk.ac.ebi.spot.gwas.rest.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.model.EfoTrait;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.EFOTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.EFOTraitService;
import uk.ac.ebi.spot.gwas.rest.dto.EFOTraitDTO;
import uk.ac.ebi.spot.gwas.rest.dto.EFOWrapperDTO;
import uk.ac.ebi.spot.gwas.rest.dto.SearchEfoParams;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_EFO_TRAITS)
public class EFOTraitsController {

    @Autowired
    EFOTraitService efoTraitService;

    @Autowired
    EFOTraitDtoAssembler efoTraitDtoAssembler;

    @Autowired
    PagedResourcesAssembler<EfoTrait> pagedResourcesAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<EFOTraitDTO> getEFOTraits(SearchEfoParams searchEfoParams, Pageable pageable) {
      Page<EfoTrait> efoTraits = efoTraitService.getEFOTraits(searchEfoParams, pageable);
      return pagedResourcesAssembler.toModel(efoTraits, efoTraitDtoAssembler);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{shortForm}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EFOTraitDTO getEFOTraitsDTO(@PathVariable String shortForm) {
      EfoTrait efoTrait =  efoTraitService.getEFOTrait(shortForm);
      if(efoTrait != null) {
         return efoTraitDtoAssembler.toModel(efoTrait);
      } else {
          throw new EntityNotFoundException(shortForm);
      }
    }
}
