package de.tim_greller.susserver.dto;

import java.util.List;
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

    private String testClassName;
    private TestStatus testStatus;
    private long elapsedTime;
    private Map<String, TestDetailsDTO> testDetails;
    private Map<String, Map<Integer, Integer>> coverage;
    private Map<String, Map<Integer, Map<String, String>>> variables;
    private Map<String, List<LogEntry>> logs;

}
