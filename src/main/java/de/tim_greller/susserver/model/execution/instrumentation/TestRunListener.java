package de.tim_greller.susserver.model.execution.instrumentation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tim_greller.susserver.dto.TestDetailsDTO;
import de.tim_greller.susserver.dto.TestStatus;
import lombok.Getter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

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

    private TestDetailsDTO createTestSuiteDetails(TestIdentifier testIdentifier) {
        String methodName = testIdentifier.getDisplayName();
        
        // already add to map
        TestDetailsDTO testDetailsDTO = map.computeIfAbsent(
                methodName,
                k -> new TestDetailsDTO()
        );

        // set names
        testDetailsDTO.setMethodName(methodName);

        // Extract class name from the test identifier's unique ID or parent
        String uniqueId = testIdentifier.getUniqueId();
        String className = extractClassNameFromId(uniqueId);
        
        if (className != null) {
            String[] arr = className.split("\\.");
            String name = arr[arr.length - 1];
            testDetailsDTO.setClassName(name);

            String[] arr1 = name.split("_");
            String testSuite = arr1[0];
            testDetailsDTO.setTestSuiteName(testSuite);
        }

        return testDetailsDTO;
    }
    
    private String extractClassNameFromId(String uniqueId) {
        // JUnit 5 unique IDs typically look like: [engine:junit-jupiter]/[class:com.example.TestClass]/[method:testMethod()]
        if (uniqueId.contains("[class:") && uniqueId.contains("]")) {
            int start = uniqueId.indexOf("[class:") + 7;
            int end = uniqueId.indexOf("]", start);
            if (end > start) {
                return uniqueId.substring(start, end);
            }
        }
        return null;
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            String methodName = testIdentifier.getDisplayName();
            Optional.ofNullable(map.get(methodName)).ifPresent(TestDetailsDTO::setElapsedTime);
            
            if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
                handleTestFailure(testIdentifier, testExecutionResult);
            } else if (testExecutionResult.getStatus() == TestExecutionResult.Status.ABORTED) {
                handleTestIgnored(testIdentifier);
            }
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        testSuiteElapsedTime = System.currentTimeMillis() - testSuiteStartTime;
    }

    @SuppressWarnings("removal")
    private void handleTestFailure(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        String methodName = testIdentifier.getDisplayName();
        TestDetailsDTO testSuiteDetails = map.computeIfAbsent(
                methodName,
                k -> new TestDetailsDTO()
        );
        testSuiteDetails.setMethodName(methodName);
        
        Throwable exc = testExecutionResult.getThrowable().orElse(null);
        if (exc != null) {
            testSuiteDetails.setTrace(getStackTrace(exc));
            testSuiteDetails.setTestStatus(TestStatus.FAILED);

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
            } else if (exc instanceof SecurityException se) {
                testSuiteDetails.setAccessDenied(se.getMessage());
            }
        }
    }

    private void handleTestIgnored(TestIdentifier testIdentifier) {
        TestDetailsDTO testSuiteDetails = createTestSuiteDetails(testIdentifier);
        ignoredCount++;
        testSuiteDetails.setTestStatus(TestStatus.IGNORED);
    }
    
    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}
