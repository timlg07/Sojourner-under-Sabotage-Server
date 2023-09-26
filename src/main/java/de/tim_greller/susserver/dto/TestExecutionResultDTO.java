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
    private Map<String, TestDetailsDTO> testDetails;
    private long elapsedTime;
    private String testClassName;

}
