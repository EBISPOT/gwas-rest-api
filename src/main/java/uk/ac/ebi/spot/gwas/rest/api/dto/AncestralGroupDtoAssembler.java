package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.AncestralGroup;
import uk.ac.ebi.spot.gwas.rest.dto.AncestralGroupDTO;

@Component
public class AncestralGroupDtoAssembler {

    public AncestralGroupDTO assemble(AncestralGroup ancestralGroup) {
        return AncestralGroupDTO.builder()
                .ancestralGroup(ancestralGroup.getAncestralGroup())
                .build();
    }
}
