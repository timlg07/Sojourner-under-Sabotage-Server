package de.tim_greller.susserver.controller.api;

import de.tim_greller.susserver.dto.CutSourceDTO;
import de.tim_greller.susserver.dto.TestExecutionResultDTO;
import de.tim_greller.susserver.dto.TestSourceDTO;
import de.tim_greller.susserver.dto.TestStatus;
import de.tim_greller.susserver.model.execution.compilation.InMemoryCompiler;
import de.tim_greller.susserver.model.execution.instrumentation.CoverageClassTransformer;
import de.tim_greller.susserver.model.execution.instrumentation.TestRunListener;
import de.tim_greller.susserver.service.execution.CutService;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExecutionController {

    private final CutService cutService;
    private final CutSourceDTO CUT = new CutSourceDTO(
            "Demo","Demo", """
                        public class Demo {
                            public static int add(int a, int b) {
                                return 3;
                            }
                        }
                        """);

    @Autowired
    public TestExecutionController(CutService cutService) {
        this.cutService = cutService;
    }

    @PostMapping(value = "${paths.api}/execute")
    public @ResponseBody TestExecutionResultDTO executeTest(@RequestBody TestSourceDTO testSource) {
        var res = new TestExecutionResultDTO();
        var compiler = new InMemoryCompiler();
        var cut = cutService.getCutForComponent(testSource.getCutComponentName()).orElse(CUT);

        compiler.addSource(cut);
        compiler.addSource(testSource);
        compiler.addTransformer(new CoverageClassTransformer(), CUT.getClassName());
        compiler.compile();

        Class<?> testClass = compiler.getClass(testSource.getClassName()).orElseThrow();
        TestRunListener listener = new TestRunListener();
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(listener);
        Result r = jUnitCore.run(testClass);

        res.setTestClassName(testSource.getClassName());
        res.setTestStatus(r.wasSuccessful() ? TestStatus.PASSED : TestStatus.FAILED);
        res.setTestDetails(listener.getMap());
        return res;
    }
}
