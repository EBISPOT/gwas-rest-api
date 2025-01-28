package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.RiskAllele;
import uk.ac.ebi.spot.gwas.rest.dto.RiskAlleleDTO;

@Component
public class RisklAlleleDtoAssembler {

    public RiskAlleleDTO assemble(RiskAllele riskAllele) {
        return RiskAlleleDTO.builder()
                .riskAlleleName(riskAllele.getRiskAlleleName())
                .riskFrequency(riskAllele.getRiskFrequency())
                .genomeWide(riskAllele.getGenomeWide())
                .limitedList(riskAllele.getLimitedList())
                .build();
    }

}
