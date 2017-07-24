package net.sjrx.gradle.plugins.tango.example.auto;


public class AutoImplementationOne implements AutoSPIInterface {

    @Override
    public String helloWorld() {
        return "Hello";
    }
}
