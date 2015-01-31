import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by SanDomingo on 30/1/15.
 */
public class Indexer {
    private IndexWriter writer;

    public Indexer(String indexDir) throws IOException{
        Directory dir = FSDirectory.open(new File(indexDir));
        writer = new IndexWriter(dir, new IndexWriterConfig(Version.LUCENE_30, new StandardAnalyzer(Version.LUCENE_30)));
    }

    public static void main(String[] argss) throws Exception {
        String[] args = new String[]{
                "/Users/Jing/san's workspace/Project/san-lucene-in-action /indexDir/javaAPIIndex",
//                "/Users/Jing/san's workspace/SrcTrunk/elasticsearch/docs/java-api"
                "/Users/Jing/san's workspace/SrcTrunk/elasticsearch/docs/community"
                };
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java " + Indexer.class.getName() + " <index dir> <data dir>");
        }
        String indexDir = args[0];
        String dataDir = args[1];

        long start = System.currentTimeMillis();
        Indexer indexer = new Indexer(indexDir);
        int numIndexed;
        try {
            numIndexed = indexer.index(dataDir, new TextFilesFilter());
        } finally {
            indexer.close();
        }
        long end = System.currentTimeMillis();

        System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
    }

    private int index(String dataDir, FileFilter filter) throws Exception {
        File[] files = new File(dataDir).listFiles();

        for (File file : files) {
            if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() &&
                    (filter == null || filter.accept(file))) {
                indexFile(file);
            }
        }
        return writer.numDocs();
    }

    private void indexFile(File file) throws Exception {
        System.out.println("Indexing " + file.getCanonicalPath());
        Document doc = getDocument(file);
        writer.addDocument(doc);
    }

    private Document getDocument(File file) throws Exception {
        Document doc = new Document();
        doc.add(new Field("contents", new FileReader(file)));
        doc.add(new Field("filename", file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("fullpath", file.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }

    private void close() throws IOException {
        writer.close();
    }

    private static class TextFilesFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().endsWith(".asciidoc");
        }
    }
}
