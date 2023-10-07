package de.tim_greller.susserver.model.execution.instrumentation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tim_greller.susserver.dto.TestDetailsDTO;
import de.tim_greller.susserver.dto.TestStatus;
import lombok.Getter;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

@Getter
public class TestRunListener extends RunListener {

    private final Map<String, TestDetailsDTO> map = new LinkedHashMap<>();

    private long testSuiteStartTime;
    private long testSuiteElapsedTime;
    private int ignoredCount;


    public void testRunStarted(Description description) {
        testSuiteStartTime = System.currentTimeMillis();
    }

    public void testStarted(Description description) {
        TestDetailsDTO testDetailsDTO = createTestSuiteDetails(description);
        testDetailsDTO.setTestStatus(TestStatus.PASSED);
        testDetailsDTO.setStartTime(System.currentTimeMillis());
    }

    private TestDetailsDTO createTestSuiteDetails(Description description) {
        // already add to map
        TestDetailsDTO testDetailsDTO = map.computeIfAbsent(
                description.getMethodName(),
                k -> new TestDetailsDTO()
        );

        // set names
        testDetailsDTO.setMethodName(description.getMethodName());

        String[] arr = description.getTestClass().getName().split("\\.");
        String name = arr[arr.length - 1];
        testDetailsDTO.setClassName(name);

        String[] arr1 = name.split("_");
        String testSuite = arr1[0];
        testDetailsDTO.setTestSuiteName(testSuite);

        return testDetailsDTO;
    }

    public void testFinished(Description description) {
        Optional.ofNullable(map.get(description.getMethodName())).ifPresent(TestDetailsDTO::setElapsedTime);
    }

    public void testRunFinished(org.junit.runner.Result result) {
        testSuiteElapsedTime = System.currentTimeMillis() - testSuiteStartTime;
    }

    @SuppressWarnings("removal")
    public void testFailure(Failure failure) {
        TestDetailsDTO testSuiteDetails = map.computeIfAbsent(
                failure.getDescription().getMethodName(),
                k -> new TestDetailsDTO()
        );
        testSuiteDetails.setMethodName(failure.getDescription().getMethodName());
        testSuiteDetails.setTrace(failure.getTrimmedTrace());
        testSuiteDetails.setTestStatus(TestStatus.FAILED);

        Throwable exc = failure.getException();
        if (exc instanceof AssertionError assertionError) {
            Matcher matcher = Pattern
                    .compile("^expected:<(?<expected>.*)> but was:<(?<actual>.*)>$")
                    .matcher(assertionError.getMessage());
            if (matcher.find()) {
                testSuiteDetails.setExpectedTestResult(matcher.group("expected"));
                testSuiteDetails.setActualTestResult(matcher.group("actual"));
            }
        } else if (exc instanceof java.security.AccessControlException ace) {
            testSuiteDetails.setAccessDenied(ace.getPermission().getName());
        }
    }

    public void testIgnored(Description description) {
        TestDetailsDTO testSuiteDetails = createTestSuiteDetails(description);
        ignoredCount++;
        testSuiteDetails.setTestStatus(TestStatus.IGNORED);
    }

}