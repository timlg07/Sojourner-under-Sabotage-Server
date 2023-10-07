package de.tim_greller.susserver.service.execution;

import de.tim_greller.susserver.dto.TestExecutionResultDTO;
import de.tim_greller.susserver.dto.TestSourceDTO;
import de.tim_greller.susserver.dto.TestStatus;
import de.tim_greller.susserver.model.execution.compilation.InMemoryCompiler;
import de.tim_greller.susserver.model.execution.instrumentation.CoverageClassTransformer;
import de.tim_greller.susserver.model.execution.instrumentation.TestRunListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExecutionService {

    private final CutService cutService;
    private final TestService testService;

    @Autowired
    public ExecutionService(CutService cutService, TestService testService) {
        this.cutService = cutService;
        this.testService = testService;
    }

    public TestExecutionResultDTO execute(String componentName, String userId) {
        var compiler = new InMemoryCompiler();
        var cutSource = cutService
                .getCutForComponent(componentName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "CUT for the specified component was not found"
                ));
        var testSource = TestSourceDTO.fromTestEntity(testService.getOrCreateTestForComponent(componentName, userId));

        compiler.addSource(cutSource);
        compiler.addSource(testSource);
        compiler.addTransformer(new CoverageClassTransformer(), cutSource.getClassName());
        compiler.compile();

        Class<?> testClass = compiler.getClass(testSource.getClassName()).orElseThrow();
        TestRunListener listener = new TestRunListener();
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(listener);
        Result r = jUnitCore.run(testClass);

        var res = new TestExecutionResultDTO();
        res.setTestClassName(testSource.getClassName());
        res.setTestStatus(r.wasSuccessful() ? TestStatus.PASSED : TestStatus.FAILED);
        res.setTestDetails(listener.getMap());
        return res;
    }

}
