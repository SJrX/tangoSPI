package net.sjrx.gradle.plugins.tango.integrationtests.fixtures

import groovy.transform.CompileStatic
import org.gradle.testkit.runner.GradleRunner

/**
 * Helper class for generating a Gradle File Builder for Test Purposes
 *
 * TODO Evaluate if this can be ported to Native Groovy Builders, when familiar with them.
 */
@CompileStatic
class GradleProjectBuilder {

    private String gradleFile = ""

    private File buildDirectoryRoot = null;

    boolean nonPluginApplied = false

    private List<String> interfacesToSearch = new ArrayList<>();

    /**
     * Force Private Constructor, use static builder methods.
     */
    private GradleProjectBuilder()
    {

    }

    /**
     * Adds the following to the gradle build file, which has the effect of loading the plugin.
     *
     * plugins {
     *     id "net.sjrx.tangospi
     * }
     * @return this
     */
    GradleProjectBuilder loadAndApplyTangoPlugin() {
        verifyPluginStatementCanBeExecuted()
        gradleFile += """plugins { 
        id "net.sjrx.tangospi"
    }
    """;
        return this;
    }

    /**
     * Adds the following to the gradle build file, which has the effect of loading the plugin, but not applying it
     *
     * plugins {
     *     id "net.sjrx.tangospi" apply false
     * }
     *
     * This method is hardly used, and primarily is around from initial prototyping as I was trying to get everything bootstrapped with TestKit
     *
     * @return this
     */
    GradleProjectBuilder loadTangoPlugin() {
        verifyPluginStatementCanBeExecuted()
        // Source: http://mrhaki.blogspot.ca/2016/09/gradle-goodness-add-but-do-not-apply.html
        gradleFile += """plugins { 
    id "net.sjrx.tangospi" apply false
    }
"""
        return this
    }

    /**
     * Adds the following to the gradle build file
     *
     * apply plugin "net.sjrx.tangospi"
     *
     * This method is hardly used and is the sister method of loadTangoPlugin(), and primarily exists from initial prototyping. To the best of my knowledge you cannot load non native plugins this way (unless they magically are on the class path).
     *
     * @return this
     */
    GradleProjectBuilder applyTangoPlugin() {
        return applyPlugin("net.sjrx.tangospi")
    }

    /**
     * Adds the following to the gradle build file
     *
     * apply plugin: 'java'
     *
     * @return this
     */
    GradleProjectBuilder applyJava() {
        return applyPlugin('java')
    }

    /**
     * Adds the following to the gradle build file
     *
     * apply plugin: 'scala'
     *
     * @return
     */
    GradleProjectBuilder applyScala() {
        return applyPlugin('scala')
    }

    /**
     * Adds the following to the gradle build file
     *
     * apply plugin: 'groovy'
     *
     * @return
     */
    GradleProjectBuilder applyGroovy() {
        return applyPlugin('groovy')
    }

    /**
     * Writes the built up gradle file and then returns a GradleRunner ready to execute against this build file.
     *
     * @return
     */
    GradleRunner prepareRunner() {
        assert buildDirectoryRoot != null

        new File(buildDirectoryRoot.getAbsolutePath() + "/build.gradle").write(gradleFile, "UTF-8")

        return GradleRunner.create().withPluginClasspath().withProjectDir(buildDirectoryRoot)
    }

    /**
     * Returns a GradleProjectBuilder that represents an entirely empty build file.
     * @return
     */
    static GradleProjectBuilder empty()
    {
        return new GradleProjectBuilder();
    }

    /**
     * Creates a build file that will be written in a particular directory.
     * @param buildDirectoryRoot
     * @return
     */
    GradleProjectBuilder withTempDirectory(final File buildDirectoryRoot) {

        this.buildDirectoryRoot = buildDirectoryRoot

        return this;
    }

    /**
     * Configures the build file to also search for a particular interface.
     *
     *
     * @param interfaceName - the fully qualified class name to add to the build file (will only be added when the addTangoConfigurationBlock() method is invoked)
     * @return
     */
    GradleProjectBuilder addInterfaceToSearch(String interfaceName) {
        this.interfacesToSearch += interfaceName

        return this;
    }

    /**
     * Adds the following block to the Gradle build file
     *
     * tangospi {
     *     interfaces = [ {interfaces that have been added via addInterfaceToSearch} ]
     * }
     *
     * @return this
     */
    GradleProjectBuilder addTangoConfigurationBlock() {
        nonPluginApplied = true

        gradleFile += """tangospi {
interfaces = [${this.interfacesToSearch.collect {"'$it'"}.join(",")}]
}
"""
        return this
    }

