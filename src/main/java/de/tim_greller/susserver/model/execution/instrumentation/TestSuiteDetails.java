package de.tim_greller.susserver.model.execution.instrumentation;

import java.util.Objects;

public class TestSuiteDetails {
    public enum TestStatus {
        PASSED, FAILED, IGNORED
    }
    private TestStatus testStatus;
    private String testClassName;
    private String testSuiteName;
    private String testCaseName;
    private String testDescription;

    private long startTime;
    private long elapsedTime;

    private String actualTestResult;
    private String expectedTestResult;


    public String getTestClassName() {
        return testClassName;
    }

    public void setTestClassName(String testClassName) {
        this.testClassName = testClassName;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public TestStatus getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(TestStatus testStatus) {
        this.testStatus = testStatus;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public boolean areActualExpectedDefined() {
        return actualTestResult != null && expectedTestResult != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestSuiteDetails that = (TestSuiteDetails) o;
        return Objects.equals(testClassName, that.testClassName)
                && Objects.equals(testCaseName, that.testCaseName);
    }

    @Override
    public int hashCode() {
        int result = testClassName != null ? testClassName.hashCode() : 0;
        result = 31 * result + (testCaseName != null ? testCaseName.hashCode() : 0);
        return result;
    }

    public String getTestSuiteName() {
        return testSuiteName;
    }

    public void setTestSuiteName(String testSuiteName) {
        this.testSuiteName = testSuiteName;
    }

    public String getActualTestResult() {
        return actualTestResult;
    }

    public void setActualTestResult(String actualTestResult) {
        this.actualTestResult = actualTestResult;
    }

    public String getExpectedTestResult() {
        return expectedTestResult;
    }

    public void setExpectedTestResult(String expectedTestResult) {
        this.expectedTestResult = expectedTestResult;
    }
}