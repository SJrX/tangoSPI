package net.sjrx.gradle.plugins.tango.integrationtests.testkit

import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.*

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class TestKitBaseTest extends Specification {

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "hello world task prints hello world"() {
        given:
        buildFile << """
    task helloWorld { 
        doLast { 
            println 'hello world!'
        }
    }
"""

        when:
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).withArguments('helloWorld').build()

        then:

        result.output.contains('hello world!')
        result.task(":helloWorld").outcome == SUCCESS
    }

}
