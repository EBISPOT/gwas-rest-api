package uk.ac.ebi.spot.gwas.rest.api.dto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.ensembl.Variant;
import uk.ac.ebi.spot.gwas.model.SingleNucleotidePolymorphism;
import uk.ac.ebi.spot.gwas.rest.api.controller.GenomicContextController;
import uk.ac.ebi.spot.gwas.rest.api.controller.SnpsController;
import uk.ac.ebi.spot.gwas.rest.api.service.RestInteractionService;
import uk.ac.ebi.spot.gwas.rest.api.service.SnpService;
import uk.ac.ebi.spot.gwas.rest.dto.LocationDTO;
import uk.ac.ebi.spot.gwas.rest.dto.SingleNucleotidePolymorphismDTO;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SnpDtoAssembler extends RepresentationModelAssemblerSupport<SingleNucleotidePolymorphism, SingleNucleotidePolymorphismDTO> {

    @Autowired
    LocationDtoAssembler locationDtoAssembler;

    @Autowired
    RestInteractionService restInteractionService;

    @Autowired
    SnpService snpService;

    public SnpDtoAssembler() {
        super(SnpsController.class, SingleNucleotidePolymorphismDTO.class);
    }

    private final String ALLELE_PATTERN = "\\w{1,2}";

    public SingleNucleotidePolymorphismDTO toModel(SingleNucleotidePolymorphism snp) {
        Variant variant = restInteractionService.getEnsemblResponse(snp.getRsId());

        SingleNucleotidePolymorphismDTO singleNucleotidePolymorphismDTO = SingleNucleotidePolymorphismDTO
                .builder()
                .rsId(snp.getRsId())
                .merged(snp.getMerged())
                .currentSnp(snp.getCurrentSnp() !=  null ? snp.getCurrentSnp().getRsId() : null)
                .locations(this.getLocations(snp))
                .functionalClass(snp.getFunctionalClass())
                .lastUpdateDate(snp.getLastUpdateDate())
                .maf(variant != null ? variant.getMaf() : null)
                .minorAllele(variant != null ? variant.getMinorAllele() : "NA")
                .alleles(variant != null ? this.getAlleles(variant) : null)
                .mostSevereConsequence(variant != null ? variant.getMostSevereConsequence() : null)
                .mappedGenes(snpService.findMatchingGenes(snp.getId()))
                .build();
        singleNucleotidePolymorphismDTO.add(linkTo(methodOn(SnpsController.class).getSingleNucleotidePolymorphism(snp.getRsId())).withSelfRel());
        singleNucleotidePolymorphismDTO.add(linkTo(methodOn(GenomicContextController.class).getGenomicContexts(snp.getRsId())).withRel("genomic-contexts"));
        return singleNucleotidePolymorphismDTO;
    }


    private List<LocationDTO> getLocations(SingleNucleotidePolymorphism snp) {
        return snp.getLocations().stream()
                .map(locationDtoAssembler::assemble)
                .collect(Collectors.toList());
    }


    private String getAlleles(Variant variant) {

        final Pattern pattern = Pattern.compile(ALLELE_PATTERN);

        return variant.getMappings().stream().map(mapping -> {
            Matcher matcher = pattern.matcher(mapping.getSeqRegionName());
            if(matcher.find()) {
                String alleleString = mapping.getAlleleString();
                String strand = mapping.getStrand() == 1 ? "forward" : "reverse";
                return String.format("%s %s%s%s", alleleString,"(",strand,")");
            }
            return null;
        }).collect(Collectors.joining(" "));
    }

}
