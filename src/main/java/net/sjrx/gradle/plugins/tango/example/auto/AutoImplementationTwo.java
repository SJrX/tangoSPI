package net.sjrx.gradle.plugins.tango.example.auto;

public class AutoImplementationTwo implements AutoSPIInterface {

    @Override
    public String helloWorld() {
        return "Bonjour";
    }
}
