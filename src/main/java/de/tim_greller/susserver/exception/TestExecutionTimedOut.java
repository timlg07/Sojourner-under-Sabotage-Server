package de.tim_greller.susserver.exception;

public class TestExecutionTimedOut extends TestExecutionException {
    public TestExecutionTimedOut(int seconds) {
        super("Test execution timed out after " + seconds + " seconds");
    }
}
