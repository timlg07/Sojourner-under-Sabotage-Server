package de.tim_greller.susserver.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestExecutionResultDTO {

    private TestStatus testStatus;
    private Map<String, TestDetails> testDetails;
    private long elapsedTime;
    private String testClassName;

    public enum TestStatus {
        PASSED, FAILED, IGNORED
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestDetails {
        private String methodName;
        private TestStatus testStatus;
        private String actualTestResult;
        private String expectedTestResult;
        private String trace;
    }
}
