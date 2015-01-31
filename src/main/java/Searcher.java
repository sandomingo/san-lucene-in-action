import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

/**
 * Created by SanDomingo on 30/1/15.
 */
public class Searcher {
    public static void main(String[] argss) throws IOException, ParseException {
        String[] args = new String[]{
                "/Users/Jing/san's workspace/Project/san-lucene/indexDir/javaAPIIndex",
                "get"
        };
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java " + Searcher.class.getName() + " <index dir> <query>");
        }
        String indexDir = args[0];
        String query = args[1];

        search(indexDir, query);
    }

    private static void search(String indexDir, String q) throws IOException, ParseException {
        Directory dir = FSDirectory.open(new File(indexDir));
        DirectoryReader directoryReader = IndexReader.open(dir);
        IndexSearcher is = new IndexSearcher(directoryReader);

        QueryParser parser = new QueryParser(Version.LUCENE_30, "contents", new StandardAnalyzer(Version.LUCENE_30));
        Query query = parser.parse(q);
        long start = System.currentTimeMillis();
        TopDocs hits = is.search(query, 10);
        long end = System.currentTimeMillis();

        System.out.println("Found " + hits.totalHits + " document(s) (in " + (end - start) +
                " milliseconds) that matched query'" + q + "':");

        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(doc.get("fullpath"));
        }
        directoryReader.close();
    }
}
