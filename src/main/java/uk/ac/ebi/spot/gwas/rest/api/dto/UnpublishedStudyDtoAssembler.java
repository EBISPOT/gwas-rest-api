package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.BodyOfWork;
import uk.ac.ebi.spot.gwas.model.UnpublishedStudy;
import uk.ac.ebi.spot.gwas.rest.api.config.RestAPIConfiguration;
import uk.ac.ebi.spot.gwas.rest.api.controller.BodyOfWorkController;
import uk.ac.ebi.spot.gwas.rest.api.controller.UnpublishedAncestriesController;
import uk.ac.ebi.spot.gwas.rest.api.controller.UnpublishedStudiesController;
import uk.ac.ebi.spot.gwas.rest.dto.UnpublishedStudyDTO;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UnpublishedStudyDtoAssembler extends RepresentationModelAssemblerSupport<UnpublishedStudy, UnpublishedStudyDTO> {

    @Autowired
    RestAPIConfiguration restAPIConfiguration;

    public UnpublishedStudyDtoAssembler() {
        super(UnpublishedStudiesController.class, UnpublishedStudyDTO.class);
    }

    @Override
    public UnpublishedStudyDTO toModel(UnpublishedStudy unpublishedStudy) {

        BodyOfWork bodyOfWork = getBodoyofWork(unpublishedStudy);
        UnpublishedStudyDTO unpublishedStudyDTO = UnpublishedStudyDTO.builder()
                .accession(unpublishedStudy.getAccession())
                .agreedToCc0(unpublishedStudy.getAgreedToCc0())
                .arrayInformation(unpublishedStudy.getArrayInformation())
                .arrayManufacturer(unpublishedStudy.getArrayManufacturer())
                .trait(unpublishedStudy.getTrait())
                .backgroundEfoTrait(unpublishedStudy.getBackgroundEfoTrait())
                .backgroundTrait(unpublishedStudy.getBackgroundTrait())
                .cohort(unpublishedStudy.getCohort())
                .cohortId(unpublishedStudy.getCohortId())
                .genotypingTechnology(unpublishedStudy.getGenotypingTechnology())
                .imputation(unpublishedStudy.getImputation())
                .studyDescription(unpublishedStudy.getStudyDescription())
                .efoTrait(unpublishedStudy.getEfoTrait())
                .statisticalModel(unpublishedStudy.getStatisticalModel())
                .sumStatsAssembly(unpublishedStudy.getSumStatsAssembly())
                .sampleDescription(unpublishedStudy.getSampleDescription())
                .variantCount(unpublishedStudy.getVariantCount())
                .fullSummaryStats(unpublishedStudy.getAccession() != null ? this.getSummaryStatsFTPDetails(unpublishedStudy.getAccession()) : "NA")
                .firstAuthor(bodyOfWork != null ? bodyOfWork.getFirstAuthor() : null)
                .title(bodyOfWork != null ? bodyOfWork.getTitle() : null)
                .build();
        unpublishedStudyDTO.add(linkTo(methodOn(UnpublishedStudiesController.class).getUnpublishedStudy(unpublishedStudy.getAccession())).withSelfRel());
        unpublishedStudyDTO.add(linkTo(methodOn(UnpublishedAncestriesController.class).getUnpublishedAncestries(unpublishedStudy.getAccession())).withRel("ancestries"));


        if (bodyOfWork != null) {
            unpublishedStudyDTO.add(linkTo(methodOn(BodyOfWorkController.class).getBodyOfWork(bodyOfWork.getId())).withRel("body_of_work"));
        }
        return unpublishedStudyDTO;
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

    private BodyOfWork getBodoyofWork(UnpublishedStudy unpublishedStudy) {
       return  unpublishedStudy.getBodiesOfWork().stream().findFirst().orElse(null);
    }


}
