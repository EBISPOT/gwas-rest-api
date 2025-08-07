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
    @GetMapping(value = "/{accession_id}" + RestAPIConstants.API_ANCESTRIES, produces = MediaType.APPLICATION_JSON_VALUE)
    public CollectionModel<AncestryDTO> getAncestries(@PathVariable (name = "accession_id") String accessionId,
                                                      @RequestParam(required = false)  String sort,
                                                      @RequestParam(required = false)   String direction) {
        List<Ancestry> ancestries = ancestryService.getAncestriesForStudy(accessionId, sort, direction);
        return ancestryDtoAssembler.toCollectionModel(ancestries, accessionId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{accession_id}" + RestAPIConstants.API_ANCESTRIES +"/{ancestry_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AncestryDTO getAncestry(@PathVariable (name = "accession_id") String accessionId, @PathVariable (name = "ancestry_id")  String ancestryId) {
        Ancestry ancestry = ancestryService.getAncestry(Long.valueOf(ancestryId));
        return ancestryDtoAssembler.toModel(ancestry, accessionId);
    }

}
