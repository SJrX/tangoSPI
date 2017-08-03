package net.sjrx.gradle.plugins.tango

import groovy.transform.PackageScope
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.PluginCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sjr on 7/23/17.
 */
class TangoSPIPlugin implements Plugin<Project> {

    /**
     * Task name comes from
     */
    final String TASK_NAME = "generateProviderConfigurationFile"

    final String TEST_TASK_NAME = "generateProviderConfigurationFileTest"

    /**
     * Gradle Plugin configuration name (i.e., what users will use to configure the plugin
     */
    final String PLUGIN_CONFIGURATION_NAME = "tangospi"

    final String TASK_DESCRIPTION = "Generates provider configuration files necessary to allow the java.util.ServiceLoader to locate an implementation in the main source set."

    final String TEST_TASK_DESCRIPTION = "Generates provider configuration files necessary to allow the java.util.ServiceLoader to locate an implementation in the test source set."

    private static final Logger log = LoggerFactory.getLogger(TangoSPIPlugin.class)

    /**
     * Java task constants
     */
    private static final CLASSES_JAVA_TASK_NAME = JavaPlugin.CLASSES_TASK_NAME

    private static final COMPILE_JAVA_TASK_NAME = JavaPlugin.COMPILE_JAVA_TASK_NAME

    private static final TEST_CLASSES_JAVA_TASK_NAME = JavaPlugin.TEST_CLASSES_TASK_NAME

    private static final COMPILE_TEST_JAVA_TASK_NAME = JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME

    /**
     * Scala constants
     *
     * Don't seem to be defined anywhere that I can see
     *
     */
    private static final COMPILE_SCALA_TASK_NAME = "compileScala" /* Doesn't seem to be defined anywhere I can see */

    private static final COMPILE_TEST_SCALA_TASK_NAME = "compileTestScala"


    private static final MAIN_SOURCE_SET_NAME = "main"

    private static final TEST_SOURCE_SET_NAME = "test"

    @Override
    void apply(Project project) {
        project.extensions.create(PLUGIN_CONFIGURATION_NAME, TangoSPIPluginExtension)

        project.afterEvaluate {
            PluginCollection<JavaPlugin> javaPlugins = project.getPlugins().withType(JavaPlugin)

            switch (javaPlugins.size()) {
                case 0:
                    throw new GradleException("The Tango SPI Plugin requires the java plugin to be applied to this project")

                case 1:
                    applyToJavaPlugin(project)

                    break

                default:
                    throw new IllegalStateException("The Tango SPI Plugin does not know how to handle when there is more than one JavaPlugin applied. Please report this to the developer if this happens, as they didn't think it could ever happen but that was incorrect. The size reported by the number of plugins is ${javaPlugins.size}")


            }
        }
    }

    /**
     * Only exists to get around our usage of afterEvaluate(), which cannot be unit tested:
     * https://discuss.gradle.org/t/how-to-test-cutom-plugin-with-afterevaluate/5772
     */
    @PackageScope
    void applyToJavaPlugin(Project project) {

        createTask(project, TASK_NAME, TASK_DESCRIPTION, MAIN_SOURCE_SET_NAME, CLASSES_JAVA_TASK_NAME, COMPILE_JAVA_TASK_NAME, COMPILE_SCALA_TASK_NAME)

        createTask(project, TEST_TASK_NAME, TEST_TASK_DESCRIPTION, TEST_SOURCE_SET_NAME, TEST_CLASSES_JAVA_TASK_NAME, COMPILE_TEST_JAVA_TASK_NAME, COMPILE_TEST_SCALA_TASK_NAME)
    }

    /**
     * Helper method which creates the task and wires up the appropriate dependencies
     *
     * @param project
     * @param taskName
     * @param taskDescription
     * @param sourceSetName
     * @param classesTaskName
     * @param compileJavaTaskName
     * @param optionalOtherDependencies
     */
    static void createTask(Project project, String taskName, String taskDescription, String sourceSetName, String classesTaskName, String compileJavaTaskName, String... optionalOtherDependencies) {
        log.debug("Adding task {} of type {} ", taskName, GenerateProviderConfigurationFileTask)

        def mainTask = project.task(taskName, type: GenerateProviderConfigurationFileTask, description: taskDescription, {GenerateProviderConfigurationFileTask task ->
            task.sourceSetName = sourceSetName
        })

        project.getTasksByName(classesTaskName, false).head().dependsOn.add(mainTask)
        mainTask.dependsOn(project.getTasksByName(compileJavaTaskName, false).head())
        log.debug("Inserted dependency {} <- {} <- {} to ensure automagically created SPI mapping files for interfaces in {} source set", compileJavaTaskName, taskName, classesTaskName, sourceSetName)

        optionalOtherDependencies.each { dep ->
            Set<Task> optionalDependencyTask = project.getTasksByName(dep, false)
            if(!optionalDependencyTask.empty)
            {
                log.debug("Inserted additional dependency {} <- {} to ensure automagically created SPI mapping files for interfaces in {} source set", dep, taskName, sourceSetName)
                mainTask.dependsOn(optionalDependencyTask.head())
            }
        }

    }
}
