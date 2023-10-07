package de.tim_greller.susserver.service.execution;

import java.util.Optional;

import de.tim_greller.susserver.persistence.entity.ComponentEntity;
import de.tim_greller.susserver.persistence.entity.CutEntity;
import de.tim_greller.susserver.persistence.entity.TestEntity;
import de.tim_greller.susserver.persistence.entity.UserEntity;
import de.tim_greller.susserver.persistence.keys.ComponentKey;
import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import de.tim_greller.susserver.persistence.repository.ComponentRepository;
import de.tim_greller.susserver.persistence.repository.CutRepository;
import de.tim_greller.susserver.persistence.repository.TestRepository;
import de.tim_greller.susserver.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final ComponentRepository componentRepository;
    private final CutRepository cutRepository;

    @Autowired
    public TestService(TestRepository testRepository, UserRepository userRepository,
                       ComponentRepository componentRepository, CutRepository cutRepository) {
        this.testRepository = testRepository;
        this.userRepository = userRepository;
        this.componentRepository = componentRepository;
        this.cutRepository = cutRepository;
    }

    public Optional<TestEntity> getTestForComponent(String componentName, String userId) {
        return testRepository.findByKey(componentName, userId);
    }

    public TestEntity getOrCreateTestForComponent(String componentName, String userId) {
        return getTestForComponent(componentName, userId).orElseGet(() -> {
            final ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
            final UserEntity user = userRepository.findById(userId).orElseThrow();
            final UserComponentKey key = new UserComponentKey(component, user);
            return createEmptyTest(key);
        });
    }

    public void updateTestForComponent(String componentName, String userId, String newSourceCode) {
        final TestEntity test = getOrCreateTestForComponent(componentName, userId);
        test.setSourceCode(newSourceCode);
        testRepository.save(test);
    }

    private TestEntity createEmptyTest(UserComponentKey key) {
        final CutEntity cut = cutRepository.findById(new ComponentKey(key.getComponent())).orElseThrow();
        final String testClassName = cut.getClassName() + "Test";
        final String emptyTest = getTestTemplate(testClassName);
        return testRepository.save(new TestEntity(key, testClassName, emptyTest));
    }

    private String getTestTemplate(String testName) {
        return String.join("",
                getTestTemplateStart(testName),
                "        fail(\"Not implemented yet!\");",
                getTestTemplateEnd()
        );
    }

    private String getTestTemplateStart(String testName) {
        return (
        """
        import org.junit.Test;
        import static org.junit.Assert.*;
        
        public class\s""" + testName + """
         {
        
            @Test
            public void test() {
        """);
    }

    private String getTestTemplateEnd() {
        return (
        """
        
            }
        }
        """);
    }
}
