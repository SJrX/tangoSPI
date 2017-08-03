package net.sjrx.gradle.plugins.tango.integrationtests

import net.sjrx.gradle.plugins.tango.integrationtests.fixtures.GradleProjectBuilder
import spock.lang.Unroll

class SmokeTestWithMultipleVersions extends AbstractBaseSpec {

    static List<String> supportedGradleVersions = ["3.0","3.1", "3.2", "3.2.1", "3.3", "3.4", "3.4.1", "3.5", "3.5.1", /* "4.0", "4.0.1", "4.0.2"*/].sort()

    @Unroll
    def "Smoke test that generated meta data for Groovy, Scala, and Java implementations in both regular and tests works with Gradle #version"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava().applyGroovy().applyScala()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addGroovyImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)
                .addScalaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 2, SPI_INTERFACE_CLASSNAME)
                .addScalaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 3, SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 4, SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 5, SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 6, SPI_INTERFACE_CLASSNAME)
                .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withGradleVersion(version).withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 6)

        where:
        version << supportedGradleVersions
    }

}