package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.*;
import uk.ac.ebi.spot.gwas.rest.api.config.RestAPIConfiguration;
import uk.ac.ebi.spot.gwas.rest.api.controller.AncestryController;
import uk.ac.ebi.spot.gwas.rest.api.controller.StudiesController;
import uk.ac.ebi.spot.gwas.rest.dto.StudyDto;
import java.util.Set;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;



@Component
public class StudyDtoAssembler extends RepresentationModelAssemblerSupport<Study, StudyDto> {

    @Autowired
    EFOWrapperDtoAssembler efoWrapperDtoAssembler;

    @Autowired
    RestAPIConfiguration restAPIConfiguration;

    public StudyDtoAssembler() {
        super(StudiesController.class, StudyDto.class);
    }


    @Override
    public StudyDto toModel(Study study) {


        StudyDto studyDto = StudyDto.builder()
                .accessionId(study.getAccessionId())
                .diseaseTrait(study.getDiseaseTrait() != null ? study.getDiseaseTrait().getTrait() : null)
                .studyDesignComment(study.getStudyDesignComment())
                .fullPvalueSet(study.getFullPvalueSet())
                .gxe(study.getGxe())
                .efoTraits(study.getEfoTraits() != null ?  study.getEfoTraits().stream()
                        .map(efoWrapperDtoAssembler::assemble)
                        .collect(Collectors.toList()) : null)
                .bgEfoTraits(study.getMappedBackgroundTraits() != null ? study.getMappedBackgroundTraits().stream()
                        .map(efoWrapperDtoAssembler::assemble)
                        .collect(Collectors.toList()) : null)
                .initialSampleSize(study.getInitialSampleSize())
                .replicationSampleSize(study.getReplicateSampleSize())
                .gxg(study.getGxg())
                .snpCount(study.getSnpCount())
                .qualifier(study.getQualifier())
                .imputed(study.getImputed())
                .pooled(study.getPooled())
                .userRequested(study.getUserRequested())
                .pubmedId(study.getPublicationId() != null ? new Integer(study.getPublicationId().getPubmedId()) : null )
                .genotypingTechnologies(study.getGenotypingTechnologies() != null ? this.getGenoTypeTech(study) : null)
                .platforms(study.getPlatforms() != null ? this.getPlatforms(study) : null)
                .discoveryAncestry(study.getAncestries() != null ? this.getInitialAncestryLinks(study) : null)
                .replicationAncestry(study.getAncestries() != null ? this.getReplicationAncestryLinks(study) : null)
                .fullSummaryStats(study.getFullPvalueSet() && study.getAccessionId() != null ? this.getSummaryStatsFTPDetails(study.getAccessionId()) : "NA")
                .termsOfLicense(study.isAgreedToCc0() != null && study.isAgreedToCc0() ? restAPIConfiguration.getTermsOfUseLink() : "NA")
                .build();

        //studyDto.add(linkTo(methodOn(StudiesController.class).getStudy(String.valueOf(study.getId()))).withSelfRel());
        studyDto.add(linkTo(methodOn(StudiesController.class).getStudyByAccession(study.getAccessionId())).withSelfRel());
        studyDto.add(linkTo(methodOn(AncestryController.class).getAncestries(study.getAccessionId())).withRel("ancestries"));
        return studyDto;
    }


    private Set<String> getInitialAncestryLinks(Study study) {

        return study.getAncestries().stream()
                .filter(ancestry -> ancestry.getType().equals("initial"))
                .map(this::getAncestryDesc)
                .collect(Collectors.toSet());
    }

    private Set<String> getReplicationAncestryLinks(Study study) {

        return study.getAncestries().stream()
                .filter(ancestry -> ancestry.getType().equals("replication"))
                .map(this::getAncestryDesc)
                .collect(Collectors.toSet());
    }


