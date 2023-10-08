package de.tim_greller.susserver.controller.api;

import de.tim_greller.susserver.dto.PlainSource;
import de.tim_greller.susserver.dto.TestExecutionResultDTO;
import de.tim_greller.susserver.exception.ClassLoadException;
import de.tim_greller.susserver.exception.NotFoundException;
import de.tim_greller.susserver.exception.TestExecutionException;
import de.tim_greller.susserver.service.auth.UserService;
import de.tim_greller.susserver.service.execution.ExecutionService;
import de.tim_greller.susserver.service.execution.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TestExecutionController {

    private final TestService testService;
    private final UserService userService;
    private final ExecutionService executionService;

    @Autowired
    public TestExecutionController(TestService testService, UserService userService,
                                   ExecutionService executionService) {
        this.testService = testService;
        this.userService = userService;
        this.executionService = executionService;
    }

    @PostMapping(value = "${paths.api}/components/{componentName}/test/execute")
    public @ResponseBody TestExecutionResultDTO execute(@PathVariable String componentName,
                                                        @RequestBody PlainSource testSource) {
        // save the current source code
        testService.updateTestForComponent(
                componentName,
                userService.requireCurrentUserId(),
                testSource.getCode()
        );

        // compile and execute
        try {
            return executionService.execute(componentName, userService.requireCurrentUserId());
        } catch (ClassLoadException | TestExecutionException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
