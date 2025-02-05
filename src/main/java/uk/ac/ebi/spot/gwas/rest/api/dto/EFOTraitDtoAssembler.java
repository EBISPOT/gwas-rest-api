package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.EfoTrait;
import uk.ac.ebi.spot.gwas.rest.api.controller.EFOTraitsController;
import uk.ac.ebi.spot.gwas.rest.dto.EFOTraitDTO;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EFOTraitDtoAssembler extends RepresentationModelAssemblerSupport<EfoTrait, EFOTraitDTO> {


    public EFOTraitDtoAssembler() {
        super(EFOTraitsController.class, EFOTraitDTO.class);
    }

    @Override
    public EFOTraitDTO toModel(EfoTrait efoTrait) {
        EFOTraitDTO efoTraitDTO = EFOTraitDTO
                .builder()
                .trait(efoTrait.getTrait())
                .shortForm(efoTrait.getShortForm())
                .uri(efoTrait.getUri())
                .build();
        efoTraitDTO.add(linkTo(methodOn(EFOTraitsController.class).getEFOTraitsDTO(efoTrait.getShortForm())).withSelfRel());
        return efoTraitDTO;
    }
}