    private String getAncestryDesc(Ancestry ancestry) {
        StringBuilder ancestryDescBuilder = new StringBuilder();
        if (ancestry.getNumberOfIndividuals() != null) {
            ancestryDescBuilder.append(ancestry.getNumberOfIndividuals());
        } else {
            ancestryDescBuilder.append("NA");
        }
        ancestryDescBuilder.append(" ");
        StringBuilder ancestryGroupBuilder = new StringBuilder();
        if (ancestry.getAncestralGroups() != null && !ancestry.getAncestralGroups().isEmpty()) {
            for (AncestralGroup ancestralGroup : ancestry.getAncestralGroups()) {
                if (ancestryGroupBuilder.length() == 0) {
                    ancestryGroupBuilder.append(ancestralGroup.getAncestralGroup());
                } else {
                    ancestryGroupBuilder.append(",");
                    ancestryGroupBuilder.append(ancestralGroup.getAncestralGroup());
                }
            }
        } else {
            ancestryGroupBuilder.append("NR");
        }
        StringBuilder countryOfRecruitmentBuilder = new StringBuilder();

        if(ancestry.getCountryOfRecruitment() != null && !ancestry.getCountryOfRecruitment().isEmpty()) {
            for(Country cor : ancestry.getCountryOfRecruitment()) {
                if(countryOfRecruitmentBuilder.length() == 0) {
                    countryOfRecruitmentBuilder.append(" (");
                    countryOfRecruitmentBuilder.append(cor.getCountryName());
                } else {
                    countryOfRecruitmentBuilder.append(", ").append(cor.getCountryName());
                }
            }
        } else{
            countryOfRecruitmentBuilder.append("NR");
        }
        countryOfRecruitmentBuilder.append(")");
        ancestryDescBuilder.append(ancestryGroupBuilder);
        ancestryDescBuilder.append(countryOfRecruitmentBuilder);
        return ancestryDescBuilder.toString();
    }

    private String getSummaryStatsFTPDetails(String accessionId) {
        String ftpDir = getDirectoryBin(accessionId);
        return restAPIConfiguration.getFtpSumStatsLink().concat(ftpDir).concat("/").concat(accessionId);
    }

    private String getDirectoryBin(String accessionId) {
        String accId = accessionId.substring(accessionId.indexOf("GCST")+4);
        int gsctNum = Integer.parseInt(accId);
        int lowerRange = (int) (Math.floor((gsctNum-1)/1000))*1000+1;
        int upperRange = (int) (Math.floor((gsctNum-1)/1000)+1)*1000;
        String range = "GCST"+ StringUtils.leftPad(String.valueOf(lowerRange),6,"0")
                +"-GCST"+StringUtils.leftPad(String.valueOf(upperRange),6,"0");
        return range;
    }

    private String getPlatforms(Study study) {
        StringBuilder platformBuilder = new StringBuilder();
        String manufacturers = "";
        if (study.getPlatforms() != null && !study.getPlatforms().isEmpty()) {
            manufacturers = study.getPlatforms().stream()
                    .map(Platform::getManufacturer)
                    .sorted()
                    .collect(Collectors.joining(","));
        } else {
            manufacturers = "NR";
        }

        platformBuilder.append(manufacturers);
        platformBuilder.append(" [");

        if (study.getQualifier() != null) {
            platformBuilder.append(study.getQualifier()).append(" ");
        }
        if (study.getSnpCount() != null) {
            platformBuilder.append(study.getSnpCount()).append("]");
        } else if (study.getSnpCount() == null && (study.getPlatforms() != null &&
                !study.getPlatforms().isEmpty()) && study.getStudyDesignComment() != null) {
            platformBuilder.append(study.getStudyDesignComment()).append("]");
        } else {
            platformBuilder.append("NR]");
        }

        if (study.getImputed()) {
            platformBuilder.append(" ").append("(imputed)");
        }

        if ((study.getPlatforms() == null || study.getPlatforms().isEmpty()) && study.getStudyDesignComment() != null) {
            return study.getStudyDesignComment();
        }

        return platformBuilder.toString();
    }

    private Set<String> getGenoTypeTech(Study study) {
       return study.getGenotypingTechnologies()
                .stream()
                .map(GenotypingTechnology::getGenotypingTechnology)
                .collect(Collectors.toSet());
    }


}