package de.tim_greller.susserver.model.execution.security;

import java.util.List;

public class ClassLoadingFilter {

    private static final List<String> WHITELISTED_CLASSES = List.of(

    );

    private static final List<String> WHITELISTED_PACKAGES = List.of(
            "java.lang.",
            "java.util.",
            "java.math.",
            "java.text.",
            "java.time.",
            "org.junit.",

            "de.tim_greller.susserver.model.execution.instrumentation."
    );

    private static final List<String> BLACKLISTED_CLASSES = List.of(
            java.lang.System.class.getName(),
            java.lang.Thread.class.getName(),
            java.lang.ThreadGroup.class.getName(),
            java.lang.Runtime.class.getName(),
            java.lang.ClassLoader.class.getName()
    );

    private static final List<String> BLACKLISTED_PACKAGES = List.of(
            "java.lang.reflect.",
            "java.lang.invoke.",
            "java.lang.instrument.",
            "java.lang.runtime.",
            "java.lang.management.",
            "java.lang.constant.",
            "java.lang.module.",
            "java.lang.ref.",

            "org.junit.internal.",
            "org.junit.runner.",
            "org.junit.runners.",
            "org.junit.experimental.",
            "org.junit.platform."
    );

    private boolean isWhitelisted(String className) {
        return isIn(className, WHITELISTED_CLASSES) || isInPackage(className, WHITELISTED_PACKAGES);
    }

    private boolean isBlacklisted(String className) {
        return isIn(className, BLACKLISTED_CLASSES) || isInPackage(className, BLACKLISTED_PACKAGES);
    }

    private boolean isIn(String className, List<String> list) {
        return list.stream().anyMatch(className::equals);
    }

    private boolean isInPackage(String className, List<String> list) {
        return list.stream().anyMatch(className::startsWith);
    }

    public boolean allowDelegateLoadingOf(String className) {
        return isWhitelisted(className) && !isBlacklisted(className);
    }
}
