package uk.ac.ebi.spot.gwas.rest.api.controller;

import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.dto.SearchStudyParams;
import uk.ac.ebi.spot.gwas.rest.dto.StudyDto;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_STUDIES)
public class StudiesController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Resource<StudyDto> getStudies(PagedResourcesAssembler assemble,
                                         SearchStudyParams studyParams) {



    }
}
