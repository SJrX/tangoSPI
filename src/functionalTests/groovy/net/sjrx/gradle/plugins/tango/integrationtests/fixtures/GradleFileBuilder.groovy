package net.sjrx.gradle.plugins.tango.integrationtests.fixtures

import org.gradle.testkit.runner.GradleRunner
import org.gradle.tooling.model.GradleProject

/**
 * Helper class for generating a Gradle File Builder for Test Purposes
 *
 * TODO Evaluate if this can be ported to Native Groovy Builders, when familiar with them.
 */
class GradleProjectBuilder {

    private String gradleFile = ""

    private File buildDirectoryRoot = null;

    boolean nonPluginApplied = false

    /**
     * Force Private Constructor, use static builder methods.
     */
    private GradleProjectBuilder()
    {

    }

    GradleProjectBuilder loadAndApplyTangoPlugin() {
        verifyPluginStatementCanBeExecuted()
        gradleFile += """plugins { 
        id "net.sjrx.tangospi"
    }
    """;
        return this;
    }

    GradleProjectBuilder loadTangoPlugin() {
        verifyPluginStatementCanBeExecuted()
        // Source: http://mrhaki.blogspot.ca/2016/09/gradle-goodness-add-but-do-not-apply.html
        gradleFile += """plugins { 
    id "net.sjrx.tangospi" apply false}
    
"""
        return this
    }

    GradleProjectBuilder applyTangoPlugin() {
        nonPluginApplied = true

        gradleFile += 'apply plugin: "net.sjrx.tangospi"'

        return this
    }

    private verifyPluginStatementCanBeExecuted()
    {
        if(nonPluginApplied)
        {
            throw new IllegalStateException("""Your build will fail, because the tango plugin needs to be applied first. You will most likely get the following error from Gradle:
>>> "only buildscript {} and other plugins {} script blocks are allowed before plugins {} blocks, no other statements are allowed"
""");
        }
    }



    GradleProjectBuilder applyJava() {
        gradleFile += "apply plugin: 'java'\n"
        nonPluginApplied = true
        return this;
    }

    GradleRunner prepareRunner() {
        assert buildDirectoryRoot != null

        new File(buildDirectoryRoot.getAbsolutePath() + "/build.gradle").write(gradleFile, "UTF-8")

        return GradleRunner.create().withPluginClasspath().withProjectDir(buildDirectoryRoot)
    }


    static GradleProjectBuilder empty()
    {
        return new GradleProjectBuilder();
    }

    GradleProjectBuilder withTempDirectory(final File buildDirectoryRoot) {

        this.buildDirectoryRoot = buildDirectoryRoot

        return this;
    }
}
