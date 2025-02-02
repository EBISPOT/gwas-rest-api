package uk.ac.ebi.spot.gwas.rest.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.model.Publication;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.PublicationDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.PublicationService;
import uk.ac.ebi.spot.gwas.rest.dto.PublicationDto;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_PUBLICATIONS)
public class PublicationsController {

    @Autowired
    PagedResourcesAssembler<Publication> pagedResourcesAssembler;

    @Autowired
    PublicationService publicationService;

    @Autowired
    PublicationDtoAssembler publicationDtoAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<PublicationDto> getPublications(@RequestParam(value = "pubmedId", required = false) String pubmedId,
                                                      @RequestParam(value = "title", required = false) String title,
                                                      Pageable pageable) {
        Page<Publication> publications = publicationService.findPublications(pubmedId, title, pageable);
        return pagedResourcesAssembler.toModel(publications, publicationDtoAssembler);

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{pubmedId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PublicationDto getPublication(@PathVariable("pubmedId") String pubmedId) {
        Publication publication = publicationService.findPublicationByPmid(pubmedId);
        return publicationDtoAssembler.toModel(publication);
    }

}
