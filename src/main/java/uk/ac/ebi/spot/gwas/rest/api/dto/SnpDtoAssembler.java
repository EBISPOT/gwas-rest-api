package uk.ac.ebi.spot.gwas.rest.api.dto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.SingleNucleotidePolymorphism;
import uk.ac.ebi.spot.gwas.rest.api.controller.SnpsController;
import uk.ac.ebi.spot.gwas.rest.dto.LocationDTO;
import uk.ac.ebi.spot.gwas.rest.dto.SingleNucleotidePolymorphismDTO;
import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SnpDtoAssembler extends RepresentationModelAssemblerSupport<SingleNucleotidePolymorphism, SingleNucleotidePolymorphismDTO> {

    @Autowired
    LocationDtoAssembler locationDtoAssembler;

    public SnpDtoAssembler() {
        super(SnpsController.class, SingleNucleotidePolymorphismDTO.class);
    }

    public SingleNucleotidePolymorphismDTO toModel(SingleNucleotidePolymorphism snp) {
        SingleNucleotidePolymorphismDTO singleNucleotidePolymorphismDTO = SingleNucleotidePolymorphismDTO
                .builder()
                .rsId(snp.getRsId())
                .merged(snp.getMerged())
                .currentSnp(snp.getCurrentSnp() !=  null ? snp.getCurrentSnp().getRsId() : null)
                .locations(this.getLocations(snp))
                .functionalClass(snp.getFunctionalClass())
                .lastUpdateDate(snp.getLastUpdateDate())
                .build();
        singleNucleotidePolymorphismDTO.add(linkTo(methodOn(SnpsController.class).getSingleNucleotidePolymorphism(snp.getRsId())).withSelfRel());
        return singleNucleotidePolymorphismDTO;
    }


   private List<LocationDTO> getLocations(SingleNucleotidePolymorphism snp) {
       return snp.getLocations().stream()
                .map(locationDtoAssembler::assemble)
                .collect(Collectors.toList());
    }
}
