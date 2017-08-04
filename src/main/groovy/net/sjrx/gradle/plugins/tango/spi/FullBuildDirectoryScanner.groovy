package net.sjrx.gradle.plugins.tango.spi

import net.jcip.annotations.Immutable
import org.objectweb.asm.ClassReader

/**
 * Generates a mapping of all classes which implement the interfaces, in the supplied directories.
 */
@Immutable
class FullBuildDirectoryScanner {

    /**
     * The name of the interfaces to search for implementations of
     */
    private final Set<String> interfacesToScanFor

    /**
     * The result of the search
     */
    private final Map<String, List<String>> foundInterfaces


    FullBuildDirectoryScanner(Set<String> interfacesToScanFor, Set<File> directoriesToScan)
    {
        // Do search in constructor so that we can have immutable object
        this.interfacesToScanFor = interfacesToScanFor

        Map<String, List<String>> foundInterfaces = new HashMap<>()
        interfacesToScanFor.each { foundInterfaces.put(it, new ArrayList<String>()) }

        directoriesToScan.each { walk(it, foundInterfaces) }


        Map<String, List<String>> unmodifiableValues = new HashMap<>()
        foundInterfaces.each { unmodifiableValues.put(it.key, Collections.unmodifiableList(foundInterfaces.get(it.key)))}

        this.foundInterfaces = Collections.unmodifiableMap(unmodifiableValues)
    }

    /**
     * @return result of the search
     */
    Map<String, List<String>> getInterfaceToImplementationsMapping() {
        return this.foundInterfaces
    }

    // Adapted/Stolen from: https://stackoverflow.com/questions/2056221/recursively-list-files-in-java
    private void walk( File root , Map<String, List<String>> foundInterfaces) {
        File[] list = root.listFiles()

        if (list == null) return

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f, foundInterfaces )

            }
            else {
                // /usr/lib/jvm/java-8-openjdk/src.zip!/java/net/URLClassLoader.java:364
                if (f.name.endsWith(".class"))
                {
                    ClassReader cr = new ClassReader(new FileInputStream(f))
                    ClassInterfaceDetector cid = new ClassInterfaceDetector()
                    cr.accept(cid, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG )

                    Set<String> matchedInterfaces = new HashSet<>(cid.getInterfaces())
                    matchedInterfaces.retainAll(this.interfacesToScanFor)
                    matchedInterfaces.each {foundInterfaces.get(it).add(cid.getName())}
                }
            }
        }
    }

}
