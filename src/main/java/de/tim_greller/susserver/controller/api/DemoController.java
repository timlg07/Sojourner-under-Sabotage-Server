package de.tim_greller.susserver.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("${paths.api}/hello")
    public ResponseEntity<Object> hello() {
        return ResponseEntity.ok().body(new Object() {
            public final String message = "Hello World!";
        });
    }
}
