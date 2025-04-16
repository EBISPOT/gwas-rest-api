package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.EnsemblGene;
import uk.ac.ebi.spot.gwas.model.EntrezGene;
import uk.ac.ebi.spot.gwas.model.Gene;
import uk.ac.ebi.spot.gwas.rest.api.controller.GeneController;
import uk.ac.ebi.spot.gwas.rest.dto.GeneDTO;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

@Component
public class GeneDtoAssembler extends RepresentationModelAssemblerSupport<Gene, GeneDTO> {

    public GeneDtoAssembler() {
        super(GeneController.class, GeneDTO.class);
    }

    @Override
    @Nonnull
    public GeneDTO toModel(Gene gene) {
        return GeneDTO.builder()
                .geneName(gene.getGeneName())
                .ensemblGeneIds(gene.getEnsemblGeneIds() != null ?
                        gene.getEnsemblGeneIds().stream()
                                .map(EnsemblGene::getEnsemblGeneId)
                                .collect(Collectors.toList()) : null)
                .entrezGeneIds(gene.getEntrezGeneIds() != null ?
                        gene.getEntrezGeneIds().stream()
                                .map(EntrezGene::getEntrezGeneId)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}
