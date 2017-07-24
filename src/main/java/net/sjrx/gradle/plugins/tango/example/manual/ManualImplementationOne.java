package net.sjrx.gradle.plugins.tango.example.manual;


public class ManualImplementationOne implements ManualSPIInterface {

    @Override
    public String helloWorld() {
        return "Hello";
    }
}
