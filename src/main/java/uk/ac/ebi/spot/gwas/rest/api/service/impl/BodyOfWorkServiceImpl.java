package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.model.BodyOfWork;
import uk.ac.ebi.spot.gwas.rest.api.repository.BodyOfWorkRepository;
import uk.ac.ebi.spot.gwas.rest.api.service.BodyOfWorkService;

import java.util.Optional;

@Service
public class BodyOfWorkServiceImpl implements BodyOfWorkService {

    BodyOfWorkRepository bodyOfWorkRepository;

    @Autowired
    public BodyOfWorkServiceImpl(BodyOfWorkRepository bodyOfWorkRepository) {
        this.bodyOfWorkRepository = bodyOfWorkRepository;
    }

    public Page<BodyOfWork> getBodyOfWork(String title, String firstAuthor, Pageable pageable) {
        if(title != null && firstAuthor != null) {
            return bodyOfWorkRepository.findByTitleContainingIgnoreCaseAndFirstAuthorContainingIgnoreCase(title, firstAuthor, pageable);
        }
        if(title != null) {
            return bodyOfWorkRepository.findByTitleContainingIgnoreCase(title, pageable);
        }
        if(firstAuthor != null) {
            return bodyOfWorkRepository.findByFirstAuthorContainingIgnoreCase(firstAuthor, pageable);
        }
        return bodyOfWorkRepository.findAll(pageable);
    }

   public Optional<BodyOfWork> getBodyOfWork(String bowId) {
       return bodyOfWorkRepository.findByPublicationId(bowId);
   }
}
