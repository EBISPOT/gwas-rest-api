package uk.ac.ebi.spot.gwas.rest.api.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.UnpublishedAncestry;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.UnpublishedAncestryDTOAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.UnpublishedAncestryService;
import uk.ac.ebi.spot.gwas.rest.dto.UnpublishedAncestryDTO;

import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_UNPUBLISHED_STUDIES)
public class UnpublishedAncestriesController {

    @Autowired
    UnpublishedAncestryService unpublishedAncestryService;

    @Autowired
    UnpublishedAncestryDTOAssembler unpublishedAncestryDTOAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{accessionId}"+RestAPIConstants.API_UNPUBLISHED_ANCESTRIES)
    public CollectionModel<UnpublishedAncestryDTO> getUnpublishedAncestries(@PathVariable @Parameter(name = "accessionId") String accessionId ) {
        List<UnpublishedAncestry> unpublishedAncestries = unpublishedAncestryService.getAllUnpublishedAncestry(accessionId);
        return unpublishedAncestryDTOAssembler.toCollectionModel(unpublishedAncestries, accessionId);
    }

    @GetMapping(value = "/{accessionId}"+RestAPIConstants.API_UNPUBLISHED_ANCESTRIES + "/{ancestryId}")
    public ResponseEntity<UnpublishedAncestryDTO> getUnpublishedAncestry(@PathVariable @Parameter(name = "accessionId") String accessionId,
                                                                         @PathVariable @Parameter(name = "ancestryId") String ancestryId) {
        return unpublishedAncestryService.getAncestry(Long.valueOf(ancestryId))
                .map(unpublishedAncestryDTOAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.UNPUBLISHED_ANCESTRY, "Id", ancestryId));

    }
}
