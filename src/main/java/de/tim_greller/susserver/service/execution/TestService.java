package de.tim_greller.susserver.service.execution;

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

    public TestEntity getTestForComponent(String componentName, String userId) {
        final ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
        final UserEntity user = userRepository.findById(userId).orElseThrow();
        final UserComponentKey key = new UserComponentKey(component, user);
        return testRepository.findById(key).orElse(createEmptyTest(key));
    }

    public void updateTestForComponent(String componentName, String userId, String newSourceCode) {
        final ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
        final UserEntity user = userRepository.findById(userId).orElseThrow();
        final UserComponentKey key = new UserComponentKey(component, user);
        final TestEntity test = testRepository.findById(key).orElse(createEmptyTest(key));
        test.setSourceCode(newSourceCode);
        testRepository.save(test);
    }

    private TestEntity createEmptyTest(UserComponentKey key) {
        final CutEntity cut = cutRepository.findById(new ComponentKey(key.getComponent())).orElseThrow();
        final String emptyTest = getTestTemplate(key.getComponent().getName());
        return testRepository.save(new TestEntity(key, cut.getClassName(), emptyTest));
    }

    private String getTestTemplate(String cutName) {
        return String.join("",
                getTestTemplateStart(cutName),
                "        Assertions.fail(\"Not implemented yet!\");",
                getTestTemplateEnd()
        );
    }

    private String getTestTemplateStart(String cutName) {
        return (
        """
        import org.junit.jupiter.api.Test;
        import org.junit.jupiter.api.Assertions;
        
        public class\s""" + cutName + """
        Test {
        
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
