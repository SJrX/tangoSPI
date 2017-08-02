package net.sjrx.gradle.plugins.tango.integrationtests.testkit

import net.sjrx.gradle.plugins.tango.integrationtests.fixtures.GradleProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class TestKitBaseTest extends Specification {

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
        result.output.contains("generateSPIJava")
    }

    def "deferred tango plugin load with java plugin successfully creates task"() {
        given:
        GradleProjectBuilder builder = emptyBuilder().loadTangoPlugin().applyJava().applyTangoPlugin()

        when:
        def result = builder.prepareRunner().withArguments("tasks", "--all").build()

        then:
        result.output.contains("generateSPIJava")
    }

}
