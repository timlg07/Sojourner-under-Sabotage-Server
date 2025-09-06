package de.tim_greller.susserver.model.execution.compilation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import de.tim_greller.susserver.dto.SourceDTO;
import de.tim_greller.susserver.exception.CompilationException;
import de.tim_greller.susserver.model.execution.JavaByteObject;
import de.tim_greller.susserver.model.execution.JavaStringObject;
import de.tim_greller.susserver.model.execution.instrumentation.transformer.IClassTransformer;
import de.tim_greller.susserver.model.execution.instrumentation.transformer.IdentityClassTransformer;
import de.tim_greller.susserver.model.execution.security.ClassLoadingFilter;
import de.tim_greller.susserver.model.execution.security.Sandbox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InMemoryCompiler {
    private final Map<String, JavaStringObject> sources = new HashMap<>();
    private final Map<String, JavaByteObject> compiledClasses = new HashMap<>();
    private final Map<String, IClassTransformer> transformers = new HashMap<>();
    private final IClassTransformer defaultTransformer = new IdentityClassTransformer();
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    private final JavaFileManager fileManager = createFileManager();
    private final ClassLoader inMemoryClassLoader;
    private final Collection<String> jarsToInclude;

    public InMemoryCompiler(final String identifier, final Collection<String> jarsToInclude) {
        inMemoryClassLoader = createClassLoader(identifier);
        this.jarsToInclude = jarsToInclude;
    }

    public void addSource(SourceDTO testSource) {
        addSource(testSource.getClassName(), testSource.getSourceCode());
    }

    public void addSource(String className, String source) {
        sources.put(className, new JavaStringObject(className, source));
    }

    private Iterable<? extends JavaFileObject> getCompilationUnits() {
        return Collections.unmodifiableCollection(sources.values());
    }

    public void compile() throws CompilationException {
        final var options = List.of(
                /*
                 * The -g option causes the compiler to generate the LocalVariableTable attribute in the class file.
                 * The Converter uses this attribute to determine local variable types.
                 * => required to trace variable assignments.
                 *
                 * And also:
                 * If you do not use the -g option, the Converter attempts to determine the variable types on its own.
                 * This is expensive in terms of processing and might not produce the most efficient code.
                 */
                "-g",

                "-classpath", Stream.concat(
                        Stream.of(System.getProperty("java.class.path")), // adding to default value, not replacing it
                        jarsToInclude.stream() // add libraries
                ).collect(Collectors.joining(":"))
        );
        log.info("Compilation options: {}", options);

        final JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, options, null, getCompilationUnits());

        final boolean allCompiledSuccessfully = task.call();

        if (!allCompiledSuccessfully) {
            throw new CompilationException(diagnostics.getDiagnostics());
        }

        try {
            fileManager.close();
        } catch (IOException e) {
            System.err.println("Error while closing the file manager.\n" + e);
        }
    }

    public Optional<Class<?>> getClass(String className) {
        try {
            return Optional.of(inMemoryClassLoader.loadClass(className));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    public void addTransformer(IClassTransformer transformer, String... classNames) {
        for (String className : classNames) {
            transformers.put(className, transformer);
        }
    }

    private JavaFileManager createFileManager() {
        final StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        return new ForwardingJavaFileManager<>(standardFileManager) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                                                       FileObject sibling) {
                return compiledClasses.computeIfAbsent(className, JavaByteObject::new);
            }
        };
    }

    private ClassLoader createClassLoader(final String identifier) {
        return new ClassLoader() {

            private final ClassLoadingFilter filter = new ClassLoadingFilter();
            private static final boolean USE_SANDBOX = false;

            @Override
            public Class<?> findClass(String name) throws ClassNotFoundException {
                if (!compiledClasses.containsKey(name)) {
                    throw new ClassNotFoundException("Class " + name + " not found.");
                }

                byte[] bytes = compiledClasses.get(name).getBytes();

                String classId = name + '#' + identifier;

                // transform class to add instrumentation
                IClassTransformer transformer = transformers.getOrDefault(name, defaultTransformer);
                bytes = transformer.transform(bytes, classId);

                // DEBUG // print transformed class to file
                // writeToFile(bytes, classId + "_transformed");

                Class<?> c = defineClass(name, bytes, 0, bytes.length);
                if (USE_SANDBOX) Sandbox.confine(c);
                return c;
            }

            // Override loadClass as well to prohibit delegation to the parent class loader.
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                synchronized (getClassLoadingLock(name)) {
                    // using the parent class loader ({@code super}) does only work locally, not when deployed on Tomcat.
                    // So use the web applications class loader instead:
                    var classLoader = InMemoryCompiler.class.getClassLoader();

                    if (filter.allowDelegateLoadingOf(name)) {
                        try {
                            log.debug("Delegate loading of {} to the web application's class loader.", name);
                            return classLoader.loadClass(name);
                        } catch (NoClassDefFoundError e) {
                            log.debug("NoClassDefFoundError // Delegate loading of {} to parent class loader.", name);
                            // For some reason the `org.junit.internal.AssumptionViolatedException` can only be
                            // loaded with the {@code super} class loader, when the app is deployed on Tomcat.
                            return super.loadClass(name);
                        }
                    }
                    log.debug("Prohibit class from delegating: {}", name);

                    try {
                        return findClass(name);
                    } catch (ClassNotFoundException e) {
                        classLoader.loadClass(name); // throws ClassNotFoundException if class does not exist at all
                        throw new SecurityException("Access to the class \"" + name + "\" was denied.");
                    }
                }
            }
        };
    }

    // Only used for debugging
    private void writeToFile(byte[] bytes, String classId) {
        try {
            String path = "target/classes/" + classId + ".class";
            log.debug("Writing transformed class to {}", path);
            Files.write(Paths.get(path), bytes);
        } catch (IOException e) {
            log.error("Error while writing transformed class to {}", classId, e);
        }
    }
}
