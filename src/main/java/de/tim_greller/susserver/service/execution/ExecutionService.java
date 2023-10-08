package de.tim_greller.susserver.service.execution;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.tim_greller.susserver.dto.TestExecutionResultDTO;
import de.tim_greller.susserver.dto.TestSourceDTO;
import de.tim_greller.susserver.dto.TestStatus;
import de.tim_greller.susserver.exception.ClassLoadException;
import de.tim_greller.susserver.exception.NotFoundException;
import de.tim_greller.susserver.exception.TestExecutionException;
import de.tim_greller.susserver.exception.TestExecutionTimedOut;
import de.tim_greller.susserver.model.execution.compilation.InMemoryCompiler;
import de.tim_greller.susserver.model.execution.instrumentation.CoverageClassTransformer;
import de.tim_greller.susserver.model.execution.instrumentation.TestRunListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionService {

    private static final int MAX_TEST_EXECUTION_TIME_SECONDS = 3;
    private final CutService cutService;
    private final TestService testService;

    @Autowired
    public ExecutionService(CutService cutService, TestService testService) {
        this.cutService = cutService;
        this.testService = testService;
    }

    public TestExecutionResultDTO execute(String componentName, String userId)
            throws ClassLoadException, NotFoundException, TestExecutionException {
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
        Result r = run(testClass, listener);

        var res = new TestExecutionResultDTO();
        res.setTestClassName(testSource.getClassName());
        res.setTestStatus(r.wasSuccessful() ? TestStatus.PASSED : TestStatus.FAILED);
        res.setTestDetails(listener.getMap());
        return res;
    }

    private Result run(Class<?> testClass, TestRunListener listener) throws TestExecutionException {
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(listener);
        final ExecutorService exService = Executors.newSingleThreadExecutor();
        Future<Result> res = exService.submit(() -> jUnitCore.run(testClass));
        try {
            // This blocks the current request thread.
            return res.get(MAX_TEST_EXECUTION_TIME_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new TestExecutionTimedOut(MAX_TEST_EXECUTION_TIME_SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new TestExecutionException("Error while executing the test", e);
        } finally {
            List<Runnable> r = exService.shutdownNow();
            System.out.println("executor service shutdown. Remaining tasks: " + r.size());
        }
    }

}
