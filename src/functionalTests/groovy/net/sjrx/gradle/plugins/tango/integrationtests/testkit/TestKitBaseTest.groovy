package net.sjrx.gradle.plugins.tango.integrationtests.testkit

import net.sjrx.gradle.plugins.tango.integrationtests.fixtures.GradleProjectBuilder
import org.gradle.tooling.model.GradleProject
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class TestKitBaseTest extends Specification {

    /**
     * Do not point this to anything in the source code, if the source breaks the test should break.
     */
    private static final String EXPECTED_GENERATE_SPI_TASK_NAME = "generateProviderConfigurationFileJava"

    private static final String EXPECTED_SPI_TASK_DESCRIPTION = "Generate provider configuration files necessary to allow the java.util.ServiceLoader to locate an implementation"




    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    GradleProjectBuilder emptyBuilder() {
        return GradleProjectBuilder.empty().withTempDirectory(testProjectDir.root)
    }


    def "no java plugin but tango plugin results in build failure"() {
        given:
        GradleProjectBuilder builder = emptyBuilder().loadAndApplyTangoPlugin()

        when:
        def result = builder.prepareRunner().withArguments("tasks").buildAndFail()

        then:
        result.output.contains("The Tango SPI Plugin requires the java plugin to be applied to this project")
    }

    def "standard tango plugin loading with java plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyBuilder().loadAndApplyTangoPlugin().applyJava()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
    }

    def "deferred tango plugin load with java plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyBuilder().loadTangoPlugin().applyJava().applyTangoPlugin()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)

    }

    def "tango with default options loads"() {
        given:
        GradleProjectBuilder builder = emptyBuilder().loadAndApplyTangoPlugin().applyJava().addTangoConfigurationBlock()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        print builder

        print result.output

        then:
        result.output.contains("Yay!")
    }



}
