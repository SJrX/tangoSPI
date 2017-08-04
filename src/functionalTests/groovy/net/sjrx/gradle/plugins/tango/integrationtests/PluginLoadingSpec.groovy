package net.sjrx.gradle.plugins.tango.integrationtests

import net.sjrx.gradle.plugins.tango.integrationtests.fixtures.GradleProjectBuilder


/**
 * These tests validate that the plugin is successfully able to load, but do not execute the meat of the functionality.
 *
 * Most likely failures in these classes will point to problems in net.sjrx.gradle.plugins.tango.TangoSPIPlugin
 */
class PluginLoadingSpec extends AbstractBaseSpec {

    def "no java plugin but tango plugin results in build failure"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin()

        when:
        def result = builder.prepareRunner().withArguments("tasks").buildAndFail()

        then:
        result.output.contains("The Tango SPI Plugin requires the java plugin to be applied to this project")
    }

    def "standard tango plugin loading with java plugin successfully creates tasks"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
        result.output.contains(EXPECTED_GENERATE_SPI_TEST_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TEST_TASK_DESCRIPTION)
    }

    def "deferred tango plugin load with java plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadTangoPlugin().applyJava().applyTangoPlugin()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
        result.output.contains(EXPECTED_GENERATE_SPI_TEST_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TEST_TASK_DESCRIPTION)

    }

    def "tango with default configuration block loads"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava().addTangoConfigurationBlock()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
        result.output.contains(EXPECTED_GENERATE_SPI_TEST_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TEST_TASK_DESCRIPTION)
    }

    def "tango plugin load with Scala plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyScala()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
        result.output.contains(EXPECTED_GENERATE_SPI_TEST_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TEST_TASK_DESCRIPTION)
    }


    def "tango plugin load with Groovy plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyGroovy()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains(EXPECTED_GENERATE_SPI_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TASK_DESCRIPTION)
        result.output.contains(EXPECTED_GENERATE_SPI_TEST_TASK_NAME)
        result.output.contains(EXPECTED_SPI_TEST_TASK_DESCRIPTION)
    }
}