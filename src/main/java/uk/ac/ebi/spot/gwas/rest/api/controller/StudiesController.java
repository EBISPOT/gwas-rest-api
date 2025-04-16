package uk.ac.ebi.spot.gwas.rest.api.controller;

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
import uk.ac.ebi.spot.gwas.model.Study;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.StudyDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.dto.StudyProjectionDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.StudyService;
import uk.ac.ebi.spot.gwas.rest.dto.SearchStudyParams;
import uk.ac.ebi.spot.gwas.rest.dto.StudyDto;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_STUDIES)
@Tag(name = "studies")
public class StudiesController {

    @Autowired
    StudyService studyService;

    @Autowired
    StudyProjectionDtoAssembler studyProjectionDtoAssembler;

    @Autowired
    StudyDtoAssembler studyDtoAssembler;

    @Autowired
    PagedResourcesAssembler<Study> pagedResourcesAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<StudyDto> getStudies(@RequestParam SearchStudyParams studyParams,
                                           @SortDefault(sort = "accessionId", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable) {

       Page<Study>  studies = studyService.getStudies(pageable, studyParams);
       return  pagedResourcesAssembler.toModel(studies, studyDtoAssembler );
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{accessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StudyDto> getStudyByAccession(@PathVariable String accessionId) {
        return studyService.getStudy(accessionId)
                .map(studyDtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.STUDIES, "Accession Id", accessionId));

    }
}
