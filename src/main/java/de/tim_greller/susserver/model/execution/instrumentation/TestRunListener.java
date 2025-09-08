package de.tim_greller.susserver.model.execution.instrumentation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static de.tim_greller.susserver.dto.TestStatus.IGNORED;

import de.tim_greller.susserver.dto.TestDetailsDTO;
import de.tim_greller.susserver.dto.TestStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

@Slf4j
@Getter
public class TestRunListener implements TestExecutionListener {

    /**
     * The map of test suite details. Maps the test method name to the details.
     */
    private final Map<String, TestDetailsDTO> map = new LinkedHashMap<>();

    private long testSuiteStartTime;
    private long testSuiteElapsedTime;
    private int ignoredCount;


    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        testSuiteStartTime = System.currentTimeMillis();
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            TestDetailsDTO testDetailsDTO = createTestSuiteDetails(testIdentifier);
            testDetailsDTO.setTestStatus(TestStatus.PASSED);
            testDetailsDTO.setStartTime(System.currentTimeMillis());
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            var methodName = methodName(testIdentifier);
            Optional.ofNullable(map.get(methodName)).ifPresent(TestDetailsDTO::setElapsedTime);
            var status = testExecutionResult.getStatus();

            switch (status) {
                case FAILED, ABORTED -> handleTestFailure(testIdentifier, testExecutionResult);
            }
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        testSuiteElapsedTime = System.currentTimeMillis() - testSuiteStartTime;
    }

    @SuppressWarnings("removal")
    private void handleTestFailure(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        var methodName = methodName(testIdentifier);
        TestDetailsDTO testSuiteDetails = map.computeIfAbsent(methodName, k -> new TestDetailsDTO());
        testSuiteDetails.setMethodName(methodName);
        
        Throwable exc = testExecutionResult.getThrowable().orElse(null);
        if (exc == null) {
            log.info("Test failed without exception ({})", testExecutionResult);
            return;
        }

        testSuiteDetails.setTrace(getStackTrace(exc));
        testSuiteDetails.setTestStatus(TestStatus.FAILED);

        if (exc instanceof AssertionError assertionError) {
            // Handle assertEquals / assertTrue / assertFalse / assertNull violation
            var matcher = Pattern
                    .compile("^expected:\\s?<(?<expected>.*)> but was:\\s?<(?<actual>.*)>$")
                    .matcher(assertionError.getMessage());
            if (matcher.find()) {
                testSuiteDetails.setExpectedTestResult(matcher.group("expected"));
                testSuiteDetails.setActualTestResult(matcher.group("actual"));
                return;
            }
            // Handle assertNotEquals violation
            matcher = Pattern.compile("^expected:\\s?not equal but was:\\s?<(?<actual>.*)>$")
                    .matcher(assertionError.getMessage());
            if (matcher.find()) {
                var actual = matcher.group("actual");
                testSuiteDetails.setExpectedTestResult("anything else, but not " + actual);
                testSuiteDetails.setActualTestResult(actual);
                return;
            }
            // Handle assertNotNull violation
            matcher = Pattern.compile("^expected: not <null>$").matcher(assertionError.getMessage());
            if (matcher.find()) {
                testSuiteDetails.setExpectedTestResult("not null");
                testSuiteDetails.setActualTestResult("null");
                return;
            }
            // Handle assertArrayEquals violation
            matcher = Pattern.compile("^array contents differ at index \\[(?<index>.*)], expected: <(?<expected>.*)> but was: <(?<actual>.*)>$")
                    .matcher(assertionError.getMessage());
            if (matcher.find()) {
                testSuiteDetails.setExpectedTestResult(
                        matcher.group("expected") + " at index [" + matcher.group("index") + "]");
                testSuiteDetails.setActualTestResult(matcher.group("actual"));
                return;
            }
        } else if (exc instanceof java.security.AccessControlException ace) {
            testSuiteDetails.setAccessDenied(ace.getPermission().getName());
        } else if (exc instanceof SecurityException se) {
            testSuiteDetails.setAccessDenied(se.getMessage());
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        handleTestIgnored(testIdentifier);
    }

    private void handleTestIgnored(TestIdentifier testIdentifier) {
        log.info("Ignored test ({})", testIdentifier);
        TestDetailsDTO testSuiteDetails = createTestSuiteDetails(testIdentifier);
        ignoredCount++;
        testSuiteDetails.setTestStatus(IGNORED);
    }
    
    private String getStackTrace(Throwable throwable) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private String methodName(TestIdentifier testIdentifier) {
        return testIdentifier.getDisplayName().replaceFirst("\\(.*$", "");
    }

    private TestDetailsDTO createTestSuiteDetails(TestIdentifier testIdentifier) {
        var methodName = methodName(testIdentifier);
        var testDetailsDTO = map.computeIfAbsent(methodName, k -> new TestDetailsDTO());
        testDetailsDTO.setMethodName(methodName);

        var className = extractClassNameFromId(testIdentifier.getUniqueId());
        if (className != null) {
            var allTokens = className.split("\\.");
            var name = allTokens[allTokens.length - 1];
            testDetailsDTO.setClassName(name);

            var testSuite = name.split("_")[0];
            testDetailsDTO.setTestSuiteName(testSuite);
        }

        return testDetailsDTO;
    }

    private String extractClassNameFromId(String uniqueId) {
        // JUnit 5 unique IDs typically look like:
        // [engine:junit-jupiter]/[class:com.example.TestClass]/[method:testMethod()]
        var classMarker = "[class:";
        if (uniqueId.contains(classMarker)) {
            int start = uniqueId.indexOf(classMarker) + classMarker.length();
            int end = uniqueId.indexOf("]", start);
            if (end > start) { // checks for end == -1 as well
                return uniqueId.substring(start, end);
            }
        }
        return null;
    }
}
