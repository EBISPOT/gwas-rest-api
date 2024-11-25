package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.Region;
import uk.ac.ebi.spot.gwas.rest.dto.RegionDTO;

@Component
public class RegionDtoAssembler {

    public RegionDTO assemble(Region region) {
        return RegionDTO.builder()
                .name(region.getName())
                .build();
    }

}
