package net.sjrx.gradle.plugins.tango.integrationtests.testkit

import net.sjrx.gradle.plugins.tango.integrationtests.fixtures.GradleProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import java.util.regex.Pattern

import static org.gradle.testkit.runner.TaskOutcome.*

import spock.lang.Specification

class TestKitBaseTest extends Specification {

    /**
     * Do not point this to anything in the source code, if the source breaks the test should break.
     */
    private static final String EXPECTED_GENERATE_SPI_TASK_NAME = "generateProviderConfigurationFileJava"

    private static final String EXPECTED_SPI_TASK_DESCRIPTION = "Generate provider configuration files necessary to allow the java.util.ServiceLoader to locate an implementation"

    private static final String JAVA_SPI_INTERFACE_NAME = "fixture.example.SimpleService"

    private static final String JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX = "fixture.example.impl.SomeInterfaceImpl"


    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    GradleProjectBuilder emptyGradleBuildFile() {
        return GradleProjectBuilder.empty().withTempDirectory(testProjectDir.root)
    }

    // This test is more about sanity checking our fixture builder library
    def "Java interface and class implementing interface from GradleProjectBuilder can pass the :classes step"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().applyJava()
                .addJavaInterfaceToSource(JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1, JAVA_SPI_INTERFACE_NAME)

        when:
        def result = builder.prepareRunner().withArguments("classes").build()

        println result.output

        then:
        result.task(":classes").outcome == SUCCESS
    }


    def "no java plugin but tango plugin results in build failure"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin()

        when:
        def result = builder.prepareRunner().withArguments("tasks").buildAndFail()

        then:
        result.output.contains("The Tango SPI Plugin requires the java plugin to be applied to this project")
    }

    def "standard tango plugin loading with java plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
    }

    def "deferred tango plugin load with java plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadTangoPlugin().applyJava().applyTangoPlugin()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)

    }

    def "tango with default configuration block loads"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava().addTangoConfigurationBlock()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
    }

    def "tango plugin load with Scala plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyScala()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
    }


    def "tango plugin load with Groovy plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyGroovy()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
    }

    def "implementation of interface can be loaded using SPI and the tango plugin"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1, JAVA_SPI_INTERFACE_NAME)
                .addInterfaceToSearch(JAVA_SPI_INTERFACE_NAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(JAVA_SPI_INTERFACE_NAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, JAVA_SPI_INTERFACE_NAME, 1);

    }

    def "two implementation of interface can be loaded using SPI and the tango plugin"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1, JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 2, JAVA_SPI_INTERFACE_NAME)
                .addInterfaceToSearch(JAVA_SPI_INTERFACE_NAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(JAVA_SPI_INTERFACE_NAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, JAVA_SPI_INTERFACE_NAME, 2);

    }

    def "no implementations of interface can be loaded using SPI when not configured to search for interface"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1, JAVA_SPI_INTERFACE_NAME)
                /* NOTICE THIS LINE IS COMMENTED OUT: .addInterfaceToSearch(JAVA_SPI_INTERFACE_NAME) */
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(JAVA_SPI_INTERFACE_NAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, JAVA_SPI_INTERFACE_NAME, 0);

    }

    def "interfaces can be loaded if META-INF file is specified manually and tango is not configured to use that interface"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1, JAVA_SPI_INTERFACE_NAME)
        /* NOTICE THIS LINE IS COMMENTED OUT: .addInterfaceToSearch(JAVA_SPI_INTERFACE_NAME) */
                .manuallyAddProviderConfigurationFile(JAVA_SPI_INTERFACE_NAME, [JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1])
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(JAVA_SPI_INTERFACE_NAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, JAVA_SPI_INTERFACE_NAME, 1);
    }


    def "build fails when META-INF file already declared in resources folder for configured interface and tango is configured to use that interface"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1, JAVA_SPI_INTERFACE_NAME)
                .addInterfaceToSearch(JAVA_SPI_INTERFACE_NAME)
                .manuallyAddProviderConfigurationFile(JAVA_SPI_INTERFACE_NAME, [JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1])
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(JAVA_SPI_INTERFACE_NAME)

        when:
        def result = builder.prepareRunner().withArguments(":run", "-d").buildAndFail()

        println(result.output)
        then:
        result.task(":$EXPECTED_GENERATE_SPI_TASK_NAME").outcome == FAILED
        result.output.contains("Cannot auto-generate META-INF/services/$JAVA_SPI_INTERFACE_NAME as it seems to already exist in the source set.")

    }

    def "implementation of interface in test folder can be loaded using SPI and the tango plugin"() {
       when:
       null
        then: false

    }

    def "manually created META-INF file and automagically generated ones can co-exist"() {
        given:
        String MANUAL_JAVA_SPI_INTERFACE_NAME = JAVA_SPI_INTERFACE_NAME + "1";
        String AUTOMATIC_JAVA_SPI_INTERFACE_NAME = JAVA_SPI_INTERFACE_NAME + "2";
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(MANUAL_JAVA_SPI_INTERFACE_NAME)
                .addJavaInterfaceToSource(AUTOMATIC_JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1, MANUAL_JAVA_SPI_INTERFACE_NAME)
                .manuallyAddProviderConfigurationFile(MANUAL_JAVA_SPI_INTERFACE_NAME, [JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 1])
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 2, AUTOMATIC_JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(JAVA_SPI_INTERFACE_IMPLEMENTATION_PREFIX + 3, AUTOMATIC_JAVA_SPI_INTERFACE_NAME)
                .addInterfaceToSearch(AUTOMATIC_JAVA_SPI_INTERFACE_NAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(AUTOMATIC_JAVA_SPI_INTERFACE_NAME, MANUAL_JAVA_SPI_INTERFACE_NAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, AUTOMATIC_JAVA_SPI_INTERFACE_NAME, 2);
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, MANUAL_JAVA_SPI_INTERFACE_NAME, 1);
    }

    def "multiple interfaces are supported by tango and mapping files are generated properly"() {
        when:
        null

        then:
        false
    }


    def "implementation of interface written in Scala can be loaded using SPI and the tango plugin"() {
        when:
        null

        then:
        false

    }

    def "implementation of the interface written in Groovy can bo loaded using SPI and the tango plugin"() {
        when:
        null
        then:
        false
    }



    void numberOfLoadedInterfacesMatchesExpectedInBuildResult(BuildResult result, String interfaceName, int expectedMatches)
    {
        assert result.task(":run").outcome == SUCCESS

        def matcher = result.output =~ /SPI Count: ${Pattern.quote(interfaceName)} ==> (?<count>[0-9]+)/

        if(matcher.find())
        {
            int foundMatches = Integer.valueOf(matcher.group('count'))

            if(expectedMatches != foundMatches)
            {
                System.err.println("ERROR ABOUT TO FAIL")
                System.err.println(result.output)
                System.err.println(result.getProperties())
                System.err.println(testProjectDir.root)
                Thread.sleep(500000);
            }
            assert expectedMatches == foundMatches
        } else {
            throw new IllegalStateException("Could not find expected string in output: $result.output with regular expression: ${matcher.pattern().pattern()}");
        }
    }

}
