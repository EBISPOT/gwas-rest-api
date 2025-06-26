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
import uk.ac.ebi.spot.gwas.model.BodyOfWork;
import uk.ac.ebi.spot.gwas.model.UnpublishedStudy;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.BodyOfWorkDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.dto.UnpublishedStudyDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.BodyOfWorkService;
import uk.ac.ebi.spot.gwas.rest.api.service.UnpublishedStudyService;
import uk.ac.ebi.spot.gwas.rest.dto.BodyOfWorkDTO;
import uk.ac.ebi.spot.gwas.rest.dto.UnpublishedStudyDTO;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_BODY_OF_WORK)
public class BodyOfWorkController {

    @Autowired
   BodyOfWorkService bodyOfWorkService;

    @Autowired
    PagedResourcesAssembler<BodyOfWork> pagedResourcesAssembler;

    @Autowired
    PagedResourcesAssembler<UnpublishedStudy> unpublishedStudyPagedResourcesAssembler;

    @Autowired
    BodyOfWorkDtoAssembler bodyOfWorkDtoAssembler;

    @Autowired
    UnpublishedStudyDtoAssembler unpublishedStudyDtoAssembler;

    @Autowired
    UnpublishedStudyService unpublishedStudyService;

   @ResponseStatus(HttpStatus.OK)
   @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
   public PagedModel<BodyOfWorkDTO> getBodiesOfWork(@RequestParam(value = "title", required = false)    String title,
                                                    @RequestParam(value = "first_author", required = false) String firstAuthor,
                                                    @SortDefault(sort = "id", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable) {
       Page<BodyOfWork> bodyOfWorks = bodyOfWorkService.getBodyOfWork(title, firstAuthor, pageable);
       return pagedResourcesAssembler.toModel(bodyOfWorks, bodyOfWorkDtoAssembler);
    }

    @GetMapping(value = "/{bow_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BodyOfWorkDTO> getBodyOfWork(@PathVariable (name = "bow_id") Long bowId) {
        return bodyOfWorkService.getBodyOfWork(bowId)
                .map(bodyOfWorkDtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.BODY_OF_WORKS, "Id", String.valueOf(bowId)));
    }

    @GetMapping(value = "/{bow_id}"+RestAPIConstants.API_UNPUBLISHED_STUDIES, produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<UnpublishedStudyDTO> getUnpublishedStudies(@PathVariable (name = "bow_id") Long bowId, @ParameterObject Pageable pageable) {
        Page<UnpublishedStudy> unpublishedStudies = unpublishedStudyService.findByBodyOfWork(bowId, pageable);
        return unpublishedStudyPagedResourcesAssembler.toModel(unpublishedStudies, unpublishedStudyDtoAssembler);
    }

}
