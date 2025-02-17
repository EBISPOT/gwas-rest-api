package uk.ac.ebi.spot.gwas.rest.api.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RestAPIConfiguration {

    @Value("${sumstats.link:#{NULL}}")
    private String ftpSumStatsLink;

    @Value("${terms-of-use.link:#{NULL}}")
    private String termsOfUseLink;

    @Value("${ensembl.server.url:#{NULL}}")
    private String ensemblServerUrl;

    @Value("${ensembl.mapping.version:#{NULL}}")
    private String ensemblVersion;

    @Value("${cco.exception.pmids:#{NULL}}")
    private String ccoExceptionPmids;

    @Value("${cco.link:#{NULL}}")
    private String ccoLink;

    @Value("${cco.exception.readme-text:#{NULL}}")
    private String ccoReadmeText;
}
