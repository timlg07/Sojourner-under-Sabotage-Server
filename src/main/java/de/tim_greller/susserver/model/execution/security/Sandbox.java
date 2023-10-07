package de.tim_greller.susserver.model.execution.security;

import java.security.Permission;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class is derived from <a href="https://stackoverflow.com/a/24225075/6336728">this SO answer</a>.
 * <p>
 * This class establishes a security manager that confines the permissions for code executed through specific classes,
 * which may be specified by class, class name and/or class loader.
 * <p>
 * To 'execute through a class' means that the execution stack includes the class. E.g., if a method of class {@code A}
 * invokes a method of class {@code B}, which then invokes a method of class {@code C}, and all three classes were
 * previously {@link #confine(Class, Permissions) confined}, then for all actions that are executed by class {@code C}
 * the <i>intersection</i> of the three {@link Permissions} apply.
 * <p>
 * Once the permissions for a class, class name or class loader are confined, they cannot be changed; this prevents any
 * attempts (e.g. of the confined class itself) to release the confinement.
 * <p>
 * Code example:
 * <pre>
 *  Runnable unprivileged = new Runnable() {
 *      public void run() {
 *          System.getProperty("user.dir");
 *      }
 *  };
 *
 *  // Run without confinement.
 *  unprivileged.run(); // Works fine.
 *
 *  // Set the most strict permissions.
 *  Sandbox.confine(unprivileged.getClass(), new Permissions());
 *  unprivileged.run(); // Throws a SecurityException.
 *
 *  // Attempt to change the permissions.
 *  {
 *      Permissions permissions = new Permissions();
 *      permissions.add(new AllPermission());
 *      Sandbox.confine(unprivileged.getClass(), permissions); // Throws a SecurityException.
 *  }
 *  unprivileged.run();
 * </pre>
 */
@SuppressWarnings("removal")
public final class Sandbox {

    private Sandbox() {}

    private static final Permissions NO_PERMISSIONS = new Permissions();

    private static final Map<Class<?>, java.security.AccessControlContext>
            CHECKED_CLASSES = Collections.synchronizedMap(new WeakHashMap<>());

    private static final Map<String, java.security.AccessControlContext>
            CHECKED_CLASS_NAMES = Collections.synchronizedMap(new HashMap<>());

    private static final Map<ClassLoader, java.security.AccessControlContext>
            CHECKED_CLASS_LOADERS = Collections.synchronizedMap(new WeakHashMap<>());

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

                    // Check if an ACC was set for the class.
                    {
                        java.security.AccessControlContext acc = Sandbox.CHECKED_CLASSES.get(class_);
                        if (acc != null) {
                            System.out.println("Checking permission for class " + class_);
                            acc.checkPermission(perm);
                        }
                    }

                    // Check if an ACC was set for the class name.
                    {
                        java.security.AccessControlContext acc = Sandbox.CHECKED_CLASS_NAMES.get(class_.getName());
                        if (acc != null) {
                            acc.checkPermission(perm);
                        }
                    }

                    // Check if an ACC was set for the class loader.
                    {
                        java.security.AccessControlContext acc =
                                Sandbox.CHECKED_CLASS_LOADERS.get(class_.getClassLoader());
                        if (acc != null) {
                            System.out.println("Checking permission for classloader " + class_.getClassLoader());
                            acc.checkPermission(perm);
                        }
                    }
                }
            }
        });
    }

    // --------------------------

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

    public static void confine(String className, java.security.AccessControlContext accessControlContext) {

        if (Sandbox.CHECKED_CLASS_NAMES.containsKey(className)) {
            throw new SecurityException("Attempt to change the access control context for '" + className + "'");
        }

        Sandbox.CHECKED_CLASS_NAMES.put(className, accessControlContext);
    }

    public static void confine(String className, ProtectionDomain protectionDomain) {
        Sandbox.confine(className, new java.security.AccessControlContext(new ProtectionDomain[] {protectionDomain}));
    }

    public static void confine(String className, Permissions permissions) {
        Sandbox.confine(className, new ProtectionDomain(null, permissions));
    }

    public static void confine(ClassLoader classLoader, java.security.AccessControlContext accessControlContext) {

        if (Sandbox.CHECKED_CLASS_LOADERS.containsKey(classLoader)) {
            throw new SecurityException("Attempt to change the access control context for '" + classLoader + "'");
        }

        Sandbox.CHECKED_CLASS_LOADERS.put(classLoader, accessControlContext);
    }

    public static void confine(ClassLoader classLoader, ProtectionDomain protectionDomain) {
        Sandbox.confine(classLoader, new java.security.AccessControlContext(new ProtectionDomain[] {protectionDomain}));
    }

    public static void confine(ClassLoader classLoader, Permissions permissions) {
        Sandbox.confine(classLoader, new ProtectionDomain(null, permissions));
    }

    public static ClassLoader confine(ClassLoader classLoader) {
        Sandbox.confine(classLoader, NO_PERMISSIONS);
        return classLoader;
    }

}