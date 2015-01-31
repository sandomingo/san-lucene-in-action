import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * Created by SanDomingo on 31/1/15.
 */
public class TestUtil {
    public static int hitCount(IndexSearcher searcher, Query query) throws IOException {
        return searcher.search(query, 1).totalHits;
    }
}
