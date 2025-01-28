package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.model.Publication;

public interface PublicationService {


   Page<Publication> findPublications(String pubmedId, String title, Pageable pageable);

   Publication findPublicationByPmid(String pmid);
}
