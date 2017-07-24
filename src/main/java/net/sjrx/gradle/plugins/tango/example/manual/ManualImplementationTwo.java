package net.sjrx.gradle.plugins.tango.example.manual;

public class ManualImplementationTwo implements ManualSPIInterface {

    @Override
    public String helloWorld() {
        return "Bonjour";
    }
}
