import net.sjrx.gradle.plugins.tango.example.auto.AutoSPIInterface;
import net.sjrx.gradle.plugins.tango.example.manual.ManualSPIInterface;
import org.junit.Assert;
import org.junit.Test;

import java.util.ServiceLoader;

public class TestAssert {

    @Test
    public void testAssertTrue()
    {

        System.out.println("CLASS PATH");
        for(String s : System.getProperty("java.class.path").split(":"))
        {
            System.out.println("\t" + s);
        }

        System.out.println("\n\n\n");

        ServiceLoader<ManualSPIInterface> sl = ServiceLoader.load(ManualSPIInterface.class);

        int i = 0;
        for(ManualSPIInterface o : sl)
        {
            System.out.println("\t> Found implementation manually: " + o.getClass());
            i++;
        }

        Assert.assertEquals("Manual implementations found should be 1",1, i);


        ServiceLoader<AutoSPIInterface> sl2 = ServiceLoader.load(AutoSPIInterface.class);

        i = 0;
        for(AutoSPIInterface o : sl2)
        {
            System.out.println("\t>Found implementation automatically: " + o.getClass());
            i++;
        }

        Assert.assertEquals("Auto implementations found should be 2", 2, i);
    }
}
