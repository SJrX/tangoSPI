package net.sjrx.gradle.plugins.tango

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetOutput

import java.lang.reflect.Method

/**
 * Helper class to support both Gradle 4.0, and pre Gradle 4.0 without generating warnings:
 *
 *
 * Source of this class: https://discuss.gradle.org/t/gradle-4-0-warning-what-is-this/23004
 *
 */
class SourceSetOutputHelper {

    private static Method getClassesDirs = maybeGetMethod(SourceSetOutput.class, "getClassesDirs", null)

    private static Method maybeGetMethod(Class<?> type, String name, Class[] argTypes) {
        try {
            return type.getMethod(name, argTypes)
        } catch (NoSuchMethodException ignored) {
            // Method doesn't exist
            return null
        }
    }

    static Set<File> getClassesDirs(SourceSetOutput sso) {
        if (getClassesDirs == null) {
            return Collections.singleton(sso.getClassesDir())
        }
        try {
            FileCollection dirs = (FileCollection) getClassesDirs.invoke(sso, null)
            return dirs.getFiles()
        } catch (Exception e) {
            throw new IllegalStateException(e)
        }
    }
}