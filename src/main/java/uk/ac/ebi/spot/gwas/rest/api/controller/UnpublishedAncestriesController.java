package uk.ac.ebi.spot.gwas.rest.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.model.UnpublishedAncestry;
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
    public CollectionModel<UnpublishedAncestryDTO> getUnpublishedAncestries(@PathVariable String accessionId ) {
        List<UnpublishedAncestry> unpublishedAncestries = unpublishedAncestryService.getAllUnpublishedAncestry(accessionId);
        return unpublishedAncestryDTOAssembler.toCollectionModel(unpublishedAncestries, accessionId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{accessionId}"+RestAPIConstants.API_UNPUBLISHED_ANCESTRIES + "/{ancestryId}")
    public UnpublishedAncestryDTO getUnpublishedAncestry(@PathVariable String accessionId,
                                                         @PathVariable String ancestryId) {
        UnpublishedAncestry unpublishedAncestry =  unpublishedAncestryService.getAncestry(Long.valueOf(ancestryId));
        return unpublishedAncestryDTOAssembler.toModel(unpublishedAncestry);
    }
}
