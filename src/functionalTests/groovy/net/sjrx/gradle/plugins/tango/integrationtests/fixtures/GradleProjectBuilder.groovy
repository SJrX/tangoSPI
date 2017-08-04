package net.sjrx.gradle.plugins.tango.integrationtests.fixtures

import groovy.transform.CompileStatic
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.DefaultGradleRunner

import java.lang.management.ManagementFactory

/**
 * Builder class that abstracts details for building projects and source files for testing purposes.
 *
 * For the most part methods in this class are applied in sequence to the gradleFile, so if you inverse two operations the build file will do those operations in that order.
 *
 * Unfortunately the API is somewhat inconsistent at present, as the following methods have deferred effects:
 *  1) Adding interfaces for tango to search for, will not be processed until you apply the tango configuration.
 *  2) Adding dependencies to the build file, will not be processed until the build.gradle file is rendered.
 *  3) Adding repositories to the build file, will not be processed until the build.gradle file is rendered.
 *
 */
@CompileStatic
class GradleProjectBuilder {

    /**
     * Accumulates the gradle file that we are building.
     */
    private String gradleFile = ""

    /**
     * Root directory of project (build file and other resources will be created within this directory)
     */
    private File buildDirectoryRoot = null

    /**
     * Flag variable that keeps track of whether we have done anything other than apply a plugin.
     */
    boolean nonPluginApplied = false

    /**
     * Interfaces that we expect the plugin to search for
     */
    private List<String> interfacesToSearch = new ArrayList<>()

    /**
     * Repositories we need added to the build file
     */
    private Set<String> repositories = new TreeSet<>()

    /**
     * Dependencies we need added to the build file.
     */
    private Set<String> dependencies = new TreeSet<>()

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
     *     id "net.sjrx.tangospi"
     * }
     * @return this
     */
    GradleProjectBuilder loadAndApplyTangoPlugin() {
        verifyPluginStatementCanBeExecuted()
        gradleFile += """plugins { 
        id "net.sjrx.tangospi"
    }
    """
        return this
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
        useMavenCentral()
        addDependency("compile", "org.scala-lang:scala-library:2.11.11")
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
        useMavenCentral()
        addDependency("compile", "org.codehaus.groovy:groovy-all:2.4.7")

        return applyPlugin('groovy')

    }

    /**
     * Adds a repository for the build script
     *
     * i.e., semantically puts the equivalent in the build file
     *
     * repositories {
     *  mavenCentral()
     * }
     *
     * @return
     */
    GradleProjectBuilder useMavenCentral() {

        this.repositories.add("mavenCentral()")

        return this
    }

    /**
     * Creates a dependency for the build script
     *
     * i.e., semantically puts the equivalent in the build file
     *
     * dependencies {
     *     type 'dep'
     * }
     *
     * @param type
     * @param dep
     * @return
     */
    GradleProjectBuilder addDependency(String type, String dep)
    {
        this.dependencies.add("$type '$dep'".toString())
        return this
    }

    /**
     * Writes the built up gradle file and then returns a GradleRunner ready to execute against this build file.
     *
     * @return
     */
    GradleRunner prepareRunner() {
        assert buildDirectoryRoot != null

        String renderedGradleFile = renderGradleFile()

        new File(buildDirectoryRoot.getAbsolutePath() + "/build.gradle").write(renderedGradleFile, "UTF-8")

        GradleRunner r = GradleRunner.create()

        passThruJavaAgent(r)

        return r.withPluginClasspath().withProjectDir(buildDirectoryRoot)

    }



    /**
     * Returns a GradleProjectBuilder that represents an entirely empty build file.
     * @return
     */
    static GradleProjectBuilder empty()
    {
        return new GradleProjectBuilder()
    }

    /**
     * Creates a build file that will be written in a particular directory.
     * @param buildDirectoryRoot
     * @return
     */
    GradleProjectBuilder withTempDirectory(final File buildDirectoryRoot) {

        this.buildDirectoryRoot = buildDirectoryRoot

        return this
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

        return this
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
        String directoryName = buildDirectoryRoot.absolutePath + "/src/main/resources/META-INF/services/"

        mkdirIfNotExist(directoryName)

        new File("$directoryName/$interfaceName").write("""# Autogenerated for test\n${classNames.join("\n")}""", "UTF-8")

        return this
    }

    /**
     * Adds some functionality to the gradle build that creates two new tasks, run and runTest, which when run outputs information about service loading for the requestedInterfaceNames
     *
     * The primary use of this functionality is to do end-to-end testing of the plugin. This actually uses the service loader to verify that they _can_ be loaded which is more confidence
     * inducing than simply manually validating files on disk.
     *
     * The output of the process is two things which can be scraped and tested against:
     *
     * SPI Implementation: <InterfaceName> ==> <ImplementationCanonicalClassName>
     * SPI Count: <InterfaceName> ==> <count>
     *
     * @param requestedInterfaceNames - each one of these will have information printed.
     * @return
     */
    GradleProjectBuilder enableValidationRunTaskForInterface(String... requestedInterfaceNames) {

        JavaStructureInfo info = new JavaStructureInfo("net.sjrx.fixtures.executor.ServiceLoaderTestExecutor")

        nonPluginApplied = true
        gradleFile += """

task run(type: JavaExec) { 
    classpath = sourceSets.main.runtimeClasspath
    main = '$info.canonicalName'
    dependsOn classes
}



task runTest(type: JavaExec) { 
    classpath = sourceSets.test.runtimeClasspath
    main = '$info.canonicalName'
    dependsOn testClasses
}

"""

        String requestedInterfacesAsJavaArray = requestedInterfaceNames.collect{String.valueOf(it) + ".class" }.join(",")

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
        return "CurrentBuildFile: ${this.renderGradleFile()}\n"
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
            
           }"""

        return writeSourceOfJavaStructureToFile(interfaceInfo, source)
    }

    /**
     * Adds a Java implementation of the supplied interface name to the source directory of the build project
     *
     * @param className
     * @param interfaceName
     * @param sourceSetName
     *
     * @return
     */
    GradleProjectBuilder addJavaImplementationOfInterfaceToSource(String className, String interfaceName, String sourceSetName = "main") {
        JavaStructureInfo interfaceInfo = new JavaStructureInfo(interfaceName)
        JavaStructureInfo classInfo = new JavaStructureInfo(className, sourceSetName)

        String source = """package $classInfo.packageName;

