package uk.ac.ebi.spot.gwas.rest.api.dto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.controller.AssociationController;
import uk.ac.ebi.spot.gwas.rest.api.controller.LocusController;
import uk.ac.ebi.spot.gwas.rest.api.controller.SnpsController;
import uk.ac.ebi.spot.gwas.rest.api.service.AssociationService;
import uk.ac.ebi.spot.gwas.rest.dto.AssociationDTO;
import uk.ac.ebi.spot.gwas.rest.dto.EFOWrapperDTO;
import uk.ac.ebi.spot.gwas.rest.dto.RiskAlleleWrapperDTO;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@Slf4j
public class AssociationDtoAssembler extends RepresentationModelAssemblerSupport<Association, AssociationDTO> {

    @Autowired
    AssociationService associationService;

    @Autowired
    EFOWrapperDtoAssembler efoWrapperDtoAssembler;

    private final String RANGE_PATTERN_REGEX = "\\[([+-]?\\d+\\.?\\d*)-([+-]?\\d+\\.?\\d*)\\]";

    public AssociationDtoAssembler() {
        super(AssociationController.class, AssociationDTO.class);
    }

    @Override
    public AssociationDTO toModel(Association association) {
        Pair<Float, Float> ciValues = Optional.ofNullable(association.getRange()).map(this::getCIValues).orElse(null);
        List<Pair<RiskAlleleWrapperDTO, String>> pairList = getRiskAllele(association);
        AssociationDTO associationDTO =  AssociationDTO.builder()
                .associationId(association.getId())
                .accessionID(association.getStudy() != null ? association.getStudy().getAccessionId()
                        : null)
                .riskAllele(this.getSnpRiskAlleles(pairList))
                .effectAlleles(this.getEffectAlleles(pairList))
                .pValue(getPValue(association))
                .pvalueDescription(Optional.ofNullable(association.getDescription())
                        .map(this::tranformPValueAnnotation).orElse(""))
                .riskFrequency(association.getRiskFrequency())
                .orValue(Optional.ofNullable(association.getOrPerCopyNum()).map(orVal ->
                        this.transformOrValue(orVal, association.getDescription())).orElse(null))
                .orPerCopyNum(association.getOrPerCopyNum())
                .beta(Optional.ofNullable(association.getBetaNum()).map(betaNum ->
                        this.transformBeta(betaNum, association.getBetaUnit(), association.getBetaDirection()))
                        .orElse("-"))
                .range(Optional.ofNullable(association.getRange()).orElse("-"))
                .ciLower(Optional.ofNullable(association.getRange()).filter(range -> !range.contains("NR")).map(range -> ciValues.getLeft()).orElse(null))
                .ciUpper(Optional.ofNullable(association.getRange()).filter(range -> !range.contains("NR")).map(range -> ciValues.getRight()).orElse(null))
                .mappedGenes(this.getMappedGenes(association))
                .reportedTrait(this.getReportedTrait(association.getId()))
                .efoTraits(this.getEFOTraits(association))
                .bgEfoTraits(this.getBGEFOTraits(association))
                .locations(this.getLocationDetails(association))
                .pubmedId(this.getPubmedId(association))
                .firstAuthor(this.getFirstAuthor(association))
                .build();

        associationDTO.add(linkTo(methodOn(AssociationController.class).getAssociation(String.valueOf(association.getId()))).withSelfRel());
        if(association.getLoci() != null && !association.getLoci().isEmpty()) {
            associationDTO.add(linkTo(methodOn(LocusController.class).getLoci(String.valueOf(association.getId()))).withRel("loci"));
        }
        for(Locus locus : association.getLoci()) {
            for(RiskAllele  riskAllele : locus.getStrongestRiskAlleles() ) {
                if(riskAllele.getSnp() != null) {
                    associationDTO.add(linkTo(methodOn(SnpsController.class).getSingleNucleotidePolymorphism(riskAllele.getSnp().getRsId())).withRel("snp"));
                }
            }

            }

        return associationDTO;
    }

    private String getPubmedId(Association association) {
       return  association.getStudy().getPublicationId().getPubmedId();
    }

    private String getFirstAuthor(Association association) {
        return association.getStudy().getPublicationId().getFirstAuthor().getFullnameStandard();
    }

    private Double getPValue(Association association) {
        return association.getPvalueMantissa() * Math.pow(10, association.getPvalueExponent());
    }

    private List<String> getReportedTrait(Long associationId) {
        return associationService.getDiseaseTraits(associationId).stream()
                .map(DiseaseTrait::getTrait)
                .collect(Collectors.toList());

    }

    private List<EFOWrapperDTO> getEFOTraits(Association association) {
       return association.getEfoTraits().stream()
                .map(efoWrapperDtoAssembler::assemble)
                .collect(Collectors.toList());
    }

    private List<EFOWrapperDTO> getBGEFOTraits(Association association) {
        return association.getBkgEfoTraits().stream()
                .map(efoWrapperDtoAssembler::assemble)
                .collect(Collectors.toList());
    }

