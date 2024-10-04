package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import uk.ac.ebi.spot.gwas.model.Study;
import uk.ac.ebi.spot.gwas.rest.dto.StudyDto;

public class StudyDtoAssembler implements ResourceAssembler<Study, Resource<StudyDto>> {

    @Override
    public Resource<StudyDto> toResource(Study study) {
       StudyDto  studyDto = StudyDto.builder()
               .accessionId(study.getAccessionId())
               .diseaseTrait(study.getDiseaseTrait() != null ? study.getDiseaseTrait().getTrait() : null)
               .studyDesignComment(study.getStudyDesignComment())
               .efoTraits()
    }
}
