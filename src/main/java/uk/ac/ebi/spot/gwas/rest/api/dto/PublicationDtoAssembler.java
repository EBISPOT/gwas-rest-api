package uk.ac.ebi.spot.gwas.rest.api.dto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.Publication;
import uk.ac.ebi.spot.gwas.model.PublicationAuthors;
import uk.ac.ebi.spot.gwas.rest.api.controller.PublicationsController;
import uk.ac.ebi.spot.gwas.rest.dto.PublicationDto;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PublicationDtoAssembler extends RepresentationModelAssemblerSupport<Publication, PublicationDto> {

    @Autowired
    PublicationAuthorDtoAssembler publicationAuthorDtoAssembler;

    public PublicationDtoAssembler() {
        super(PublicationsController.class, PublicationDto.class);
    }

    public PublicationDto toModel(Publication publication) {
        PublicationDto publicationDto =  PublicationDto.builder()
                .pubmedId(publication.getPubmedId())
                .publicationDate(publication.getPublicationDate())
                .authors(publication.getAuthors() != null ? publication.getPublicationAuthors().stream()
                        .map(PublicationAuthors::getAuthor)
                        .map(publicationAuthorDtoAssembler::assemble)
                        .collect(Collectors.toList()) : null)
                .firstAuthor(publication.getFirstAuthor() != null ?
                        publicationAuthorDtoAssembler.assemble(publication.getFirstAuthor()) : null)
                .journal(publication.getPublication())
                .title(publication.getTitle())
                .build();

        publicationDto.add(linkTo(methodOn(PublicationsController.class).getPublication(publication.getPubmedId())).withSelfRel());
        return publicationDto;
    }
}
