import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by SanDomingo on 30/1/15.
 */
public class SimpleTest {
    @Test
    public void testClassName() {
        System.out.println(SimpleTest.class);
        System.out.println(SimpleTest.class.getName());
    }

    @Test
    public void testFilePath() throws IOException {
        File file = new File("pom.xml");
        System.out.println("File name: " + file.getName());
        System.out.println("File canonical path: " + file.getCanonicalPath());
        System.out.println("File absolute path: " + file.getAbsolutePath());
    }
}
