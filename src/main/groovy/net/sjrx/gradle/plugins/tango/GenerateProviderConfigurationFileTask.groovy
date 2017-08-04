package net.sjrx.gradle.plugins.tango

import net.sjrx.gradle.plugins.tango.spi.FullBuildDirectoryScanner
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Task implementation which provides the heart of the functionality of this plugin, namely automatically generating META-INF/service files.
 *
 */
class GenerateProviderConfigurationFileTask extends DefaultTask {

    final String METAINF_SERVICES_DIRECTORY = "META-INF/services"

    final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")

    final Logger log = LoggerFactory.getLogger(GenerateProviderConfigurationFileTask.class)

    public String sourceSetName = null

    @TaskAction
    def generate() {

        if (sourceSetName == null)
        {
            throw new IllegalStateException("Source Set must be specified when creating plugin")
        }

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
            sourceSets.each { sourceSet ->

                if(sourceSet.name != sourceSetName)
                {
                    log.trace("Ignoring {} as this task only generates for {}", sourceSet.name, sourceSetName)
                    return
                }

                checkForConflictingMetaInfServiceFiles(sourceSet, interfacesToGenerateMapping)

                File servicesDirectory = createOutputDirectory(sourceSet)

                scanForImplementationsAndWriteProviderConfigurationFile(sourceSet, servicesDirectory, interfacesToGenerateMapping)


            }
        }
    }

    /**
     * Checks that the user hasn't specified there own file already which would cause us to silently overwrite it.
     *
     * This is primarily a defensive check to prevent hard to debug groaners.
     *
     * @param sourceSet
     */
    protected void checkForConflictingMetaInfServiceFiles(SourceSet sourceSet, Set<String> interfacesToGenerateMapping) {
        log.debug("Searching for conflicting manually generated resource files in source set: ${sourceSet.name}")

        List<String> conflictingFilenames = interfacesToGenerateMapping.collect { interfaceName -> "META-INF/services/$interfaceName" }

        sourceSet.resources.each { resourceFile ->
            log.debug("Checking for conflict with $resourceFile")

            conflictingFilenames.each { conflictingFile ->
                if (resourceFile.absolutePath.endsWith(conflictingFile)) {
                    throw new InvalidUserDataException("Cannot auto-generate $conflictingFile as it seems to already exist in the source set. Please delete this file from the source set, or remove it from the configuration of the tangospi plugin")
                }

            }

        }
    }

    /**
     * Create the appropriate output directory for the provider configuration file
     *
     * There probably is better way to do this in Groovy, or with some random library since we need to create many levels.
     *
     * @param sourceSet
     * @return
     */
    protected File createOutputDirectory(SourceSet sourceSet) {
        def servicesDirectory = new File(sourceSet.output.resourcesDir.getAbsolutePath() + File.separator + METAINF_SERVICES_DIRECTORY)
        servicesDirectory.mkdirs()
        if (!servicesDirectory.isDirectory()) {
            throw new IllegalStateException("Could not create required directory in output " + servicesDirectory.absolutePath)
        }

        log.debug("Appropriate directory created for source set {} : {} ", sourceSet.name, servicesDirectory.absolutePath)

        return servicesDirectory
    }

    /**
     * Finds all the implementations and then writes the appropriate files for them
     *
     * @param sourceSet
     * @param servicesDirectory
     */
    protected void scanForImplementationsAndWriteProviderConfigurationFile(SourceSet sourceSet, File servicesDirectory, Set<String> interfacesToGenerateMapping) {
        log.debug("Scanning for implementations of $interfacesToGenerateMapping in source set ${sourceSet.name} and directories: ${sourceSet.allJava.srcDirs} via $sourceSet.output.classesDir")
        FullBuildDirectoryScanner fbds = new FullBuildDirectoryScanner(interfacesToGenerateMapping, Collections.singleton(sourceSet.output.classesDir))


        fbds.interfaceToImplementationsMapping.each { interfaceToImplMapping ->
            def serviceFile = new File(servicesDirectory.absolutePath + File.separator + interfaceToImplMapping.key)
            serviceFile.delete() // Delete the existing file, it won't be a user specified file, and we don't support partial updates yet, so if we see the file we should just delete it.

            serviceFile << "#Generated by ${this.getClass().getCanonicalName()} on ${dateFormat.format(new Date())}\n"
            interfaceToImplMapping.value.each {
                serviceFile << "$it\n"
            }

            log.info("Found {} implementations for {}", interfaceToImplMapping.value.size(), interfaceToImplMapping.key)
        }
    }


}
