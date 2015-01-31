import junit.framework.TestCase;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by SanDomingo on 31/1/15.
 */
public class IndexingTest extends TestCase {
    protected String[] ids = {"1", "2"};
    protected String[] unindexed = {"Netherlands", "Italy"};
    protected String[] unsorted = {"Amsterdam has lots of bridges", "Venice has los of canals"};

    protected String[] text = {"Amsterdam", "Venice"};

    private Directory directory;
    private IndexWriter writer;

    @Before
    protected void setUp() throws IOException {
        directory = new RAMDirectory();

        IndexWriter writer = getWriter();
        for (int i = 0; i < ids.length; i++) {
            Document doc = new Document();
            doc.add(new Field("id", ids[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field("country", unindexed[i], Field.Store.YES, Field.Index.NO));
            doc.add(new Field("contents", unsorted[i], Field.Store.NO, Field.Index.ANALYZED));
            // [Program 3.2 Page 40, fixed by SanDomingo] city name should not be analyzed
            doc.add(new Field("city", text[i], Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        writer.close();
    }

    public IndexWriter getWriter() throws IOException {
        return new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_30, new WhitespaceAnalyzer()));
    }

    protected int getHitCount(String fieldName, String searchString) throws IOException {
        DirectoryReader open = IndexReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(open);
        Term t = new Term(fieldName, searchString);
        Query query = new TermQuery(t);
        int hitCount = TestUtil.hitCount(searcher, query);
        open.close();
        return hitCount;
    }

    @Test
    public void testIndexWriter() throws IOException {
        IndexWriter writer = getWriter();
        assertEquals(ids.length, writer.numDocs());
        writer.close();
    }

    @Test
    public void testIndexReader() throws IOException {
        IndexReader reader = IndexReader.open(directory);
        assertEquals(ids.length, reader.maxDoc());
        assertEquals(ids.length, reader.numDocs());
        reader.close();
    }

    @Test
    public void testDeleteBeforeOptimize() throws IOException {
        IndexWriter writer = getWriter();
        assertEquals(2, writer.numDocs());
        writer.deleteDocuments(new Term("id", "1"));
        writer.commit();
        assertEquals(2, writer.maxDoc()); // delete from index but may not be flushed.
        assertEquals(1, writer.numDocs());
        writer.close();
    }

    @Test
    public void testDeleteAfterOptimize() throws IOException {
        IndexWriter writer = getWriter();
        assertEquals(2, writer.numDocs());
        writer.deleteDocuments(new Term("id", "1"));
        writer.forceMergeDeletes();
        writer.commit();
        assertEquals(1, writer.maxDoc()); // force merge segments
        assertEquals(1, writer.numDocs());
        writer.close();
    }

    @Test
    public void testUpdate() throws IOException {
        assertEquals(1, getHitCount("city", "Amsterdam"));

        IndexWriter writer = getWriter();

        Document doc = new Document();
        doc.add(new Field("id", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("country", "Netherlands", Field.Store.YES, Field.Index.NO));
        doc.add(new Field("contents", "Den Haag has a lot of museums", Field.Store.NO, Field.Index.ANALYZED));
        // [Program 3.2 Page 40, fixed by SanDomingo] city name should not be analyzed
        doc.add(new Field("city", "Den Haag", Field.Store.YES, Field.Index.NOT_ANALYZED));

        writer.updateDocument(new Term("id", "1"), doc);
        writer.close();

        assertEquals(0, getHitCount("city", "Amsterdam"));
        // because city name is not analyzed, the result document can be hit by field value complete matching.
        assertEquals(1, getHitCount("city", "Den Haag"));
    }
}
