package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.Publication;
import uk.ac.ebi.spot.gwas.rest.api.repository.PublicationRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.PublicationService;

@Service
public class PublicationServiceImpl implements PublicationService {

    PublicationRepository publicationRepository;

    public PublicationServiceImpl(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    @Override
    public Page<Publication> findPublications(String pubmedId, String title,Pageable pageable) {
        if(pubmedId != null && title != null) {
            return publicationRepository.findByPubmedIdEqualsAndTitleContainingIgnoreCase(pubmedId, title, pageable);
        }
        if(pubmedId != null) {
            return publicationRepository.findByPubmedIdEquals(pubmedId, pageable);
        }
        if(title != null) {
            return publicationRepository.findByTitleContainingIgnoreCase(title, pageable);
        }
       return publicationRepository.findAll(pageable);
    }

    public Publication findPublicationByPmid(String pmid) {
       return publicationRepository.findByPubmedIdEquals(pmid).orElse(null);
    }
}
