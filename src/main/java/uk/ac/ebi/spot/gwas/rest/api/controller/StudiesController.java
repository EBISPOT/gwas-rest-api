package uk.ac.ebi.spot.gwas.rest.api.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
import uk.ac.ebi.spot.gwas.rest.dto.StudiesSortParam;
import uk.ac.ebi.spot.gwas.rest.dto.StudyDto;

@Slf4j
@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_STUDIES)
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
    public PagedModel<StudyDto> getStudies(@RequestParam @ParameterObject SearchStudyParams studyParams,
                                           @RequestParam(required = false)  String sort,
                                           @RequestParam(required = false)   String direction,
                                           @ParameterObject Pageable pageable) {
        log.info("Inside getStudies()");
        Page<Study>  studies = studyService.getStudies(pageable, studyParams, sort, direction);
        return  pagedResourcesAssembler.toModel(studies, studyDtoAssembler );
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{accession_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StudyDto> getStudyByAccession(@PathVariable (name = "accession_id") String accessionId) {
        return studyService.getStudy(accessionId)
                .map(studyDtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.STUDIES, "Accession Id", accessionId));

    }
}
