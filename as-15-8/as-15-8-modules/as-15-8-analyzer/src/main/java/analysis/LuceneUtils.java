package analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

public class LuceneUtils {


    public static Query createPhraseQuery(Analyzer analyzer, String phrase) throws IOException {
        if(phrase.trim().isEmpty())
            return null;

        QueryParser parser = new QueryParser("text", analyzer);
        //return parser.createPhraseQuery("text",  screeninigQuery(phrase));
        return parser.createPhraseQuery("text",  QueryParserBase.escape(phrase));
    }

    private static String screeninigQuery(String query) {
        return query.replaceAll("(\\+|-|&&|\\|\\||!|\\(|\\)|\\{|\\}|\\[|\\]|\\^|\"|~|\\*|\\?|:|\\\\|\\/)", "\\\\$1");
    }

    public static RAMDirectory createIndex(Analyzer analyzer, String ...texts) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        //config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        RAMDirectory ramDirectory = new RAMDirectory();

        try(IndexWriter indexWriter = new IndexWriter(ramDirectory, config)) {
            Document doc = new Document();
            for(String text : texts)
                doc.add(new TextField("text", text, Field.Store.NO));
            indexWriter.addDocument(doc);
        }
        return ramDirectory;
    }

}
