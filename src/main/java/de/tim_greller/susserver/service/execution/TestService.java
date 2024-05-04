package de.tim_greller.susserver.service.execution;

import java.util.Optional;

import de.tim_greller.susserver.dto.Range;
import de.tim_greller.susserver.dto.TestSourceDTO;
import de.tim_greller.susserver.persistence.entity.ComponentEntity;
import de.tim_greller.susserver.persistence.entity.ComponentStatusEntity;
import de.tim_greller.susserver.persistence.entity.CutEntity;
import de.tim_greller.susserver.persistence.entity.FallbackTestEntity;
import de.tim_greller.susserver.persistence.entity.TestEntity;
import de.tim_greller.susserver.persistence.entity.UserEntity;
import de.tim_greller.susserver.persistence.keys.ComponentKey;
import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import de.tim_greller.susserver.persistence.repository.ComponentRepository;
import de.tim_greller.susserver.persistence.repository.ComponentStatusRepository;
import de.tim_greller.susserver.persistence.repository.CutRepository;
import de.tim_greller.susserver.persistence.repository.FallbackTestRepository;
import de.tim_greller.susserver.persistence.repository.TestRepository;
import de.tim_greller.susserver.service.auth.UserService;
import de.tim_greller.susserver.service.tracking.UserEventTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final UserService userService;
    private final UserEventTrackingService trackingService;
    private final ComponentRepository componentRepository;
    private final CutRepository cutRepository;
    private final ComponentStatusRepository componentStatusRepository;
    private final FallbackTestRepository fallbackTestRepository;

    public Optional<TestEntity> getTestForComponent(String componentName, String userId) {
        return testRepository.findByKey(componentName, userId);
    }

    private TestEntity getOrCreateTestEntityForComponent(String componentName, String userId) {
        return getTestForComponent(componentName, userId).orElseGet(() -> {
            final ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
            final UserEntity user = userService.loadUserByEmail(userId).orElseThrow();
            final UserComponentKey key = new UserComponentKey(component, user);
            return createEmptyTest(key);
        });
    }

    public TestSourceDTO getOrCreateTestDtoForComponent(String componentName, String userId) {
        TestEntity entity = getOrCreateTestEntityForComponent(componentName, userId);
        return TestSourceDTO
                .fromTestEntity(entity)
                .restrictTo(getEditableRange(entity.getSourceCode()));
    }

    public void replaceWithAutoGeneratedTest(ComponentStatusEntity componentStatus, String userId) {
        final String componentName = componentStatus.getUserComponentKey().getComponent().getName();
        final int stage = componentStatus.getStage();
        final FallbackTestEntity fallbackTest = fallbackTestRepository.findByKey(componentName, stage).orElseThrow();

        updateTestForComponent(componentName, userId, fallbackTest.getSourceCode());
    }

    public TestSourceDTO getHiddenTestForComponent(ComponentStatusEntity componentStatus) {
        final String componentName = componentStatus.getUserComponentKey().getComponent().getName();
        final int stage = componentStatus.getStage();
        final FallbackTestEntity fallbackTest = fallbackTestRepository.findByKey(componentName, stage).orElseThrow();
        return TestSourceDTO.fromFallbackTestEntity(fallbackTest);
    }

    public boolean updateTestForComponent(String componentName, String userId, String newSourceCode) {
        final TestEntity test = getOrCreateTestEntityForComponent(componentName, userId);
        if (!checkTestUsesTemplateParts(test.getClassName(), newSourceCode)) {
            throw new SecurityException("Test has modified template parts.");
        }
        final boolean testChanged = !test.getSourceCode().equals(newSourceCode);
        if (testChanged) {
            test.setSourceCode(newSourceCode);
            testRepository.save(test);
            trackingService.trackEvent("test-modified", test);
        }
        return testChanged;
    }

    private boolean checkTestUsesTemplateParts(String testName, String newSourceCode) {
        return newSourceCode.startsWith(getTestTemplateStart(testName))
                && newSourceCode.endsWith(getTestTemplateEnd());
    }

    public Range getEditableRange(String sourceCode) {
        final int templateStartLineCount = getTestTemplateStart("").split("\n").length;
        final int templateEndLineCount = getTestTemplateEnd().split("\n").length;
        final String[] sourceLines = sourceCode.split("\n");
        final int firstEditableLine = templateStartLineCount + 1;
        final int lastEditableLine = sourceLines.length - templateEndLineCount + 1;
        final int lastEditableLineColumns = sourceLines[lastEditableLine - 1].length() + 1;
        return new Range(
                firstEditableLine, 1,
                lastEditableLine, lastEditableLineColumns
        );
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
                """
                    @Test
                    public void test() {
                        fail("Not implemented yet!");
                    }
                """,
                getTestTemplateEnd()
        );
    }

    private String getTestTemplateStart(String testName) {
        return (
        """
        import org.junit.Test;
        import static org.junit.Assert.*;

        /* For assertions you can use:
         - assertTrue
         - assertFalse
         - assertEquals
         - assertNotEquals
         - assertArrayEquals
         - assertThrows
         - assertNull
         - assertNotNull
         */
        
        public class\s""" + testName + """
         {
        
        """);
    }

    private String getTestTemplateEnd() {
        return (
        """
        
        }
        """);
    }

    // TODO: fallback test method may have same name as an already existing test method
    public void addHiddenTestMethodToUserTest(String methodName, String componentName, String userId) {
        var userTest = getOrCreateTestDtoForComponent(componentName, userId);
        // Cannot inject componentStatusService due to circular dependency. But the component should always have a status.
        var componentStatus = componentStatusRepository.findByKey(componentName, userId).orElseThrow();
        var hiddenTest = getHiddenTestForComponent(componentStatus);

        var hiddenTestMethod = new StringBuilder();
        var lines = hiddenTest.getSourceCode().lines().toList();
        String lastLine = null;
        boolean isTestBegin = false;
        boolean isTargetTest = false;
        int curlyBraceCount = 0;
        for (var line : lines) {
            if (isTestBegin && line.matches("^\\s*(public|private|protected)?\\s+void\\s+" + methodName + "\\s*\\(.*\\)\\s*\\{.*$")) {
                isTargetTest = true;
                hiddenTestMethod.append("\n").append(lastLine).append("\n").append(line).append("\n");
                curlyBraceCount = 1;
            } else if (isTargetTest) {
                hiddenTestMethod.append(line).append("\n");

                int curlyOpen = line.length() - line.replace("{", "").length();
                int curlyClose = line.length() - line.replace("}", "").length();
                curlyBraceCount += curlyOpen - curlyClose;
                if (curlyBraceCount == 0) {
                    break;
                }
            }

            isTestBegin = line.matches("^\\s*@Test[\\s(]?.*$");
            lastLine = line;
        }

        if (hiddenTestMethod.isEmpty()) {
            throw new IllegalArgumentException("Method not found in hidden test.");
        }

        var userTestSource = userTest.getSourceCode();
        var userTestSourceWithoutTemplateEnd = userTestSource.substring(0, userTestSource.lastIndexOf(getTestTemplateEnd()));
        var newSourceCode = userTestSourceWithoutTemplateEnd + hiddenTestMethod + getTestTemplateEnd();

        updateTestForComponent(componentName, userId, newSourceCode);
    }

    public void resetTestsForUser(String userId) {
        testRepository.deleteAllByUserComponentKeyUserEmail(userId);
    }
}
