package analysis;

import model.KeyWord;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.List;


public class KeyWordsCounter {

    private static Analyzer analyzer = new RussianAnalyzer();

    public static int getCount(String text, List<KeyWord> keyWords) throws IOException {
        int count = 0;
        if (keyWords == null || keyWords.size() == 0)
            return count;

        Directory directory = LuceneUtils.createIndex(analyzer, text);

        for (KeyWord keyWord : keyWords){
            Query query = LuceneUtils.createPhraseQuery(analyzer, keyWord.getWord());

            DirectoryReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            TopDocs topDocs = indexSearcher.search(query, 100);

            count += topDocs.totalHits.value;
        }
        return count;
    }
}
