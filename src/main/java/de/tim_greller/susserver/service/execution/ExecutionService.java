package de.tim_greller.susserver.service.execution;

import de.tim_greller.susserver.dto.TestExecutionResultDTO;
import de.tim_greller.susserver.dto.TestSourceDTO;
import de.tim_greller.susserver.dto.TestStatus;
import de.tim_greller.susserver.exception.ClassLoadException;
import de.tim_greller.susserver.exception.NotFoundException;
import de.tim_greller.susserver.model.execution.compilation.InMemoryCompiler;
import de.tim_greller.susserver.model.execution.instrumentation.CoverageClassTransformer;
import de.tim_greller.susserver.model.execution.instrumentation.TestRunListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionService {

    private final CutService cutService;
    private final TestService testService;

    @Autowired
    public ExecutionService(CutService cutService, TestService testService) {
        this.cutService = cutService;
        this.testService = testService;
    }

    public TestExecutionResultDTO execute(String componentName, String userId)
            throws ClassLoadException, NotFoundException {
        var compiler = new InMemoryCompiler();
        var cutSource = cutService
                .getCutForComponent(componentName)
                .orElseThrow(() -> new NotFoundException("CUT for the specified component was not found"));
        var testSource = TestSourceDTO.fromTestEntity(testService.getOrCreateTestForComponent(componentName, userId));

        compiler.addSource(cutSource);
        compiler.addSource(testSource);
        compiler.addTransformer(new CoverageClassTransformer(), cutSource.getClassName());
        compiler.compile();

        Class<?> testClass = compiler.getClass(testSource.getClassName())
                .orElseThrow(() -> new ClassLoadException(
                        "Error loading the test class \"" + testSource.getClassName() + "\"."
                ));
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
