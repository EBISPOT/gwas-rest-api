package uk.ac.ebi.spot.gwas.rest.api.dto.solr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolrApiResponse<T> {

    @JsonProperty("responseHeader")
    private ResponseHeader responseHeader;

    @JsonProperty("response")
    private Response<T> response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseHeader {
        private int status;
        private int QTime;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response<T> {
        private int numFound;
        private int start;
        private List<T> docs;
    }
}
