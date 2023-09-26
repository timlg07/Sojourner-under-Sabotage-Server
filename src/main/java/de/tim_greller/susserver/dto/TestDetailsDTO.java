package de.tim_greller.susserver.dto;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestDetailsDTO {

    private String className;
    private String methodName;
    private String testSuiteName;

    private TestStatus testStatus;
    private String actualTestResult;
    private String expectedTestResult;
    private String trace;

    /**
     * The time when the test was started in milliseconds since the epoch.
     */
    private long startTime;
    /**
     * The time that was needed to execute the test in milliseconds.
     */
    private long elapsedTime;

    /**
     * Calculates and sets the elapsed time of the test as the difference between now and the start time.
     */
    public void setElapsedTime() {
        this.elapsedTime = System.currentTimeMillis() - startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestDetailsDTO that = (TestDetailsDTO) o;
        return Objects.equals(className, that.className)
                && Objects.equals(methodName, that.methodName);
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        return result;
    }
}