    /**
     * Manually adds the file META-INF/services/<interfaceName> with all class names supplied to the build
     *
     * Normally our plugin is designed to automagically do this, but we need to test cases where this file already exists.
     *
     * @param interfaceName
     * @param classNames
     * @return
     */
    GradleProjectBuilder manuallyAddProviderConfigurationFile(String interfaceName, List<String> classNames) {
        String directoryName = buildDirectoryRoot.absolutePath + "/src/main/resources/META-INF/services/";

        mkdirIfNotExist(directoryName);

        new File("$directoryName/$interfaceName").write("""# Autogenerated for test\n${classNames.join("\n")}""", "UTF-8")

        return this
    }

    /**
     * Adds a run task that will output everything
     *
     *
     * @return
     */
    GradleProjectBuilder enableValidationRunTaskForInterface(String... requestedInterfaceName) {
        applyPlugin('application');

        JavaStructureInfo info = new JavaStructureInfo("net.sjrx.fixtures.executor.ServiceLoaderTestExecutor");


        gradleFile += """mainClassName = "$info.canonicalName"\n
"""

        String requestedInterfacesAsJavaArray = requestedInterfaceName.collect{String.valueOf(it) + ".class" }.join(",")

        return writeSourceOfJavaStructureToFile(info, """
package $info.packageName;

import java.util.ServiceLoader;

class $info.simpleName { 


public static void main(String[] args) {

    Class[] classes = { $requestedInterfacesAsJavaArray };
 
    for( Class cls : classes) {
        ServiceLoader<Object> sl = ServiceLoader.load(cls);
    
        int i = 0;
        for(Object o : sl)
        {
                System.out.println("SPI Implementation: " + cls.getCanonicalName() + " ==> " + o.getClass());
                i++;
        }
    
        System.out.println("SPI Count: " + cls.getCanonicalName() + " ==> " + i);
    }
}

}
""")
    }

    String toString() {
        return "CurrentBuildFile: ${this.gradleFile}\n"
    }

    private GradleProjectBuilder applyPlugin(String plugin) {
        gradleFile += "apply plugin: '$plugin'\n"
        nonPluginApplied = true
        return this;
    }

    private verifyPluginStatementCanBeExecuted()
    {
        if(nonPluginApplied)
        {
            throw new IllegalStateException("""Your build will fail, because the tango plugin needs to be applied first. You will most likely get the following error from Gradle:
>>> "only buildscript {} and other plugins {} script blocks are allowed before plugins {} blocks, no other statements are allowed"
""");
        }
    }

    /**
     * Adds a Java file with the supplied interface name to the source directory of the build project.
     *
     * @param interfaceName
     */
    GradleProjectBuilder addJavaInterfaceToSource(String interfaceName) {

        JavaStructureInfo interfaceInfo = new JavaStructureInfo(interfaceName)

        String source = """package $interfaceInfo.packageName; 

           public interface $interfaceInfo.simpleName {
           
           public String helloWorld();
            
           }""";

        return writeSourceOfJavaStructureToFile(interfaceInfo, source)
    }

    /**
     * Adds a Java implementation of the supplied interface name to the source directory of the build project
     * @param className
     * @param interfaceName
     * @return
     */
    GradleProjectBuilder addJavaImplementationOfInterfaceToSource(String className, String interfaceName) {
        JavaStructureInfo interfaceInfo = new JavaStructureInfo(interfaceName);
        JavaStructureInfo classInfo = new JavaStructureInfo(className);

        String source = """package $classInfo.packageName;

import $interfaceInfo.canonicalName;

public class $classInfo.simpleName implements $interfaceInfo.simpleName
{

   public String helloWorld() {
        return "Howdy Friend";
    
    }
}
""";

        writeSourceOfJavaStructureToFile(classInfo, source)

        return this;
    }



    private GradleProjectBuilder writeSourceOfJavaStructureToFile(JavaStructureInfo interfaceInfo, String source) {
        String directoryName = buildDirectoryRoot.absolutePath + "/" + interfaceInfo.directoryName

        mkdirIfNotExist(directoryName)

        File sourceFile = new File(buildDirectoryRoot.absolutePath + "/" + interfaceInfo.sourceFileName)

        sourceFile.write(source, "UTF-8")

        return this;
    }

    private void mkdirIfNotExist(String directoryName) {
        if (!new File(directoryName).mkdirs() && !(new File(directoryName).exists())) {
            System.err.println("Could not create $directoryName");

            throw new IllegalStateException("Couldn't create directory $directoryName")

        }
    }

/**
     * Helper class that encapsulates logic for converting a class name into various other strings necessary to auto-generate files
     */
    private static class JavaStructureInfo
    {
        String packageName

        String simpleName

        String sourceFileName

        String canonicalName

        String directoryName

        JavaStructureInfo(String classCanonicalName)
        {
            packageName = classCanonicalName.split("\\.").dropRight(1).join(".")
            simpleName = classCanonicalName.split("\\.").last()
            this.canonicalName = classCanonicalName
            directoryName = "src/main/java/${packageName.replace('.','/')}"
            sourceFileName = "$directoryName/${simpleName}.java"

        }

    }
}
