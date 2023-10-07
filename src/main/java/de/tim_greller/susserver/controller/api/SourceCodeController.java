package de.tim_greller.susserver.controller.api;

import java.text.MessageFormat;

import de.tim_greller.susserver.dto.CutSourceDTO;
import de.tim_greller.susserver.dto.PlainSource;
import de.tim_greller.susserver.dto.TestSourceDTO;
import de.tim_greller.susserver.service.auth.UserService;
import de.tim_greller.susserver.service.execution.CutService;
import de.tim_greller.susserver.service.execution.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SourceCodeController {

    private final CutService cutService;
    private final TestService testService;
    private final UserService userService;

    @Autowired
    public SourceCodeController(CutService cutService, TestService testService, UserService userService) {
        this.cutService = cutService;
        this.testService = testService;
        this.userService = userService;
    }

    @GetMapping("${paths.api}/components/{componentName}/cut/src")
    public CutSourceDTO getCutSourceCode(@PathVariable String componentName) {
        return cutService
                .getCutForComponent(componentName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MessageFormat.format(
                        "CUT for the specified component \"{0}\" was not found", componentName
                )));
    }

    @GetMapping("${paths.api}/components/{componentName}/test/src")
    public TestSourceDTO getTestSourceCode(@PathVariable String componentName) {
        return TestSourceDTO.fromTestEntity(
                testService.getOrCreateTestForComponent(
                        componentName,
                        userService.requireCurrentUserId()
                )
        );
    }

    @PutMapping("${paths.api}/components/{componentName}/test/src")
    public void updateTestSourceCode(@PathVariable String componentName,
                                     @RequestBody PlainSource newSource) {
        testService.updateTestForComponent(
                componentName,
                userService.requireCurrentUserId(),
                newSource.getCode()
        );
    }

}
