package uk.ac.ebi.spot.gwas.rest.api.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.gwas.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.model.Locus;
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

    @GetMapping(value = "/{associationId}" + RestAPIConstants.API_LOCI, produces = MediaType.APPLICATION_JSON_VALUE)
    public CollectionModel<LocusDTO> getLoci(@PathVariable String associationId) {
      List<Locus> loci = locusService.findLociByAssociationId(Long.valueOf(associationId));
      return locusDtoAssembler.toCollectionModel(loci, Long.valueOf(associationId));
    }

    @GetMapping(value = "/{associationId}" + RestAPIConstants.API_LOCI + "/{locusId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public LocusDTO getLocus(@PathVariable String associationId, @PathVariable String locusId) {
      Locus locus =  locusService.findByLocusId(Long.valueOf(locusId));
      return locusDtoAssembler.toModel(locus, Long.valueOf(associationId));
    }



}
