package net.sjrx.gradle.plugins.tango.integrationtests

import net.sjrx.gradle.plugins.tango.integrationtests.fixtures.GradleProjectBuilder

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS


/**
 * These are some simple smoke tests for the GradleProjectBuilder
 *
 * The unifying theme of tests in this method, is that if they fail, they will most likely be caused by failures in net.sjrx.gradle.plugins.tango.integrationtests.fixtures.GradleProjectBuilder, and not the plugin.
 */
class FixtureSmokeTestSpec extends AbstractBaseSpec {

    // This test is more about sanity checking our fixture builder library
    def "Java interface and class implementing interface from GradleProjectBuilder can pass the :classes step"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().applyJava()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments("classes").build()

        then:
        result.task(":classes").outcome == SUCCESS
    }

    def "Java interface and Scala class implementing interface from GradleProjectBuilder can pass the :classes step"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().applyJava().applyScala()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addScalaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments("classes").build()

        then:
        result.task(":classes").outcome == SUCCESS
    }

    def "Java interface and Groovy class implementing interface from GradleProjectBuilder can pass the :classes step"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().applyJava().applyGroovy()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addGroovyImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments("classes").build()

        then:
        result.task(":classes").outcome == SUCCESS
    }

    def "Running fixture with Gradle 3.1 executes with Gradle 3.1"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().gradleVersion("3.1").printGradleVersionOnStartUp()

        when:
        def result = builder.prepareRunner().withArguments("tasks").build()

        then:
        println result.output
        result.output.contains("Running gradle version: 3.1")
    }

    def "Running fixture with Gradle 4.0.2 executes with Gradle 4.0.2"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().gradleVersion("4.0.2").printGradleVersionOnStartUp()

        when:
        def result = builder.prepareRunner().withArguments("tasks").build()

        then:
        println result.output
        result.output.contains("Running gradle version: 4.0.2")
    }
}