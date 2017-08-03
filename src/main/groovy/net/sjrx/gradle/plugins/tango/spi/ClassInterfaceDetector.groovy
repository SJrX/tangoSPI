package net.sjrx.gradle.plugins.tango.spi

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class ClassInterfaceDetector extends ClassVisitor {

    private Set<String> interfaces = new HashSet<String>()

    private name = ""

    private visited = false

    ClassInterfaceDetector()
    {
        super(Opcodes.ASM4)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        if (visited) throw new IllegalStateException("This object has already been visited once, cannot visit again")

        // ASM uses the class format for types (i.e., java/lang/String, we need java.lang.String)
        // See also UrlClassLoader#findClass()
        def interfacesUsingJavaSyntax = Arrays.asList(interfaces).collect { it.replace('/','.')}

        this.interfaces = Collections.unmodifiableSet(new HashSet<String>(interfacesUsingJavaSyntax))
        this.name = name.replace("/", ".")

        visited = true
    }

    Set<String> getInterfaces()
    {
        this.verifyVisited()
        return this.interfaces
    }

    String getName()
    {
        this.verifyVisited()
        return this.name
    }

    private verifyVisited()
    {
        if (!visited) throw new IllegalStateException("This object has not been visited and as such is not ready to read")
    }

}