import $interfaceInfo.canonicalName;

public class $classInfo.simpleName implements $interfaceInfo.simpleName
{

   public String helloWorld() {
        return "Howdy Friend";
    
    }
}
"""

        return writeSourceOfJavaStructureToFile(classInfo, source)

    }

    /**
     * Adds a Scala implementation of the supplied interface to the source directory of the build project
     *
     * @param className
     * @param interfaceName
     * @param sourceSetName
     * @return
     */
    GradleProjectBuilder addScalaImplementationOfInterfaceToSource(String className, String interfaceName, String sourceSetName = "main") {
        JavaStructureInfo interfaceInfo = new JavaStructureInfo(interfaceName)
        JavaStructureInfo classInfo = new JavaStructureInfo(className, sourceSetName, "scala")

        String source = """package $classInfo.packageName

import $interfaceInfo.canonicalName

class $classInfo.simpleName extends $interfaceInfo.simpleName { 

    def helloWorld(): String = {
        "Wowzers!"
    }
}
"""
        return writeSourceOfJavaStructureToFile(classInfo, source)
    }
    /**
     * Adds a Groovy implementation of the supplied interface to the source directory of the build project
     *
     * @param className
     * @param interfaceName
     * @param sourceSetName
     *
     */
    def addGroovyImplementationOfInterfaceToSource(String className, String interfaceName, String sourceSetName = "main") {
        JavaStructureInfo interfaceInfo = new JavaStructureInfo(interfaceName)
        JavaStructureInfo classInfo = new JavaStructureInfo(className, sourceSetName, "groovy")

        String source = """package $classInfo.packageName

import $interfaceInfo.canonicalName

class $classInfo.simpleName implements $interfaceInfo.simpleName
{

   String helloWorld() {
        return \"\"\"Really Groovy
 in \$this\"\"\"
    }
}
"""
        return writeSourceOfJavaStructureToFile(classInfo, source)

    }



    private GradleProjectBuilder applyPlugin(String plugin) {
        gradleFile += "apply plugin: '$plugin'\n"
        nonPluginApplied = true
        return this
    }

    private verifyPluginStatementCanBeExecuted()
    {
        if(nonPluginApplied)
        {
            throw new IllegalStateException("""Your build will fail, because the tango plugin needs to be applied first. You will most likely get the following error from Gradle:
>>> "only buildscript {} and other plugins {} script blocks are allowed before plugins {} blocks, no other statements are allowed"
""")
        }
    }


    private GradleProjectBuilder writeSourceOfJavaStructureToFile(JavaStructureInfo interfaceInfo, String source) {
        String directoryName = buildDirectoryRoot.absolutePath + "/" + interfaceInfo.directoryName

        mkdirIfNotExist(directoryName)

        File sourceFile = new File(buildDirectoryRoot.absolutePath + "/" + interfaceInfo.sourceFileName)

        sourceFile.write(source, "UTF-8")

        return this
    }

    private static void mkdirIfNotExist(String directoryName) {
        if (!new File(directoryName).mkdirs() && !(new File(directoryName).exists())) {
            throw new IllegalStateException("Couldn't create directory $directoryName")
        }
    }

    /**
     * Passes through any java agent arguments we may have received to the subtask
     *
     * See: https://discuss.gradle.org/t/gradle-plugins-integration-tests-code-coverage-with-jacoco-plugin/12403
     *
     * @param r
     */
    private static void passThruJavaAgent(GradleRunner r) {
        List<String> jvmArguments = new ArrayList<>()
        for (String s : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (s.startsWith("-javaagent") && s.contains("jacoco")) {
                jvmArguments += s.replaceAll("build/", System.getProperty("user.dir") + "/build/")
            }
        }

        if (r instanceof DefaultGradleRunner) {
            ((DefaultGradleRunner) r).withJvmArguments(jvmArguments)
        }
    }

    /**
     * Generates the gradle file that will be used (dynamically generating some fields as needed)
     */
    private String renderGradleFile() {
        String gradleFileToWrite = """
$gradleFile

repositories { 
    ${this.repositories.join("\n")}
}

dependencies { 
    ${this.dependencies.join("\n")}
}
\n
"""
        return gradleFileToWrite
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

        JavaStructureInfo(String classCanonicalName, String directory = "main", String language = "java")
        {
            packageName = classCanonicalName.split("\\.").dropRight(1).join(".")
            simpleName = classCanonicalName.split("\\.").last()
            this.canonicalName = classCanonicalName
            directoryName = "src/$directory/$language/${packageName.replace('.','/')}"
            sourceFileName = "$directoryName/${simpleName}.$language"

        }

    }
}