    private String transformBeta(Float beta, String betaUnit, String betaDirection) {
        StringBuilder finalBeta = new StringBuilder();
        finalBeta.append(beta);
        if(!(String.valueOf(beta)).isEmpty()) {
            if(betaUnit != null && !betaUnit.isEmpty()) {
                finalBeta.append(" ");
                finalBeta.append(betaUnit);
            }
            if(betaDirection != null && !betaDirection.isEmpty()) {
                finalBeta.append(" ");
                finalBeta.append(betaDirection);
            }
        }
        return finalBeta.toString();
    }

    private String transformOrValue(Float orValue ,String orDescription){
        return  Optional.ofNullable(orDescription).map(orDesc -> orValue + " " + orDescription)
                .orElse(String.valueOf(orValue));
    }

    private String tranformPValueAnnotation(String qualifier) {
        if(!qualifier.isEmpty()) {
            if(qualifier.matches(".+"))
                return qualifier;
            else
                return null;
        }
        return null;
    }

    private List<Pair<RiskAlleleWrapperDTO, String>> getRiskAllele(Association association) {

        String primary_delimiter = "";
        if(association.getSnpInteraction()) {
            primary_delimiter = " x ";
        }else {
            primary_delimiter = "; ";
        }
        String riskAlleles = association.getLoci().stream()
                    .flatMap(loci -> loci.getStrongestRiskAlleles().stream())
                    .map(RiskAllele::getRiskAlleleName)
                    .collect(Collectors.joining(primary_delimiter));

        String riskAllele =  riskAlleles.replaceAll(" -","-");
        String separator = " x ";
        if( riskAllele.contains(";")){
            separator = ";";
        }
        String[] alleles = riskAllele.split(separator);
        return  Arrays.stream(alleles).map((allele) -> {
            if (allele.matches(".+-.+")) {
                int lastIndex = allele.lastIndexOf("-");
                String allele_rsId = allele.substring(0, lastIndex).trim().replace(" ", "");
                String allele_label = allele.substring(lastIndex + 1).trim();
                RiskAlleleWrapperDTO riskAlleleWrapperDTO = RiskAlleleWrapperDTO.builder()
                        .riskAllele(allele_label)
                        .rsID(allele_rsId)
                        .build();
                return Pair.of(riskAlleleWrapperDTO,allele_rsId + "-" + allele_label);
            }
            return null;
        }).collect(Collectors.toList());
    }

    private List<String> getSnpRiskAlleles(List<Pair<RiskAlleleWrapperDTO, String>> pairList) {
       return pairList.stream().map(Pair::getRight).collect(Collectors.toList());
    }

    private List<RiskAlleleWrapperDTO> getEffectAlleles(List<Pair<RiskAlleleWrapperDTO, String>> pairList) {
        return pairList.stream().map(Pair::getLeft).collect(Collectors.toList());
    }


    private List<String> getMappedGenes(Association association) {
        List<String> mappedGenes = new ArrayList<>();
        for(Locus locus : association.getLoci()) {
            for(RiskAllele  riskAllele : locus.getStrongestRiskAlleles() ) {
               List<String> snpMappedGenes = getMappedGenes(riskAllele.getSnp());
               if(snpMappedGenes !=  null ) {
                   mappedGenes.addAll(snpMappedGenes);
               }
            }
        }
       return mappedGenes;
    }

    private Pair<Float, Float> getCIValues(String range) {
        Pattern pattern = Pattern.compile(RANGE_PATTERN_REGEX, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(range);
        if(matcher.find()) {
            return Pair.of(Float.valueOf(matcher.group(1)), Float.valueOf(matcher.group(2)));
        }
        return null;
    }

    private List<String> getMappedGenes(SingleNucleotidePolymorphism snp) {
       return snp.getMappedSnpGenes().stream().map(Gene::getGeneName).collect(Collectors.toList());
    }


    private List<String> getLocationDetails(Association association) {
      return association.getLoci().stream()
                .flatMap(loci -> loci.getStrongestRiskAlleles().stream())
                .map(riskAllele -> riskAllele.getSnp())
                .flatMap(snp -> snp.getLocations().stream())
                .map(this::transformSnpLocation)
                .collect(Collectors.toList());
    }

    private String transformSnpLocation(Location snpLocation) {
        String chromosomeName = "";
        String chromosomePosition = "";
        if(snpLocation.getChromosomeName() != null) {
             chromosomeName = snpLocation.getChromosomeName();
        } else {
            chromosomeName = "NA";
        }
        if(snpLocation.getChromosomePosition() != null) {
            chromosomePosition = String.valueOf(snpLocation.getChromosomePosition());
        } else {
            chromosomePosition = "NA";
        }

        if(chromosomeName.length() < 3) {
            return chromosomeName + ":" + chromosomePosition;
        }
        return "";
    }

}