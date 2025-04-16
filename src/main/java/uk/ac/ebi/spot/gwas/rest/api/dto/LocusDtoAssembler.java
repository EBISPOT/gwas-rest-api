package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.Locus;
import uk.ac.ebi.spot.gwas.rest.api.controller.LocusController;
import uk.ac.ebi.spot.gwas.rest.dto.LocusDTO;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocusDtoAssembler extends RepresentationModelAssemblerSupport<Locus, LocusDTO> {



    @Autowired
    GeneDtoAssembler geneDtoAssembler;

    @Autowired
    RisklAlleleDtoAssembler risklAlleleDtoAssembler;


    public LocusDtoAssembler() {
        super(LocusController.class, LocusDTO.class);
    }

    @Override
    public LocusDTO toModel(Locus locus) {
        return toModel(locus, locus.getAssociation().getId());
    }

    public LocusDTO toModel(Locus locus, Long associationId) {
        LocusDTO locusDTO = LocusDTO.builder()
                .haplotypeSnpCount(locus.getHaplotypeSnpCount())
                .description(locus.getDescription())
                .strongestRiskAlleles(locus.getStrongestRiskAlleles() != null ?
                        locus.getStrongestRiskAlleles().stream()
                                .map(risklAlleleDtoAssembler::assemble)
                                .collect(Collectors.toList()) : null)
                .authorReportedGenes(locus.getAuthorReportedGenes() != null ?
                        locus.getAuthorReportedGenes().stream()
                                .map(geneDtoAssembler::toModel)
                                .collect(Collectors.toList()) : null)
                .build();
        locusDTO.add(linkTo(methodOn(LocusController.class).getLocus(String.valueOf(associationId),
                String.valueOf(locus.getId()))).withSelfRel());
        return locusDTO;
    }

    public CollectionModel<LocusDTO> toCollectionModel(List<Locus> loci, Long associationId) {
       List<LocusDTO>  locusDTOS = loci.stream()
                .map(locus -> toModel(locus, associationId))
                .collect(Collectors.toList());

        return CollectionModel.of(locusDTOS);
    }
}
