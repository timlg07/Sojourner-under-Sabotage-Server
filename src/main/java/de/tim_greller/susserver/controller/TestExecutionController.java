package de.tim_greller.susserver.controller;

import de.tim_greller.susserver.dto.TestExecutionResultDTO;
import de.tim_greller.susserver.dto.TestSourceDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExecutionController {

    @PostMapping(
            value = "/api/execute",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody TestExecutionResultDTO executeTest(@RequestBody TestSourceDTO testSource) {
        var res = new TestExecutionResultDTO();
        res.setTestClassName(testSource.getClassName());
        return res;
    }

    @GetMapping("/api/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok().body("Hello World");
    }
}
