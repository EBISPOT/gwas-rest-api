package uk.ac.ebi.spot.gwas.rest.api.controller;

import io.swagger.v3.oas.annotations.Parameter;
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
import uk.ac.ebi.spot.gwas.model.UnpublishedStudy;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.UnpublishedStudyDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.UnpublishedStudyService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchUnpublishedStudyParams;
import uk.ac.ebi.spot.gwas.rest.dto.UnpublishedStudyDTO;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_UNPUBLISHED_STUDIES)
@Tag(name = "unpublished-studies")
public class UnpublishedStudiesController {

    @Autowired
    UnpublishedStudyService unpublishedStudyService;

    @Autowired
    UnpublishedStudyDtoAssembler unpublishedStudyDtoAssembler;

    @Autowired
    PagedResourcesAssembler<UnpublishedStudy> pagedResourcesAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<UnpublishedStudyDTO> getUnpublishedStudies(@RequestParam SearchUnpublishedStudyParams searchUnpublishedStudyParams,
                                                                 @SortDefault(sort = "accession", direction = Sort.Direction.DESC)  @ParameterObject Pageable pageable) {
        Page<UnpublishedStudy> unpublishedStudies = unpublishedStudyService.getUnpublishedStudies(searchUnpublishedStudyParams, pageable);
        return pagedResourcesAssembler.toModel(unpublishedStudies, unpublishedStudyDtoAssembler);
    }
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{accessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UnpublishedStudyDTO> getUnpublishedStudy(@PathVariable @Parameter(name = "accessionId",
            description = "The study’s GWAS Catalog accession ID <br/> <br/>" +
                    "<i> Example </i> : GCST000854") String accessionId) {
        return unpublishedStudyService.findByAccession(accessionId)
                .map(unpublishedStudyDtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.UNPUBLISHED_STUDY, "Accession Id", accessionId));
    }
}
