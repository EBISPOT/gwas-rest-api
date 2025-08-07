package uk.ac.ebi.spot.gwas.rest.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.exception.ErrorResponse;
import uk.ac.ebi.spot.gwas.model.Association;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.AssociationDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.AssociationService;
import uk.ac.ebi.spot.gwas.rest.dto.AssociationDTO;
import uk.ac.ebi.spot.gwas.rest.dto.SearchAssociationParams;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_ASSOCIATIONS)
public class AssociationController {

    @Autowired
    AssociationService associationService;

    @Autowired
    AssociationDtoAssembler associationDtoAssembler;

    @Autowired
    PagedResourcesAssembler<Association> pagedResourcesAssembler;


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<AssociationDTO> getAssociations(@RequestParam SearchAssociationParams searchAssociationParams,
                                                      @RequestParam(required = false)  String sort,
                                                      @RequestParam(required = false)   String direction,
                                                      @SortDefault(sort = "id", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable) {
        Page<Association> associations = associationService.getAssociations(pageable, searchAssociationParams, sort, direction);
        return pagedResourcesAssembler.toModel(associations, associationDtoAssembler);
    }

    @GetMapping(value = "/{association_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AssociationDTO> getAssociation(@PathVariable  (name = "association_id")  String associationId) {
        return associationService.getAssociation(Long.valueOf(associationId))
                .map(associationDtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.ASSOCIATIONS, "Id", associationId));


    }

}
