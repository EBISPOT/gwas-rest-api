package uk.ac.ebi.spot.gwas.rest.api.config;

import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This class is used to skip access logging for static resources by setting the skipLogging attribute in requests to true.
 * The attribute is used in the LoggingFilter to skip logging for static resources.
 * It's referenced in application properties through server.tomcat.accesslog.condition-unless: skipLogging.
 */
@Configuration
public class SkipStaticLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String path = ((HttpServletRequest) req).getRequestURI();
        if (path.matches(".*\\.(css|js|png|jpg|jpeg|gif|ico|woff2?|ttf|svg)$")) {
            req.setAttribute("skipLogging", Boolean.TRUE);
        }
        chain.doFilter(req, res);
    }
}
