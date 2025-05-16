package uk.ac.ebi.spot.gwas.rest.api.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.model.Ancestry;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.AncestryDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.AncestryService;
import uk.ac.ebi.spot.gwas.rest.dto.AncestryDTO;

import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_STUDIES)
public class AncestryController {

    @Autowired
    AncestryService ancestryService;

    @Autowired
    AncestryDtoAssembler ancestryDtoAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{accessionId}" + RestAPIConstants.API_ANCESTRIES, produces = MediaType.APPLICATION_JSON_VALUE)
    public CollectionModel<AncestryDTO> getAncestries(@PathVariable @Parameter(name = "accessionId") String accessionId) {
        List<Ancestry> ancestries = ancestryService.getAncestriesForStudy(accessionId);
        return ancestryDtoAssembler.toCollectionModel(ancestries, accessionId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{accessionId}" + RestAPIConstants.API_ANCESTRIES +"/{ancestryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AncestryDTO getAncestry(@PathVariable @Parameter(name = "accessionId") String accessionId, @PathVariable @Parameter(name = "ancestryId")  String ancestryId) {
        Ancestry ancestry = ancestryService.getAncestry(Long.valueOf(ancestryId));
        return ancestryDtoAssembler.toModel(ancestry, accessionId);
    }

}
