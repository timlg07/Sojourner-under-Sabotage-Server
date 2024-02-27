package de.tim_greller.susserver.persistence.initial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

import de.tim_greller.susserver.persistence.entity.CutEntity;
import de.tim_greller.susserver.persistence.entity.FallbackTestEntity;
import de.tim_greller.susserver.persistence.entity.GameProgressionEntity;
import de.tim_greller.susserver.persistence.keys.ComponentKey;
import de.tim_greller.susserver.persistence.keys.ComponentStageKey;
import de.tim_greller.susserver.persistence.repository.ComponentRepository;
import de.tim_greller.susserver.persistence.repository.CutRepository;
import de.tim_greller.susserver.persistence.repository.FallbackTestRepository;
import de.tim_greller.susserver.persistence.repository.GameProgressionRepository;
import de.tim_greller.susserver.persistence.repository.PatchRepository;
import de.tim_greller.susserver.persistence.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

@Component
public class InsertInitialData implements CommandLineRunner {

    private static final Pattern CLASS_NAME_REGEX = Pattern.compile(
            ".*?(?<=\\n|\\A)(?:public\\s)?(?:class|interface|enum)\\s*\\n*\\s*(?<classname>\\w+).*?",
            Pattern.DOTALL
    );

    private final ComponentRepository componentRepository;
    private final CutRepository cutRepository;
    private final PatchRepository patchRepository;
    private final FallbackTestRepository fallbackTestRepository;
    private final GameProgressionRepository gameProgressionRepository;
    private final RoomRepository roomRepository;
    private final ResourceLoader resourceLoader;
    private final boolean initData;
    private final String cutPattern;
    private final String fallbackTestPattern;

    @Autowired
    public InsertInitialData(ComponentRepository componentRepository, CutRepository cutRepository,
                             PatchRepository patchRepository, FallbackTestRepository fallbackTestRepository,
                             GameProgressionRepository gameProgressionRepository, RoomRepository roomRepository,
                             ResourceLoader resourceLoader,
                             @Value("${initData:false}") boolean initData,
                             @Value("${cutPattern:classpath:cut/*.java}") String cutPattern,
                             @Value("${fallbackTestPattern:classpath:test/stage-*/*.java}") String fallbackTestPattern) {
        this.componentRepository = componentRepository;
        this.cutRepository = cutRepository;
        this.patchRepository = patchRepository;
        this.fallbackTestRepository = fallbackTestRepository;
        this.gameProgressionRepository = gameProgressionRepository;
        this.roomRepository = roomRepository;
        this.resourceLoader = resourceLoader;
        this.initData = initData;
        this.cutPattern = cutPattern;
        this.fallbackTestPattern = fallbackTestPattern;
    }

    @Override
    public void run(String... args) throws Exception {
        if (initData) {
            readAndSaveCuts();
            readAndSaveFallbackTests();
            readAndSaveGameProgression();
        }
    }

    private void readAndSaveCuts() throws IOException {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        for (Resource resource : resolver.getResources(cutPattern)) {
            // component name = name of file
            var name = Objects.requireNonNull(resource.getFilename()).split("\\.java")[0];
            var content = resource.getContentAsString(UTF_8);
            // extract the classname via regex
            var className = extractClassName(content);
            assert !className.isEmpty();
            // write to DB
            var component = componentRepository.getOrCreate(name);
            var cut = new CutEntity(new ComponentKey(component), className, content);
            cutRepository.save(cut);
        }
    }

    private void readAndSaveFallbackTests() throws IOException {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        for (Resource resource : resolver.getResources(fallbackTestPattern)) {
            // stage number = folder name
            var stage = Integer.parseInt(resource.getURL().getPath().split("/stage-")[1].split("/")[0]);
            // component name = name of file without -Test.java
            var name = Objects.requireNonNull(resource.getFilename()).split("Test\\.java")[0];
            var content = resource.getContentAsString(UTF_8);
            // extract the classname via regex
            var className = extractClassName(content);
            assert !className.isEmpty();
            // write to DB
            var componentKey = new ComponentStageKey(componentRepository.getOrCreate(name), stage);
            fallbackTestRepository.save(new FallbackTestEntity(componentKey, className, content));
        }
    }

    private static String extractClassName(String content) {
        return Stream.of(CLASS_NAME_REGEX.matcher(content))
                .filter(Matcher::matches)
                .map(m -> m.group("classname"))
                .findAny().orElseThrow();
    }

    private void readAndSaveGameProgression() throws IOException {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        Resource resource = resolver.getResource("classpath:game/game-progression.csv");

        try (Stream<String> lines = new BufferedReader(new InputStreamReader(resource.getInputStream(), UTF_8)).lines()) {
            lines.skip(1) // Skip the header line
                    .map(line -> line.split(";"))
                    .map(this::createGameProgressionEntity)
                    .forEach(gameProgressionRepository::save);
        }
    }

    private GameProgressionEntity createGameProgressionEntity(String[] values) {
        return GameProgressionEntity.builder()
                .orderIndex(Integer.parseInt(values[0]))
                .roomId(Integer.parseInt(values[1]))
                .component(componentRepository.getOrCreate(values[2]))
                .stage(Integer.parseInt(values[3]))
                .delaySeconds(Integer.parseInt(values[4]))
                .build();
    }
}
