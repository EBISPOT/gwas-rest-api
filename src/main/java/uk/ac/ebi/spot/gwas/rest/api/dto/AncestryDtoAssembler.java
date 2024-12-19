package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.Ancestry;
import uk.ac.ebi.spot.gwas.rest.api.controller.AncestryController;
import uk.ac.ebi.spot.gwas.rest.dto.AncestryDTO;
import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AncestryDtoAssembler extends RepresentationModelAssemblerSupport<Ancestry, AncestryDTO> {

    @Autowired
    private AncestralGroupDtoAssembler ancestralGroupDtoAssembler;

    @Autowired
    private CountryDtoAssembler countryDtoAssembler;

    public AncestryDtoAssembler() {
        super(AncestryController.class, AncestryDTO.class);
    }

    @Override
    public AncestryDTO toModel(Ancestry ancestry) {
        return toModel(ancestry, ancestry.getStudy().getAccessionId());
    }

    public AncestryDTO toModel(Ancestry ancestry, String accessionId) {
        AncestryDTO ancetryDTO = AncestryDTO.builder()
                .type(ancestry.getType())
                .numberOfIndividuals(ancestry.getNumberOfIndividuals())
                .countryOfOrigin(ancestry.getCountryOfOrigin() != null ?
                        ancestry.getCountryOfOrigin().stream()
                                .map(countryDtoAssembler::assemble)
                                .collect(Collectors.toList()) : null)
                .countryOfRecruitment(ancestry.getCountryOfRecruitment() != null ?
                        ancestry.getCountryOfRecruitment().stream()
                                .map(countryDtoAssembler::assemble)
                                .collect(Collectors.toList()) : null)
                .ancestralGroups(ancestry.getAncestralGroups() != null ?
                        ancestry.getAncestralGroups().stream()
                                .map(ancestralGroupDtoAssembler::assemble)
                                .collect(Collectors.toList()) : null)
                .build();
        ancetryDTO.add(linkTo(methodOn(AncestryController.class).getAncestry(accessionId, String.valueOf(ancestry.getId()))).withSelfRel());
        return ancetryDTO;
    }



    public CollectionModel<AncestryDTO> toCollectionModel(List<Ancestry> ancestries, String accessionId) {
        List<AncestryDTO> ancetryDTOS = ancestries.stream()
                .map(ancestry -> toModel(ancestry, accessionId))
                .collect(Collectors.toList());
        return CollectionModel.of(ancetryDTOS);
    }
}
