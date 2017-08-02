package net.sjrx.gradle.plugins.tango.integrationtests

import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class BasicTests {

    public static final String BASE_GRADLE_BUILD_FILE = """buildscript {
    repositories {
        maven { url "${getPluginJarLocation()}" }
        mavenCentral() 
    }
    
    dependencies { 
        classpath group: 'net.sjrx.gradle.plugins', name:'tangospi-plugin', version: '0.1-SNAPSHOT'
    }
}

apply plugin: "net.sjrx.tangospi"

task showClasspath {
    doLast {
        buildscript.configurations.classpath.each { println it.name }
    }
}
"""

    public static final String getPluginJarName()
    {
        return "tangospi-plugin-0.1-SNAPSHOT.jar";
    }
    //TODO Get system jar path someother way
    public static final String getPluginJarLocation()
    {
        return "/home/sjr/development/TangoSPI/mvnRepo/";
    }

    private static File testWorkingDirectory = Paths.get("/tmp/tangoSpiTestDirectory").toFile() //Files.Files.createTempDirectory("tangoSpiTestDirectory");

    @Test
    public void testPluginLoads()
    {
        return;

        List<String> cmd = ["/home/sjr/Apps/gradle-2.12/bin/gradle", "wrapper","--gradle-version", "3.3"]

        new File(testWorkingDirectory.absolutePath + "/build.gradle").delete()

        executeCmdAndPrintOutput(cmd)

        System.out.println(System.getProperty("java.class.path"));
        System.out.println(testWorkingDirectory.absolutePath);

        new File(testWorkingDirectory.absolutePath + "/build.gradle").write(BASE_GRADLE_BUILD_FILE);

        executeCmdAndPrintOutput(["./gradlew", "showClasspath"]);

    }

    private static void executeCmdAndPrintOutput(List<String> cmd) {
        def proc = new ProcessBuilder(cmd).directory(testWorkingDirectory).redirectErrorStream(true).start()
        proc.getInputStream().eachLine { println it }
    }


}
