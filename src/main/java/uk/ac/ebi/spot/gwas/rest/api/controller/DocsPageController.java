package uk.ac.ebi.spot.gwas.rest.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsPageController {

    @GetMapping("/v2/docs")
    public String showDocsPage() {
        return "docs-template";
    }
}
