package net.sjrx.gradle.plugins.tango.integrationtests

import net.sjrx.gradle.plugins.tango.integrationtests.fixtures.GradleProjectBuilder

import static org.gradle.testkit.runner.TaskOutcome.FAILED


class MainFunctionalitySpec extends AbstractBaseSpec {


    def "implementation of interface can be loaded using SPI and the tango plugin"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)
                .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 1)

    }

    def "two implementation of interface can be loaded using SPI and the tango plugin"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 2, SPI_INTERFACE_CLASSNAME)
                .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 2)

    }

    def "no implementations of interface can be loaded using SPI when not configured to search for interface"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)
                /* NOTICE THIS LINE IS COMMENTED OUT: .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME) */
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 0)

    }

    def "interfaces can be loaded if META-INF file is specified manually and tango is not configured to use that interface"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)
        /* NOTICE THIS LINE IS COMMENTED OUT: .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME) */
                .manuallyAddProviderConfigurationFile(SPI_INTERFACE_CLASSNAME, [SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1])
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 1)
    }


    def "build fails when META-INF file already declared in resources folder for configured interface and tango is configured to use that interface"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)
                .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME)
                .manuallyAddProviderConfigurationFile(SPI_INTERFACE_CLASSNAME, [SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1])
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":run", "-d").buildAndFail()

        then:
        result.task(":$EXPECTED_GENERATE_SPI_TASK_NAME").outcome == FAILED
        result.output.contains("Cannot auto-generate META-INF/services/$SPI_INTERFACE_CLASSNAME as it seems to already exist in the source set.")

    }

    def "implementation of interface in test folder can be loaded using SPI and the tango plugin"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME, "test")
                .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":runTest", "-d").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 1)
    }

    def "manually created META-INF file and automagically generated ones can co-exist"() {
        given:
        String MANUAL_JAVA_SPI_INTERFACE_NAME = SPI_INTERFACE_CLASSNAME + "1"
        String AUTOMATIC_JAVA_SPI_INTERFACE_NAME = SPI_INTERFACE_CLASSNAME + "2"
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(MANUAL_JAVA_SPI_INTERFACE_NAME)
                .addJavaInterfaceToSource(AUTOMATIC_JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, MANUAL_JAVA_SPI_INTERFACE_NAME)
                .manuallyAddProviderConfigurationFile(MANUAL_JAVA_SPI_INTERFACE_NAME, [SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1])
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 2, AUTOMATIC_JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 3, AUTOMATIC_JAVA_SPI_INTERFACE_NAME)
                .addInterfaceToSearch(AUTOMATIC_JAVA_SPI_INTERFACE_NAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(AUTOMATIC_JAVA_SPI_INTERFACE_NAME, MANUAL_JAVA_SPI_INTERFACE_NAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, AUTOMATIC_JAVA_SPI_INTERFACE_NAME, 2)
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, MANUAL_JAVA_SPI_INTERFACE_NAME, 1)
    }

    def "multiple interfaces are supported by tango and mapping files are generated properly"() {
        given:
        String FIRST_JAVA_SPI_INTERFACE_NAME = SPI_INTERFACE_CLASSNAME + "1"
        String SECOND_JAVA_SPI_INTERFACE_NAME = SPI_INTERFACE_CLASSNAME + "2"
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava()
                .addJavaInterfaceToSource(FIRST_JAVA_SPI_INTERFACE_NAME)
                .addJavaInterfaceToSource(SECOND_JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, FIRST_JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 2, SECOND_JAVA_SPI_INTERFACE_NAME)
                .addJavaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 3, SECOND_JAVA_SPI_INTERFACE_NAME)
                .addInterfaceToSearch(FIRST_JAVA_SPI_INTERFACE_NAME)
                .addInterfaceToSearch(SECOND_JAVA_SPI_INTERFACE_NAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SECOND_JAVA_SPI_INTERFACE_NAME, FIRST_JAVA_SPI_INTERFACE_NAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SECOND_JAVA_SPI_INTERFACE_NAME, 2)
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, FIRST_JAVA_SPI_INTERFACE_NAME, 1)
    }


    def "implementation of interface written in Scala can be loaded using SPI and the tango plugin"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava().applyScala()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addScalaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)
                .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        println result.output

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 1)

    }

    def "implementation of interface written in Scala in tests can be loaded using SPI and the tango plugin"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava().applyScala()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addScalaImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME, "test")
                .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":runTest", "-d").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 1)

    }

    def "implementation of the interface written in Groovy can be loaded using SPI and the tango plugin"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava().applyGroovy()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addGroovyImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME)
                .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":run").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 1)
    }

    def "implementation of the interface written in Groovy in tests can be loaded using SPI and the tango plugin"() {
        given:
        GradleProjectBuilder builder = emptyGradleBuildFile().loadAndApplyTangoPlugin().applyJava().applyGroovy()
                .addJavaInterfaceToSource(SPI_INTERFACE_CLASSNAME)
                .addGroovyImplementationOfInterfaceToSource(SPI_INTERFACE_IMPLEMENTATION_CLASSNAME_PREFIX + 1, SPI_INTERFACE_CLASSNAME, "test")
                .addInterfaceToSearch(SPI_INTERFACE_CLASSNAME)
                .addTangoConfigurationBlock()
                .enableValidationRunTaskForInterface(SPI_INTERFACE_CLASSNAME)

        when:
        def result = builder.prepareRunner().withArguments(":runTest").build()

        then:
        numberOfLoadedInterfacesMatchesExpectedInBuildResult(result, SPI_INTERFACE_CLASSNAME, 1)
    }





}
