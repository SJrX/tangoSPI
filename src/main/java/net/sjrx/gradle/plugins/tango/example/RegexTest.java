package net.sjrx.gradle.plugins.tango.example;

import java.util.regex.Pattern;

public class RegexTest {

    public static void main(String[] args) {
        String output = "countOfImplementationsForInterface(result, JAVA_SPI_INTERFACE_NAME, 1)\n" +
                "|                                  |       |\n" +
                "|                                  |       fixture.example.SimpleService\n" +
                "|                                  org.gradle.testkit.runner.internal.FeatureCheckBuildResult@33f7a5aa\n" +
                "java.lang.IllegalStateException: Could not find expected string in output: :compileJava\n" +
                ":generateProviderConfigurationFileJava\n" +
                ":processResources UP-TO-DATE\n" +
                ":classes\n" +
                ":runPicked up _JAVA_OPTIONS: -Dawt.useSystemAAFontSettings=on\n" +
                " \n" +
                "SPI Implementation: fixture.example.SimpleService ==> class fixture.example.impl.SomeInterfaceImpl1\n" +
                "SPI Count: fixture.example.SimpleService ==> 1\n" +
                "\n"
                +"BUILD SUCCESSFUL\n"
                +"\n";


        String interfaceName = "fixture.example.SimpleService";
        Pattern regex = Pattern.compile("SPI Count: " + Pattern.quote(interfaceName) + " ==> (?<count>[0-9]+)");

        if(regex.matcher(output).find())
        {
            System.out.println("MATCH");
        } else
        {
            System.out.println("NOMATCH \n*******************\n" + output + "\n*****************\n with `" + regex.pattern() + "`");
        }



    }
}
