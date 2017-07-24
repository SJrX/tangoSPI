package net.sjrx.gradle.plugins.tango

import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

/**
 * Created by sjr on 7/23/17.
 */
class TangoSPIPlugin implements Plugin<Project> {

    final String TASK_NAME = "generateSPIMappingFiles";

    private static final Logger log = LoggerFactory.getLogger(TangoSPIPlugin.class)

    @Override
    public void apply(Project project) {
        project.getPlugins().apply("java");

        if(project.getPlugins().withType(JavaPlugin).empty)
        {
            log.error("The Tango SPI Plugin requires the java plugin to be applied to this project");
            return;
        }

        project.extensions.create(TASK_NAME, TangoSPIPluginExtension)
        def newTask = project.task(TASK_NAME, type: GenerateSPIMappingFilesTask)
        project.getTasksByName("classes", false).head().dependsOn.add(newTask)
        log.debug("Added dependency on classes task to task: " + TASK_NAME + " which enables automagically generate SPI mapping files for interfaces")
    }
}
