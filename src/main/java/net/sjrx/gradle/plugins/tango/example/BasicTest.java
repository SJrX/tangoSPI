package net.sjrx.gradle.plugins.tango.example;

import net.sjrx.gradle.plugins.tango.example.auto.AutoSPIInterface;
import net.sjrx.gradle.plugins.tango.example.manual.ManualSPIInterface;


import java.util.ServiceLoader;

/**
 * Created by sjr on 7/23/17.
 */
public class BasicTest {

    public static void main(String[] args) {
        ServiceLoader<ManualSPIInterface> sl = ServiceLoader.load(ManualSPIInterface.class);

        int i = 0;
        for(ManualSPIInterface o : sl)
        {
            System.out.println("\t> Found implementation manually: " + o.getClass());
            i++;
        }

        System.out.println("Total manually implementations found: " + i);


        ServiceLoader<AutoSPIInterface> sl2 = ServiceLoader.load(AutoSPIInterface.class);

        i = 0;
        for(AutoSPIInterface o : sl2)
        {
            System.out.println("\t>Found implementation automatically: " + o.getClass());
            i++;
        }

        System.out.println("Total automatic implementations found: " + i);
    }
}
