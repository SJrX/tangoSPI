package net.sjrx.gradle.plugins.tango

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.PluginCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sjr on 7/23/17.
 */
class TangoSPIPlugin implements Plugin<Project> {

    final String TASK_NAME = "generateSPIJava";

    private static final Logger log = LoggerFactory.getLogger(TangoSPIPlugin.class)


    private static final CLASSES_JAVA_TASK_NAME = JavaPlugin.CLASSES_TASK_NAME;

    @Override
    public void apply(Project project) {

        PluginCollection<JavaPlugin> javaPlugins = project.getPlugins().withType(JavaPlugin)

        switch (javaPlugins.size()) {
            case 0:
                throw new GradleException("The Tango SPI Plugin requires the java plugin to be applied to this project")

            case 1:
                project.extensions.create(TASK_NAME, TangoSPIPluginExtension)
                def newTask = project.task(TASK_NAME, type: GenerateSPIMappingFilesTask)
                project.getTasksByName(CLASSES_JAVA_TASK_NAME, false).head().dependsOn.add(newTask);

                log.debug("Added dependency on " + " task to task: " + CLASSES_JAVA_TASK_NAME + " which enables automagically generate SPI mapping files for interfaces")

                break;

            default:
                throw new IllegalStateException("The Tango SPI Plugin does not know how to handle when there is more than one JavaPlugin applied. Please report this to the developer if this happens, as they didn't think it could ever happen but that was incorrect. The size reported by the number of plugins is ${javaPlugins.size}");


        }
    }
}
