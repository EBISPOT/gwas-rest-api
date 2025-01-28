package uk.ac.ebi.spot.gwas.rest.api.dto;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.model.Author;
import uk.ac.ebi.spot.gwas.rest.dto.PublicationAuthorDto;

@Component
public class PublicationAuthorDtoAssembler {

  public PublicationAuthorDto assemble(Author author) {
   return PublicationAuthorDto.builder()
              .affiliation(author.getAffiliation())
              .firstName(author.getFirstName())
              .fullName(author.getFullname())
              .fullNameStandard(author.getFullnameStandard())
              .initials(author.getInitials())
              .orcid(author.getOrcid())
              .lastName(author.getLastName())
              .build();
  }

}
