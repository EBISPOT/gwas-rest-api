package uk.ac.ebi.spot.gwas.rest.api.dto.solr.slim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneSolrDto {

    @JsonProperty("resourcename")
    private String resourceName;

    @JsonProperty("id")
    private String id;

    @JsonProperty("ensemblID")
    private String ensemblId;

    @JsonProperty("rsIDs")
    private List<String> rsIds;

    @JsonProperty("studyCount")
    private int studyCount;

    @JsonProperty("associationCount")
    private int associationCount;

    @JsonProperty("chromosomeStart")
    private long chromosomeStart;

    @JsonProperty("chromosomeEnd")
    private long chromosomeEnd;

    @JsonProperty("chromosomeName")
    private String chromosomeName;

    @JsonProperty("biotype")
    private String biotype;

    @JsonProperty("title")
    private String title;

    @JsonProperty("ensemblDescription")
    private String ensemblDescription;

    @JsonProperty("crossRefs")
    private String crossRefs;

    @JsonProperty("entrez_id")
    private String entrezId;

    @JsonProperty("cytobands")
    private String cytobands;

    @JsonProperty("description")
    private String description;
}
