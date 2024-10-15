package uk.ac.ebi.spot.gwas.rest.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.UnknownHostException;

@SpringBootApplication(scanBasePackages = "uk.ac.ebi.spot.gwas")
@EnableJpaRepositories("uk.ac.ebi.spot.gwas.rest.api.repository")
@EntityScan("uk.ac.ebi.spot.gwas.model")
@Slf4j
//public class RestApiApplication implements WebMvcConfigurer {
public class RestApiApplication {

    /*@Autowired
    private SystemConfigProperties systemConfigProperties;

    @PostConstruct
    public void init() {
        log.info("[{}] Initializing: {}", DateTime.now(), systemConfigProperties.getServerName());
    }

    @PreDestroy
    public void destroy() {
        log.info("[{}] Shutting down: {}", DateTime.now(), systemConfigProperties.getServerName());
    }
*/
    public static void main(String[] args) throws UnknownHostException {
        log.info("Inside Curation Application");
        /*String hostAddress = InetAddress.getLocalHost().getHostAddress();
        String logFileName = System.getenv(GeneralCommon.LOG_FILE_NAME);
        System.setProperty("log.file.name", logFileName + "-" + hostAddress);*/
        try {
            SpringApplication.run(RestApiApplication.class, args);
        } catch (Exception e) {
            log.error("Exception occured in main class"+e.getMessage(),e);
        }catch (Throwable e) {
            log.error("Throwable occured in main class"+e.getMessage(),e);
        }
    }
}
