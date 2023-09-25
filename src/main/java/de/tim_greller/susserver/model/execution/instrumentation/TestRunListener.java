package de.tim_greller.susserver.model.execution.instrumentation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class TestRunListener extends RunListener {

    private final Map<String, TestSuiteDetails> map = new LinkedHashMap<>();

    private long testSuiteStartTime;
    private long testSuiteElapsedTime;
    private int ignoredCount;


    public void testRunStarted(Description description) throws Exception {
        testSuiteStartTime = System.currentTimeMillis();
    }


    public void testStarted(Description description) throws Exception {
        TestSuiteDetails testSuiteDetails = createTestSuiteDetails(description);
        testSuiteDetails.setTestStatus(TestSuiteDetails.TestStatus.PASSED);
        testSuiteDetails.setStartTime(System.currentTimeMillis());
    }

    private TestSuiteDetails createTestSuiteDetails(Description description) {
        // already add to map
        TestSuiteDetails testSuiteDetails = map.computeIfAbsent(
                description.getMethodName(),
                k -> new TestSuiteDetails()
        );

        // set names
        testSuiteDetails.setTestCaseName(description.getMethodName());

        String[] arr = description.getTestClass().getName().split("\\.");
        String name = arr[arr.length - 1];
        testSuiteDetails.setTestClassName(name);

        String[] arr1 = name.split("_");
        String testSuite = arr1[0];
        testSuiteDetails.setTestSuiteName(testSuite);

        return testSuiteDetails;
    }

    public void testFinished(Description description) throws Exception {
        Optional.ofNullable(map.get(description.getMethodName()))
                .ifPresent(testSuiteDetails -> testSuiteDetails.setElapsedTime(
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - testSuiteDetails.getStartTime())
                ));
    }

    public void testRunFinished(org.junit.runner.Result result) throws Exception {
        testSuiteElapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - testSuiteStartTime);
    }

    public void testFailure(Failure failure) {
        TestSuiteDetails testSuiteDetails = map.computeIfAbsent(
                failure.getDescription().getMethodName(),
                k -> new TestSuiteDetails()
        );
        testSuiteDetails.setTestCaseName(failure.getDescription().getMethodName());
        testSuiteDetails.setTestDescription(failure.getException().toString());
        testSuiteDetails.setTestStatus(TestSuiteDetails.TestStatus.FAILED);

        Throwable exc = failure.getException();
        if (exc instanceof AssertionError) {
            AssertionError assertionError = (AssertionError) exc;
            Matcher matcher = Pattern
                    .compile("^expected:<(?<expected>.*)> but was:<(?<actual>.*)>$")
                    .matcher(assertionError.getMessage());
            if (matcher.find()) {
                testSuiteDetails.setExpectedTestResult(matcher.group("expected"));
                testSuiteDetails.setActualTestResult(matcher.group("actual"));
            }
        }
    }

    public void testIgnored(Description description) throws Exception {
        TestSuiteDetails testSuiteDetails = createTestSuiteDetails(description);
        ignoredCount++;
        testSuiteDetails.setTestStatus(TestSuiteDetails.TestStatus.IGNORED);
    }

    public int getIgnoredCount() {
        return ignoredCount;
    }

    public void setIgnoredCount(int ignoredCount) {
        this.ignoredCount = ignoredCount;
    }

    public Map<String, TestSuiteDetails> getMap() {
        return map;
    }

    public long getTestSuiteStartTime() {
        return testSuiteStartTime;
    }

    public long getTestSuiteElapsedTime() {
        return testSuiteElapsedTime;
    }
}