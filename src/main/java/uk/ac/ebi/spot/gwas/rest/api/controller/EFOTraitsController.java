package uk.ac.ebi.spot.gwas.rest.api.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.EfoTrait;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.EFOTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.EFOTraitService;
import uk.ac.ebi.spot.gwas.rest.dto.EFOTraitDTO;
import uk.ac.ebi.spot.gwas.rest.dto.SearchEfoParams;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_EFO_TRAITS)
@Tag(name = "efo-traits")
public class EFOTraitsController {

    @Autowired
    EFOTraitService efoTraitService;

    @Autowired
    EFOTraitDtoAssembler efoTraitDtoAssembler;

    @Autowired
    PagedResourcesAssembler<EfoTrait> pagedResourcesAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<EFOTraitDTO> getEFOTraits(@RequestParam SearchEfoParams searchEfoParams, @ParameterObject Pageable pageable) {
      Page<EfoTrait> efoTraits = efoTraitService.getEFOTraits(searchEfoParams, pageable);
      return pagedResourcesAssembler.toModel(efoTraits, efoTraitDtoAssembler);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{efoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EFOTraitDTO> getEFOTraitsDTO(@PathVariable  @Parameter(name = "efoId",
            description = "primary identifier of EFo Traits table <br/> <br/>" +
                    "<i> Example </i> : 123456") String efoId) {
        return efoTraitService.getEFOTrait(efoId)
                .map(efoTraitDtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.EFO_TRAIT, "Id", efoId));
    }
}
