package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.GenomicContext;
import uk.ac.ebi.spot.gwas.rest.api.controller.GenomicContextController;
import uk.ac.ebi.spot.gwas.rest.dto.GenomicContextDTO;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GenomicContextDtoAssembler extends RepresentationModelAssemblerSupport<GenomicContext, GenomicContextDTO> {



    @Autowired
    GeneDtoAssembler geneDtoAssembler;

    @Autowired
    LocationDtoAssembler locationDtoAssembler;

    public GenomicContextDtoAssembler() {
        super(GenomicContextController.class, GenomicContextDTO.class);
    }

    @Override
    public GenomicContextDTO toModel(GenomicContext genomicContext) {
        return toModel(genomicContext, genomicContext.getSnp() != null ? genomicContext.getSnp().getRsId() : null);
    }

    public GenomicContextDTO toModel(GenomicContext genomicContext, String rsId) {

        GenomicContextDTO genomicContextDTO = GenomicContextDTO.builder()
                .isDownstream(genomicContext.getIsDownstream())
                .isIntergenic(genomicContext.getIsIntergenic())
                .isUpstream(genomicContext.getIsUpstream())
                .geneDTO(genomicContext.getGene() != null ?
                        geneDtoAssembler.assemble(genomicContext.getGene()) : null)
                .locationDTO(genomicContext.getLocation() != null ?
                        locationDtoAssembler.assemble(genomicContext.getLocation()) : null)
                .distance(genomicContext.getDistance())
                .source(genomicContext.getSource())
                .mappingMethod(genomicContext.getMappingMethod())
                .isClosestGene(genomicContext.getIsClosestGene())
                .build();

        genomicContextDTO.add(linkTo(methodOn(GenomicContextController.class).getGenomicContext(rsId, String.valueOf(genomicContext.getId()))).withSelfRel());
        return genomicContextDTO;
    }


    public CollectionModel<GenomicContextDTO> toCollectionModel(List<GenomicContext> genomicContexts, String rsId) {
    List<GenomicContextDTO> genomicContextDTOS =  genomicContexts.stream()
                .map(genomicContext -> toModel(genomicContext, rsId))
                .collect(Collectors.toList());
    return CollectionModel.of(genomicContextDTOS);
    }
}
