package net.sjrx.gradle.plugins.tango

import net.sjrx.gradle.plugins.tango.spi.FullBuildDirectoryScanner
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Created by sjr on 7/23/17.
 */
class GenerateSPIMappingFilesTask extends DefaultTask {

    final String METAINF_SERVICES_DIRECTORY = "META-INF/services"

    final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    final Logger log = LoggerFactory.getLogger(GenerateSPIMappingFilesTask.class)



    @TaskAction
    def generate() {

        def tangoSPISettings = project.extensions.findByType(TangoSPIPluginExtension)

        final Set<String> interfacesToGenerateMapping = new HashSet<>(Arrays.asList(tangoSPISettings.interfaces))

        if(interfacesToGenerateMapping.empty)
        {
            log.warn("No interfaces have been specified to generate automatic SPI Mapping data")
        }

        project.getPlugins().withType(JavaPlugin.class).each {

            log.debug("Generating SPI Mappings for Java code")

            // Adapted from: https://discuss.gradle.org/t/how-do-i-add-directories-to-the-main-resources-sourceset-in-a-gradle-plugin/5953/3

            SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets")
            sourceSets.each {

                log.debug("Searching for conflicting manually generated resource files")

                List<String> conflictingFilenames = interfacesToGenerateMapping.collect { interfaceName -> "META-INF/services/$interfaceName"}

                it.resources.each { resourceFile ->
                        log.debug("Checking for conflict with $resourceFile")

                        conflictingFilenames.each { conflictingFile ->
                            if(resourceFile.absolutePath.endsWith(conflictingFile))
                            {
                                throw new InvalidUserDataException("Cannot auto-generate $conflictingFile as it seems to already exist in the source set. Please delete this file from the source set, or remove it from the configuration of the tangospi plugin");
                            }

                        }

                }

                log.debug("Scanning for implementations of $interfacesToGenerateMapping in Source Set ${it.name} and directory ${it.allJava}")

                FullBuildDirectoryScanner fbds = new FullBuildDirectoryScanner(interfacesToGenerateMapping, Collections.singleton(it.output.classesDir));

                def servicesDirectory = new File(it.output.resourcesDir.getAbsolutePath() + File.separator + METAINF_SERVICES_DIRECTORY);

                servicesDirectory.mkdirs();

                if(!servicesDirectory.isDirectory())
                {
                    throw new IllegalStateException("Could not create required directory in output " + servicesDirectory.absolutePath)
                }

                fbds.interfaceToImplementationsMapping.each {
                    def serviceFile = new File(servicesDirectory.absolutePath + File.separator + it.key)
                    serviceFile.delete()
                    serviceFile << "#Generated by ${this.getClass().getCanonicalName()} on ${dateFormat.format(new Date())}\n"
                    it.value.each {
                        serviceFile << "$it\n"
                    }

                    log.info("Found {} implementations for {}", it.value.size(), it.key)
                }


            }
        }
    }
}
