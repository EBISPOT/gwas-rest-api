package uk.ac.ebi.spot.gwas.rest.api.dto;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.BodyOfWork;
import uk.ac.ebi.spot.gwas.rest.api.controller.BodyOfWorkController;
import uk.ac.ebi.spot.gwas.rest.dto.BodyOfWorkDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BodyOfWorkDtoAssembler extends RepresentationModelAssemblerSupport<BodyOfWork, BodyOfWorkDTO> {

    public BodyOfWorkDtoAssembler() {
        super(BodyOfWorkController.class, BodyOfWorkDTO.class);
    }

    @Override
    public BodyOfWorkDTO toModel(BodyOfWork bodyOfWork) {
        BodyOfWorkDTO bodyOfWorkDTO = BodyOfWorkDTO.builder()
                .doi(bodyOfWork.getDoi())
                .firstAuthor(bodyOfWork.getFirstAuthor())
                .publicationDate(bodyOfWork.getPublicationDate())
                .title(bodyOfWork.getTitle())
                .build();

        bodyOfWorkDTO.add(linkTo(methodOn(BodyOfWorkController.class).getBodyOfWork(bodyOfWork.getPublicationId())).withSelfRel());
        bodyOfWorkDTO.add(linkTo(methodOn(BodyOfWorkController.class).getUnpublishedStudies(bodyOfWork.getPublicationId(), null)).withRel("body_of_works"));
        return bodyOfWorkDTO;
    }
}
