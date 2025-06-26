package uk.ac.ebi.spot.gwas.rest.api.controller;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.model.Locus;
import uk.ac.ebi.spot.gwas.rest.api.constants.EntityType;
import uk.ac.ebi.spot.gwas.rest.api.constants.RestAPIConstants;
import uk.ac.ebi.spot.gwas.rest.api.dto.LocusDtoAssembler;
import uk.ac.ebi.spot.gwas.rest.api.service.LocusService;
import uk.ac.ebi.spot.gwas.rest.dto.LocusDTO;

import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V2 + RestAPIConstants.API_ASSOCIATIONS)
public class LocusController {

    @Autowired
    LocusService locusService;

    @Autowired
    LocusDtoAssembler locusDtoAssembler;

    @GetMapping(value = "/{association_id}" + RestAPIConstants.API_LOCI, produces = MediaType.APPLICATION_JSON_VALUE)
    public CollectionModel<LocusDTO> getLoci(@PathVariable (name = "association_id") String associationId) {
      List<Locus> loci = locusService.findLociByAssociationId(Long.valueOf(associationId));
      return locusDtoAssembler.toCollectionModel(loci, Long.valueOf(associationId));
    }

    @GetMapping(value = "/{association_id}" + RestAPIConstants.API_LOCI + "/{locus_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LocusDTO> getLocus(@PathVariable (name = "association_id") String associationId, @PathVariable(name = "locus_id") String locusId) {
        return locusService.findByLocusId(Long.valueOf(locusId))
                .map(locusDtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(EntityType.LOCUS, "Locus id", locusId));

    }



}
