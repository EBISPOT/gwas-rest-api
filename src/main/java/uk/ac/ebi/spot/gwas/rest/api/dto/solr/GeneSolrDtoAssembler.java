package uk.ac.ebi.spot.gwas.rest.api.dto.solr;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.EnsemblGene;
import uk.ac.ebi.spot.gwas.model.EntrezGene;
import uk.ac.ebi.spot.gwas.model.Gene;
import uk.ac.ebi.spot.gwas.rest.api.controller.GeneController;
import uk.ac.ebi.spot.gwas.rest.api.controller.StudiesController;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim.GeneSolrDto;
import uk.ac.ebi.spot.gwas.rest.dto.GeneDTO;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GeneSolrDtoAssembler extends RepresentationModelAssemblerSupport<GeneSolrDto, GeneDTO> {

    public GeneSolrDtoAssembler() {
        super(GeneController.class, GeneDTO.class);
    }

    @Override
    @Nonnull
    public GeneDTO toModel(GeneSolrDto geneSolrDto) {
        GeneDTO geneDTO = GeneDTO.builder()
                .geneName(geneSolrDto.getTitle())
                .ensemblGeneIds(Collections.singletonList(geneSolrDto.getEnsemblId()))
                .entrezGeneIds(Collections.singletonList(geneSolrDto.getEntrezId()))
                .cytogenicRegion(geneSolrDto.getCytobands())
                .geneDescription(Optional.ofNullable(geneSolrDto.getDescription())
                        .map(desc -> desc.split("\\|")[0])
                        .orElse(""))
                .biotype(geneSolrDto.getBiotype())
                .location(String.format("%s:%d-%d",
                        geneSolrDto.getChromosomeName(),
                        geneSolrDto.getChromosomeStart(),
                        geneSolrDto.getChromosomeEnd()))
                .build();
        geneDTO.add(linkTo(methodOn(GeneController.class).getGeneByName(geneDTO.getGeneName())).withSelfRel());
        return geneDTO;
    }
}
