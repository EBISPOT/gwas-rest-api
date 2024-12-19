package uk.ac.ebi.spot.gwas.rest.api.dto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.controller.AssociationController;
import uk.ac.ebi.spot.gwas.rest.api.controller.LocusController;
import uk.ac.ebi.spot.gwas.rest.api.service.AssociationService;
import uk.ac.ebi.spot.gwas.rest.dto.AssociationDTO;
import uk.ac.ebi.spot.gwas.rest.dto.EFOWrapperDTO;
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

    public AssociationDtoAssembler() {
        super(AssociationController.class, AssociationDTO.class);
    }

    @Override
    public AssociationDTO toModel(Association association) {
        AssociationDTO associationDTO =  AssociationDTO.builder()
                .associationId(association.getId())
                .accessionID(association.getStudy() != null ? association.getStudy().getAccessionId()
                        : null)
                .riskAllele(this.getRiskAllele(association))
                .pValue(getPValue(association))
                .pvalueDescription(Optional.ofNullable(association.getDescription())
                        .map(this::tranformPValueAnnotation).orElse(""))
                .riskFrequency(association.getRiskFrequency())
                .orValue(Optional.ofNullable(association.getOrPerCopyNum()).map(orVal ->
                        this.transformOrValue(orVal, association.getDescription())).orElse(null))
                .beta(Optional.ofNullable(association.getBetaNum()).map(betaNum ->
                        this.transformBeta(betaNum, association.getBetaUnit(), association.getBetaDirection()))
                        .orElse("-"))
                .range(Optional.ofNullable(association.getRange()).orElse("-"))
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

    private List<String> getRiskAllele(Association association) {
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
                return allele_rsId + "-" + allele_label;
            }
            return null;
        }).collect(Collectors.toList());
    }



    private List<String> getMappedGenes(Association association) {
        String primary_delimiter = "";
        if(association.getSnpInteraction()) {
            primary_delimiter = " x ";
        }else {
            primary_delimiter = "; ";
        }
        StringBuilder geneStringBuilder = new StringBuilder();
        for(Locus locus : association.getLoci()) {
            for(RiskAllele  riskAllele : locus.getStrongestRiskAlleles() ) {
                if(!geneStringBuilder.toString().isEmpty()) {
                    geneStringBuilder.append(primary_delimiter);
                    geneStringBuilder.append(getMappedGeneString(association, riskAllele.getSnp(), RestAPIConstants.ENSEMBL_SOURCE));
                } else{
                    geneStringBuilder.append(getMappedGeneString(association, riskAllele.getSnp(), RestAPIConstants.ENSEMBL_SOURCE));
                }
            }
        }
       return Collections.singletonList(geneStringBuilder.toString());
    }

    private String getMappedGeneString(Association association,
                                                 SingleNucleotidePolymorphism snp,
                                                 String source) {
        Set<String> allMappedGenes = new HashSet<>();
        List<Long> mappedGenesToLocation = new ArrayList<>();
        Map<Long, String> closestUpstreamGeneNamesMap = new HashMap<>();
        Map<Long, String> closestDownstreamGeneNamesMap = new HashMap<>();
        Set<Long> locationUpDownStream = new HashSet<>();
        if (snp.getGenomicContexts().isEmpty()) {
            allMappedGenes.add("No mapped genes");
        } else {
            List<GenomicContext> gcs = snp.getGenomicContexts().stream()
                    .filter(context -> context.getGene() != null && context.getGene().getGeneName() != null
                            && context.getSource() != null)
                    .filter(context -> source.equalsIgnoreCase(context.getSource()))
                    .filter(this::filterLocationforGene)
                    .collect(Collectors.toList());
            allMappedGenes = gcs.stream()
                    .filter(context -> (context.getDistance() == 0))
                    .map(context -> context.getGene().getGeneName().trim())
                    .collect(Collectors.toSet());
            mappedGenesToLocation = gcs.stream()
                    .filter(context -> (context.getDistance() == 0))
                    .map(context -> context.getLocation().getId())
                    .collect(Collectors.toList());
            closestUpstreamGeneNamesMap = gcs.stream()
                    .filter(context -> (context.getDistance() != 0))
                    .filter(context -> context.getIsClosestGene() != null && context.getIsClosestGene())
                    .filter(GenomicContext::getIsUpstream)
                    .collect(Collectors.toMap(context -> context.getLocation().getId(),
                            context -> context.getGene().getGeneName().trim(), (existing, replacement) -> existing));
            closestDownstreamGeneNamesMap = gcs.stream()
                    .filter(context -> (context.getDistance() != 0))
                    .filter(context -> context.getIsClosestGene() != null && context.getIsClosestGene())
                    .filter(GenomicContext::getIsDownstream)
                    .collect(Collectors.toMap(context -> context.getLocation().getId(),
                            context -> context.getGene().getGeneName().trim(), (existing, replacement) -> existing));
        }

        mappedGenesToLocation.forEach(closestUpstreamGeneNamesMap::remove);
        mappedGenesToLocation.forEach(closestDownstreamGeneNamesMap::remove);
        closestUpstreamGeneNamesMap.keySet().forEach(locId -> locationUpDownStream.add(locId));
        closestDownstreamGeneNamesMap.keySet().forEach(locId -> locationUpDownStream.add(locId));
        List<String> allUpstreamAndDownstreamGenes = new ArrayList<>();
        for(Long locationId : locationUpDownStream) {
                    StringBuilder sbgeneUpDownStreamBuilder = new StringBuilder();
                    String upstreamGene = closestUpstreamGeneNamesMap.get(locationId);
                    String downStreamGene = closestDownstreamGeneNamesMap.get(locationId);
                    if (upstreamGene != null) {
                        sbgeneUpDownStreamBuilder.append(upstreamGene);
                    } else {
                        sbgeneUpDownStreamBuilder.append("N/A");
                    }
                    if (downStreamGene != null) {
                        sbgeneUpDownStreamBuilder.append(" - ");
                        sbgeneUpDownStreamBuilder.append(downStreamGene);
                    } else {
                        sbgeneUpDownStreamBuilder.append(" - ");
                        sbgeneUpDownStreamBuilder.append("N/A");
                    }
                    allUpstreamAndDownstreamGenes.add(sbgeneUpDownStreamBuilder.toString());
                }
        String geneString = "";
        if(!allMappedGenes.isEmpty()) {
            geneString = association.getMultiSnpHaplotype() ? String.join("; ", allMappedGenes)
                    : String.join(", ", allMappedGenes);
        } else if(!allUpstreamAndDownstreamGenes.isEmpty()) {
            geneString = association.getMultiSnpHaplotype() ? String.join("; ", allUpstreamAndDownstreamGenes)
                    : String.join(", ", allUpstreamAndDownstreamGenes);

        }
        return geneString;
    }

    private Boolean filterLocationforGene(GenomicContext context) {
        String location = context.getLocation().getChromosomeName();
        String pattern = "^\\d+$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(location);
        return (m.find() || location.equals("X") || location.equals("Y"));
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