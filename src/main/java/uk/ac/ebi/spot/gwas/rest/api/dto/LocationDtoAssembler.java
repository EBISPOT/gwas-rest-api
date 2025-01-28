package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.Location;
import uk.ac.ebi.spot.gwas.rest.dto.LocationDTO;


@Component
public class LocationDtoAssembler {

    @Autowired
    RegionDtoAssembler regionDtoAssembler;

    public LocationDTO assemble(Location location) {
       return LocationDTO.builder()
               .chromosomeName(location.getChromosomeName())
               .chromosomePosition(location.getChromosomePosition())
               .region(regionDtoAssembler.assemble(location.getRegion()))
               .build();
    }
}
