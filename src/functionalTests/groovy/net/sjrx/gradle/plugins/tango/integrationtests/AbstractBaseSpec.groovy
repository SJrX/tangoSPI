package net.sjrx.gradle.plugins.tango.integrationtests

import net.sjrx.gradle.plugins.tango.integrationtests.fixtures.GradleProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.regex.Pattern

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS


/**
 * Abstract test case with some helper methods, and shared setup
 */
abstract class AbstractBaseSpec extends Specification {

    /**
     * Do not point this to anything in the source code, if the source breaks the test should break.
     */
    protected static final String EXPECTED_GENERATE_SPI_TASK_NAME = "generateProviderConfigurationFile"
    protected static final String EXPECTED_SPI_TASK_DESCRIPTION = "Generates provider configuration files necessary to allow the java.util.ServiceLoader to locate an implementation in the main source set."


    protected static final String EXPECTED_GENERATE_SPI_TEST_TASK_NAME = "generateProviderConfigurationFileTest"
    protected static final String EXPECTED_SPI_TEST_TASK_DESCRIPTION = "Generates provider configuration files necessary to allow the java.util.ServiceLoader to locate an implementation in the test source set."

    protected static final String SPI_INTERFACE_CLASSNAME = "fixture.example.SimpleService"

    protected static final String SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX = "fixture.example.impl.SomeInterfaceImpl"


    @Rule protected final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    GradleProjectBuilder emptyGradleBuildFile() {
        return GradleProjectBuilder.empty().withTempDirectory(testProjectDir.root)
    }


    void numberOfLoadedInterfacesMatchesExpectedInBuildResult(BuildResult result, String interfaceName, int expectedMatches)
    {

        if(result.task(":run") != null)
        {
            assert result.task(":run").outcome == SUCCESS
        } else if(result.task(":runTest") != null)
        {
            assert result.task(":runTest").outcome == SUCCESS
        } else {
            throw new IllegalStateException("One of run or runTest targets must run in order for this helper method to be valid")
        }

        def matcher = result.output =~ /SPI Count: ${Pattern.quote(interfaceName)} ==> (?<count>[0-9]+)/

        if(matcher.find())
        {
            int foundMatches = Integer.valueOf(matcher.group('count'))

            assert expectedMatches == foundMatches
        } else {
            throw new IllegalStateException("Could not find expected string in output: $result.output with regular expression: ${matcher.pattern().pattern()}")
        }
    }

}