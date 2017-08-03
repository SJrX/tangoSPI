package net.sjrx.gradle.plugins.tango.spi

import org.objectweb.asm.ClassReader

/**
 * Created by sjr on 7/23/17.
 */
class FullBuildDirectoryScanner {


    private final Set<String> interfacesToScanFor

    private final Map<String, List<String>> foundInterfaces

    FullBuildDirectoryScanner(Set<String> interfacesToScanFor, Set<File> directoriesToScan)
    {
        this.interfacesToScanFor = interfacesToScanFor

        Map<String, List<String>> foundInterfaces = new HashMap<>()
        interfacesToScanFor.each { foundInterfaces.put(it, new ArrayList<String>()) }

        directoriesToScan.each { walk(it, foundInterfaces) }


        Map<String, List<String>> unmodifiableValues = new HashMap<>()
        foundInterfaces.each { unmodifiableValues.put(it.key, Collections.unmodifiableList(foundInterfaces.get(it.key)))}

        this.foundInterfaces = Collections.unmodifiableMap(unmodifiableValues)
    }


    // Adapted/Stolen from: https://stackoverflow.com/questions/2056221/recursively-list-files-in-java
    void walk( File root , Map<String, List<String>> foundInterfaces) {
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

    Map<String, List<String>> getInterfaceToImplementationsMapping() {
        return this.foundInterfaces
    }


}
