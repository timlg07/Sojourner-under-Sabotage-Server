package de.tim_greller.susserver.model.execution.compilation;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
import de.tim_greller.susserver.model.execution.instrumentation.CoverageTracker;
import de.tim_greller.susserver.model.execution.instrumentation.IClassTransformer;
import de.tim_greller.susserver.model.execution.instrumentation.IdentityClassTransformer;
import de.tim_greller.susserver.model.execution.security.ClassLoadingFilter;
import de.tim_greller.susserver.model.execution.security.Sandbox;


public class InMemoryCompiler {
    private final Map<String, JavaStringObject> sources = new HashMap<>();
    private final Map<String, JavaByteObject> compiledClasses = new HashMap<>();
    private final Map<String, IClassTransformer> transformers = new HashMap<>();
    private final IClassTransformer defaultTransformer = new IdentityClassTransformer();
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    private final JavaFileManager fileManager = createFileManager();
    private final ClassLoader inMemoryClassLoader;

    public InMemoryCompiler(final String identifier) {
        inMemoryClassLoader = createClassLoader(identifier);
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
        final JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, null, null, getCompilationUnits());

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

                // clear previous coverage information for this class
                Optional.ofNullable(CoverageTracker.getInstance().getClassTrackers().get(classId))
                        .ifPresent(CoverageTracker.ClassTracker::clear);

                // transform class to add instrumentation
                IClassTransformer transformer = transformers.getOrDefault(name, defaultTransformer);
                bytes = transformer.transform(bytes, classId);

                Class<?> c = defineClass(name, bytes, 0, bytes.length);
                if (USE_SANDBOX) Sandbox.confine(c);
                return c;
            }

            // Override loadClass as well to prohibit delegation to the parent class loader.
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                synchronized (getClassLoadingLock(name)) {
                    if (filter.allowDelegateLoadingOf(name)) {
                        System.out.println("Delegate loading of " + name + " to parent class loader.");
                        return super.loadClass(name);
                    }
                    System.out.println("Prohibit class from delegating: " + name);

                    try {
                        return findClass(name);
                    } catch (ClassNotFoundException e) {
                        super.loadClass(name); // throws ClassNotFoundException if class does not exist at all
                        throw new SecurityException("Access to the class \"" + name + "\" was denied.");
                    }
                }
            }
        };
    }
}
