package de.tim_greller.susserver.model.execution.security;

import java.security.Permission;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is derived from <a href="https://stackoverflow.com/a/24225075/6336728">this SO answer</a> which uses the
 * code of an old version of this file:
 * <a href="https://github.com/janino-compiler/janino/blob/60e5667afb0b1055a09b1ea298a17d6c94272283/commons-compiler/src/main/java/org/codehaus/commons/compiler/Sandbox.java">Sandbox.java on GitHub</a>.
 * <p>
 * This class establishes a security manager that confines the permissions for code executed through classes of specific
 * classloaders.
 * <p>
 * To 'execute through a class' means that the execution stack includes the class. E.g., if a method of class {@code A}
 * invokes a method of class {@code B}, which then invokes a method of class {@code C}, and all three classes were from a
 * previously {@link #confine(ClassLoader, Permissions) confined} classloader, then for all actions that are executed by class {@code C}
 * the <i>intersection</i> of the three {@link Permissions} apply.
 * <p>
 * Once the permissions for a class loader are confined, they cannot be changed; this prevents any
 * attempts (e.g. of the confined class itself) to release the confinement.
 */
@SuppressWarnings("removal")
@Slf4j
public final class Sandbox {

    private Sandbox() {}

    private static final Permissions NO_PERMISSIONS = new Permissions();

    private static final Map<ClassLoader, java.security.AccessControlContext>
            CHECKED_CLASS_LOADERS = Collections.synchronizedMap(new WeakHashMap<>());

    private static final Map<Class<?>, java.security.AccessControlContext>
            CHECKED_CLASSES = Collections.synchronizedMap(new WeakHashMap<>());

    static {
        // Install our custom security manager.
        if (System.getSecurityManager() != null) {
            throw new ExceptionInInitializerError("There's already a security manager set");
        }

        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                assert perm != null;
                int visitedThis = 0;
                for (Class<?> class_ : getClassContext()) {
                    // Detect circular call to avoid infinite recursion / stack overflow.
                    if (class_ == getClass() && ++visitedThis >= 2) return;

                    // Check if an ACC was set for the class loader.
                    {
                        java.security.AccessControlContext acc =
                                Sandbox.CHECKED_CLASS_LOADERS.get(class_.getClassLoader());
                        if (acc != null) {
                            log.debug("Checking permission for classloader {}", class_.getClassLoader());
                            acc.checkPermission(perm);
                        }
                    }

                    // Check if an ACC was set for the class.
                    {
                        java.security.AccessControlContext acc = Sandbox.CHECKED_CLASSES.get(class_);
                        if (acc != null) {
                            log.debug("Checking permission for class {}", class_);
                            acc.checkPermission(perm);
                        }
                    }
                }
            }
        });
    }


    /**
     * All future actions that are executed through the given {@code classLoader} will be checked against the given
     * {@code accessControlContext}.
     *
     * @throws SecurityException Permissions are already confined for the {@code classLoader}
     */
    public static void confine(ClassLoader classLoader, java.security.AccessControlContext accessControlContext) {

        if (Sandbox.CHECKED_CLASS_LOADERS.containsKey(classLoader)) {
            throw new SecurityException("Attempt to change the access control context for '" + classLoader + "'");
        }

        Sandbox.CHECKED_CLASS_LOADERS.put(classLoader, accessControlContext);
    }

    /**
     * All future actions that are executed through the given {@code classLoader} will be checked against the given
     * {@code protectionDomain}.
     *
     * @throws SecurityException Permissions are already confined for the {@code classLoader}
     */
    public static void confine(ClassLoader classLoader, ProtectionDomain protectionDomain) {
        Sandbox.confine(classLoader, new java.security.AccessControlContext(new ProtectionDomain[] {protectionDomain}));
    }

    /**
     * All future actions that are executed through the given {@code classLoader} will be checked against the given
     * {@code permissions}.
     *
     * @throws SecurityException Permissions are already confined for the {@code classLoader}
     */
    public static void confine(ClassLoader classLoader, Permissions permissions) {
        Sandbox.confine(classLoader, new ProtectionDomain(null, permissions));
    }

    /**
     * All future actions that are executed through the given {@code classLoader} will have no permissions.
     *
     * @throws SecurityException Permissions are already confined for the {@code classLoader}
     */
    public static ClassLoader confine(ClassLoader classLoader) {
        Sandbox.confine(classLoader, NO_PERMISSIONS);
        return classLoader;
    }

    /**
     * All future actions that are executed through the given {@code class_} will be checked against the given {@code
     * accessControlContext}.
     *
     * @throws SecurityException Permissions are already confined for the {@code class_}
     */
    public static void confine(Class<?> class_, java.security.AccessControlContext accessControlContext) {

        if (Sandbox.CHECKED_CLASSES.containsKey(class_)) {
            throw new SecurityException("Attempt to change the access control context for '" + class_ + "'");
        }

        Sandbox.CHECKED_CLASSES.put(class_, accessControlContext);
    }

    /**
     * All future actions that are executed through the given {@code class_} will be checked against the given {@code
     * protectionDomain}.
     *
     * @throws SecurityException Permissions are already confined for the {@code class_}
     */
    public static void confine(Class<?> class_, ProtectionDomain protectionDomain) {
        Sandbox.confine(class_, new java.security.AccessControlContext(new ProtectionDomain[] {protectionDomain}));
    }

    /**
     * All future actions that are executed through the given {@code class_} will be checked against the given {@code
     * permissions}.
     *
     * @throws SecurityException Permissions are already confined for the {@code class_}
     */
    public static void confine(Class<?> class_, Permissions permissions) {
        Sandbox.confine(class_, new ProtectionDomain(null, permissions));
    }

    public static <T> Class<T> confine(Class<T> class_) {
        Sandbox.confine(class_, NO_PERMISSIONS);
        return class_;
    }

}
