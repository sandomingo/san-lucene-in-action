import junit.framework.TestCase;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

/**
 * Created by SanDomingo on 31/1/15.
 */
public class LockTest extends TestCase {
    private Directory dir;

    protected void setUp() throws IOException {
        String indexDir = System.getProperty("java.io.tmpdir", "tmp") +
                System.getProperty("file.separator") + "index";
        dir = FSDirectory.open(new File(indexDir));
    }

    public void testWriteLock() throws IOException {
        IndexWriter writer1 = new IndexWriter(dir, new IndexWriterConfig(Version.LUCENE_30, new SimpleAnalyzer()));
        IndexWriter writer2 = null;
        try {
            writer2 = new IndexWriter(dir, new IndexWriterConfig(Version.LUCENE_30, new SimpleAnalyzer()));
            fail("We should never reach this point");
        } catch (LockObtainFailedException e) {
            e.printStackTrace();
        } finally {
            writer1.close();
            assertNull(writer2);
        }
    }
}
