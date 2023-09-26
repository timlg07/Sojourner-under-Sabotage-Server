package de.tim_greller.susserver.controller.api;

import java.util.stream.Collectors;

import de.tim_greller.susserver.dto.TestExecutionResultDTO;
import de.tim_greller.susserver.dto.TestSourceDTO;
import de.tim_greller.susserver.model.execution.compilation.InMemoryCompiler;
import de.tim_greller.susserver.model.execution.instrumentation.CoverageClassTransformer;
import de.tim_greller.susserver.model.execution.instrumentation.TestRunListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExecutionController {

    private final TestSourceDTO CUT = new TestSourceDTO(
            "Demo","""
                        public class Demo {
                            public static int add(int a, int b) {
                                return a + b;
                            }
                        }
                        """);
    @PostMapping(value = "${paths.api}/execute")
    public @ResponseBody TestExecutionResultDTO executeTest(@RequestBody TestSourceDTO testSource) {
        var res = new TestExecutionResultDTO();
        var compiler = new InMemoryCompiler();
        compiler.addSource(CUT);
        compiler.addSource(testSource);
        compiler.addTransformer(new CoverageClassTransformer(), CUT.getClassName());
        compiler.compile();
        Class<?> testClass = compiler.getClass(testSource.getClassName()).orElseThrow();
        TestRunListener listener = new TestRunListener();
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(listener);
        Result r = jUnitCore.run(testClass);
        res.setTestClassName(testSource.getClassName());
        res.setTestStatus(r.wasSuccessful() ? TestExecutionResultDTO.TestStatus.PASSED : TestExecutionResultDTO.TestStatus.FAILED);
        res.setTestDetails(listener.getMap().values().stream().map(testSuiteDetails -> new TestExecutionResultDTO.TestDetails(
                testSuiteDetails.getTestCaseName(),
                TestExecutionResultDTO.TestStatus.valueOf(testSuiteDetails.getTestStatus().toString()),
                testSuiteDetails.getActualTestResult(),
                testSuiteDetails.getExpectedTestResult(),
                testSuiteDetails.getTestDescription())).collect(Collectors.toMap(TestExecutionResultDTO.TestDetails::getMethodName, testDetails -> testDetails)));
        return res;
    }
}
