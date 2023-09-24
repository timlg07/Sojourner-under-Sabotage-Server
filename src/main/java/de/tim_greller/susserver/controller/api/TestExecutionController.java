package de.tim_greller.susserver.controller.api;

import de.tim_greller.susserver.dto.TestExecutionResultDTO;
import de.tim_greller.susserver.dto.TestSourceDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExecutionController {

    @PostMapping(value = "${paths.api}/execute")
    public @ResponseBody TestExecutionResultDTO executeTest(@RequestBody TestSourceDTO testSource) {
        var res = new TestExecutionResultDTO();
        res.setTestClassName(testSource.getClassName());
        return res;
    }
}
