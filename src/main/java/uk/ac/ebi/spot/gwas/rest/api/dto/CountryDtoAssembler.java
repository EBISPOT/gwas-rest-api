package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.Country;
import uk.ac.ebi.spot.gwas.rest.dto.CountryDTO;

@Component
public class CountryDtoAssembler {

    public CountryDTO assemble(Country country) {
        return CountryDTO.builder()
                .countryName(country.getCountryName())
                .region(country.getRegion())
                .majorArea(country.getMajorArea())
                .build();
    }
}
