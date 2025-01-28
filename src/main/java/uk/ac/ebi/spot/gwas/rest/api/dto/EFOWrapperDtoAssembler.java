package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.EfoTrait;
import uk.ac.ebi.spot.gwas.rest.dto.EFOWrapperDTO;

@Component
public class EFOWrapperDtoAssembler {

    public EFOWrapperDTO assemble(EfoTrait efoTrait) {
        return  EFOWrapperDTO.builder()
                .efoTrait(efoTrait.getTrait())
                .shortForm(efoTrait.getShortForm())
                .build();
    }
}
