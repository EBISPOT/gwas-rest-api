package uk.ac.ebi.spot.gwas.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.model.EfoTrait;
import uk.ac.ebi.spot.gwas.rest.dto.SearchEfoParams;

import java.util.Optional;

public interface EFOTraitService {

    Page<EfoTrait> getEFOTraits(SearchEfoParams searchEfoParams, Pageable pageable);

    EfoTrait getEFOTrait(String shortForm);
}
