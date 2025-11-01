package com.cineclub_backend.cineclub_backend.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ScalarController {

    @GetMapping(value = "/docs", produces = "text/html")
    @ResponseBody
    public String scalarDocs() {
        return """
                <!doctype html>
                <html>
                <head>
                    <title>Cineclub Backend API</title>
                    <meta charset="utf-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                </head>
                <body>
                    <script
                        id="api-reference"
                        data-url="/v3/api-docs"
                        data-configuration='{"theme":"purple"}'
                    ></script>
                    <script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
                </body>
                </html>
                """;
    }
}
