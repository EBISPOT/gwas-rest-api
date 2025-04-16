package uk.ac.ebi.spot.gwas.rest.api.controller;

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
import uk.ac.ebi.spot.gwas.model.Publication;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.PublicationDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.PublicationService;
import uk.ac.ebi.spot.gwas.rest.dto.PublicationDto;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_PUBLICATIONS)
@Tag(name = "publications")
public class PublicationsController {

    @Autowired
    PagedResourcesAssembler<Publication> pagedResourcesAssembler;

    @Autowired
    PublicationService publicationService;

    @Autowired
    PublicationDtoAssembler publicationDtoAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<PublicationDto> getPublications(@RequestParam(value = "pubmed_id", required = false) String pubmedId,
                                                      @RequestParam(value = "title", required = false) String title,
                                                      @RequestParam(value = "first_author", required = false) String firstAuthor,
                                                      @ParameterObject Pageable pageable) {
        Page<Publication> publications = publicationService.findPublications(pubmedId, title, firstAuthor, pageable);
        return pagedResourcesAssembler.toModel(publications, publicationDtoAssembler);

    }

    @GetMapping(value = "/{pubmedId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getPublication(@PathVariable String pubmedId) {
        return publicationService.findPublicationByPmid(pubmedId)
                .map(publicationDtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.PUBLICATIONS, "Pubmed id", pubmedId));
    }

}
