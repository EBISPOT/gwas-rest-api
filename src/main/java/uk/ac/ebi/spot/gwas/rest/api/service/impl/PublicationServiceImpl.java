package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.Publication;
import uk.ac.ebi.spot.gwas.rest.api.repository.PublicationRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.PublicationService;

import java.util.Optional;

@Service
public class PublicationServiceImpl implements PublicationService {

    PublicationRepository publicationRepository;

    public PublicationServiceImpl(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    @Override
    public Page<Publication> findPublications(String pubmedId, String title,String firstAuthor, Pageable pageable) {

        if(pubmedId != null && title != null && firstAuthor != null) {
            return publicationRepository.findByPubmedIdEqualsAndTitleContainingIgnoreCaseAndFirstAuthorFullnameContainingIgnoreCase(pubmedId, title, firstAuthor, pageable);
        }

        if(pubmedId != null && firstAuthor != null) {
            return publicationRepository.findByPubmedIdEqualsAndFirstAuthorFullnameContainingIgnoreCase(pubmedId, firstAuthor, pageable);
        }

        if( title != null && firstAuthor != null) {
            return publicationRepository.findByTitleContainingIgnoreCaseAndFirstAuthorFullnameContainingIgnoreCase(title, firstAuthor, pageable);
        }

        if(pubmedId != null && title != null) {
            return publicationRepository.findByPubmedIdEqualsAndTitleContainingIgnoreCase(pubmedId, title, pageable);
        }
        if(pubmedId != null) {
            return publicationRepository.findByPubmedIdEquals(pubmedId, pageable);
        }
        if(title != null) {
            return publicationRepository.findByTitleContainingIgnoreCase(title, pageable);
        }
        if(firstAuthor != null) {
            return publicationRepository.findByFirstAuthorFullnameContainingIgnoreCase(firstAuthor, pageable);
        }
       return publicationRepository.findAll(pageable);
    }

    public Optional<Publication> findPublicationByPmid(String pmid) {
       return publicationRepository.findByPubmedIdEquals(pmid);
    }
}
