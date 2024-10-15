package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.rest.api.controller.StudiesController;
import uk.ac.ebi.spot.gwas.rest.dto.StudyDto;
import uk.ac.ebi.spot.gwas.rest.projection.StudyProjection;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StudyProjectionDtoAssembler extends RepresentationModelAssemblerSupport<StudyProjection, StudyDto> {
    @Autowired
    EFOWrapperDtoAssembler efoWrapperDtoAssembler;

    public StudyProjectionDtoAssembler() {
        super(StudiesController.class, StudyDto.class);
    }


    @Override
    public StudyDto toModel(StudyProjection studyProjection) {
       StudyDto studyDto = StudyDto.builder()
               .accessionId(studyProjection.getAccessionId())
               .diseaseTrait(studyProjection.getDiseaseTrait() != null ? studyProjection.getDiseaseTrait().getTrait() : null)
               .studyDesignComment(studyProjection.getStudyDesignComment())
               .fullPvalueSet(studyProjection.getFullPvalueSet())
               .gxe(studyProjection.getGxe())
               .studyId(studyProjection.getStudyId())
               .build();
              /* .efoTraits(studyProjection.getEFOTraits() != null ?  studyProjection.getEFOTraits().stream()
                                        .map(efoWrapperDtoAssembler::assemble)
                                        .collect(Collectors.toList()) : null)
                .bgEfoTraits(studyProjection.getMappedBackgroundTraits() != null ? studyProjection.getMappedBackgroundTraits().stream()
                                        .map(efoWrapperDtoAssembler::assemble)
                                        .collect(Collectors.toList()) : null)*/



        studyDto.add(linkTo(methodOn(StudiesController.class).getStudy(String.valueOf(studyProjection.getStudyId()))).withSelfRel());
        return studyDto;
    }
}
