package uk.ac.ebi.spot.gwas.rest.api.controller;

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
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.model.Association;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.AssociationDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.AssociationService;
import uk.ac.ebi.spot.gwas.rest.dto.AssociationDTO;
import uk.ac.ebi.spot.gwas.rest.dto.SearchAssociationParams;

import javax.persistence.EntityNotFoundException;

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
                                                      @SortDefault(sort = "id", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable) {
      Page<Association> associations = associationService.getAssociations(pageable, searchAssociationParams);
      return pagedResourcesAssembler.toModel(associations, associationDtoAssembler);
    }

    @GetMapping(value = "/{associationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AssociationDTO getAssociation(@PathVariable String associationId) {
        Association association = associationService.getAssociation(Long.valueOf(associationId));
        if(association != null) {
            return associationDtoAssembler.toModel(association);
        } else {
            throw new EntityNotFoundException(associationId);
        }
    }

}
