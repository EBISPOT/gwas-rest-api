package uk.ac.ebi.spot.gwas.rest.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerEmbedPageController {

    @GetMapping("/docs/reference")
    public String swaggerDocsPage() {
        return "swagger-template";
    }
}
