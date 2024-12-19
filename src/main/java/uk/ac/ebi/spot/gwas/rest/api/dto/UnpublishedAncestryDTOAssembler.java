package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.Ancestry;
import uk.ac.ebi.spot.gwas.model.UnpublishedAncestry;
import uk.ac.ebi.spot.gwas.rest.api.controller.UnpublishedAncestriesController;
import uk.ac.ebi.spot.gwas.rest.dto.AncestryDTO;
import uk.ac.ebi.spot.gwas.rest.dto.UnpublishedAncestryDTO;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UnpublishedAncestryDTOAssembler extends RepresentationModelAssemblerSupport<UnpublishedAncestry, UnpublishedAncestryDTO> {

    public UnpublishedAncestryDTOAssembler() {
        super(UnpublishedAncestriesController.class, UnpublishedAncestryDTO.class);
    }

    @Override
    public UnpublishedAncestryDTO toModel(UnpublishedAncestry unpublishedAncestry) {
        return toModel(unpublishedAncestry, unpublishedAncestry.getStudy().getAccession());
    }

    public UnpublishedAncestryDTO toModel(UnpublishedAncestry unpublishedAncestry, String accessionId) {
        UnpublishedAncestryDTO unpublishedAncestryDTO = UnpublishedAncestryDTO.builder()
                .ancestry(unpublishedAncestry.getAncestry())
                .ancestryCategory(unpublishedAncestry.getAncestryCategory())
                .ancestryDescription(unpublishedAncestry.getAncestryDescription())
                .sampleDescription(unpublishedAncestry.getSampleDescription())
                .cases(unpublishedAncestry.getCases())
                .controls(unpublishedAncestry.getControls())
                .countryRecruitment(unpublishedAncestry.getCountryRecruitment())
                .sampleSize(unpublishedAncestry.getSampleSize())
                .stage(unpublishedAncestry.getStage())
                .studyTag(unpublishedAncestry.getStudyTag())
                .build();

        unpublishedAncestryDTO.add(linkTo(methodOn(UnpublishedAncestriesController.class).getUnpublishedAncestry(accessionId, String.valueOf(unpublishedAncestry.getId()))).withSelfRel());
        return unpublishedAncestryDTO;
    }

    public CollectionModel<UnpublishedAncestryDTO> toCollectionModel(List<UnpublishedAncestry> unpublishedAncestries, String accessionId) {
        List<UnpublishedAncestryDTO> unpublishedAncestryDTOS = unpublishedAncestries.stream()
                .map(unpublishedAncestry -> toModel(unpublishedAncestry, accessionId))
                .collect(Collectors.toList());
       return CollectionModel.of(unpublishedAncestryDTOS);
    }
}
