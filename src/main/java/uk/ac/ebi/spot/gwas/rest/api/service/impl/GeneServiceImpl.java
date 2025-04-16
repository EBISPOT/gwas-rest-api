package uk.ac.ebi.spot.gwas.rest.api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.SolrApiResponse;
import uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim.GeneSolrDto;
import uk.ac.ebi.spot.gwas.rest.api.service.GeneService;

@Service
public class GeneServiceImpl implements GeneService {

    private final SlimSolrServiceImpl slimSolrService;

    public GeneServiceImpl(SlimSolrServiceImpl slimSolrService) {
        this.slimSolrService = slimSolrService;
    }

    @Override
    public GeneSolrDto getGeneByName(String geneName) {
        SolrApiResponse<GeneSolrDto> solrApiResponse = slimSolrService.fetchGeneData(geneName);
        if (solrApiResponse.getResponse().getDocs().isEmpty()) {
            throw new EntityNotFoundException(geneName);
        }
        return solrApiResponse.getResponse().getDocs().get(0);
    }

    @Override
    public Page<GeneSolrDto> getGenes(Pageable pageable) {
        SolrApiResponse<GeneSolrDto> genesSolrResponse = slimSolrService.fetchGenes(pageable);
        Page<GeneSolrDto> geneSolrDtos = new PageImpl<>(genesSolrResponse.getResponse().getDocs(), pageable,
                genesSolrResponse.getResponse().getNumFound());
        return geneSolrDtos;
    }
}
