package de.tim_greller.susserver.persistence.initial;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.tim_greller.susserver.persistence.entity.CutEntity;
import de.tim_greller.susserver.persistence.keys.ComponentKey;
import de.tim_greller.susserver.persistence.repository.ComponentRepository;
import de.tim_greller.susserver.persistence.repository.CutRepository;
import de.tim_greller.susserver.persistence.repository.PatchRepository;
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
            "(?<=\\n|\\A)(?:public\\s)?(?:class|interface|enum)\\s*\\n*\\s*(?<classname>\\w+).*?",
            Pattern.DOTALL
    );

    private final ComponentRepository componentRepository;
    private final CutRepository cutRepository;
    private final PatchRepository patchRepository;
    private final ResourceLoader resourceLoader;
    private final boolean initData;
    private final String cutPattern;

    @Autowired
    public InsertInitialData(ComponentRepository componentRepository, CutRepository cutRepository,
                             PatchRepository patchRepository, ResourceLoader resourceLoader,
                             @Value("${initData:false}") boolean initData,
                             @Value("${cutPattern:classpath:cut/*.java}") String cutPattern) {
        this.componentRepository = componentRepository;
        this.cutRepository = cutRepository;
        this.patchRepository = patchRepository;
        this.resourceLoader = resourceLoader;
        this.initData = initData;
        this.cutPattern = cutPattern;
    }

    @Override
    public void run(String... args) throws Exception {
        if (initData) {
            readAndSaveCuts();
        }
    }

    private void readAndSaveCuts() throws IOException {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        for (Resource resource : resolver.getResources(cutPattern)) {
            // component name = name of file
            var name = Objects.requireNonNull(resource.getFilename()).split("\\.java")[0];
            var content = resource.getContentAsString(StandardCharsets.UTF_8);
            // extract the classname via regex
            var className = Stream.of(CLASS_NAME_REGEX.matcher(content))
                    .filter(Matcher::matches)
                    .map(m -> m.group("classname"))
                    .findAny().orElseThrow();
            assert !className.isEmpty();
            // write to DB
            var component = componentRepository.getOrCreate(name);
            var cut = new CutEntity(new ComponentKey(component), className, content);
            cutRepository.save(cut);
        }
    }
}
