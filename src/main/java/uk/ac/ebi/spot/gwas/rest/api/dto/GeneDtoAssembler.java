package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.EnsemblGene;
import uk.ac.ebi.spot.gwas.model.EntrezGene;
import uk.ac.ebi.spot.gwas.model.Gene;
import uk.ac.ebi.spot.gwas.rest.dto.GeneDTO;

import java.util.stream.Collectors;

@Component
public class GeneDtoAssembler {


    public GeneDTO assemble(Gene gene) {
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
