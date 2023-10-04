package de.tim_greller.susserver.service.execution;

import java.util.Optional;

import de.tim_greller.susserver.dto.TestSourceDTO;
import de.tim_greller.susserver.persistence.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private final TestRepository testRepository;

    @Autowired
    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public Optional<TestSourceDTO> getTestForComponent(String componentName) {
        return testRepository.findById(componentName).map(TestSourceDTO::fromTestEntity);
    }
}
