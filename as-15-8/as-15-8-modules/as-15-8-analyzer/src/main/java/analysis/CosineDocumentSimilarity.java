package analysis;

import org.apache.commons.math3.linear.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.*;

import java.io.IOException;
import java.util.*;

public class CosineDocumentSimilarity {

    public static final String CONTENT = "Content";

    private final Map<String, Integer> f1;
    private final Map<String, Integer> f2;

    private final Set<String> terms = new HashSet<>();
    private final RealVector v1;
    private final RealVector v2;

    private boolean is100 = false;
    private boolean is0 = false;

    public CosineDocumentSimilarity(String s1, String s2) throws IOException {


        Directory directory = createIndex(s1, s2);
        IndexReader reader = DirectoryReader.open(directory);

        f1 = getTermFrequencies(reader, 0);
        f2 = getTermFrequencies(reader, 1);
        reader.close();

        v1 = toRealVector(f1);
        v2 = toRealVector(f2);
    }

    Directory createIndex(String s1, String s2) throws IOException {
        Directory directory = new RAMDirectory();
        Analyzer analyzer = new SimpleAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, iwc);
        addDocument(writer, s1);
        addDocument(writer, s2);
        writer.close();
        return directory;
    }

    void addDocument(IndexWriter writer, String content) throws IOException {
        Document doc = new Document();

        FieldType typeStored = new FieldType();
        typeStored.setIndexOptions(IndexOptions.DOCS);
        typeStored.setTokenized(true);
        typeStored.setStored(true);
        typeStored.setStoreTermVectors(true);
        typeStored.setStoreTermVectorPositions(true);
        typeStored.freeze();

        Field field = new Field(CONTENT, content, typeStored);
        doc.add(field);
        writer.addDocument(doc);
    }

    /** схожесть текстов: косинус векторов */
    public double getCosineSimilarity() {
        Double res = (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
        return res.isNaN() ? 0.0 : res;
    }

    /** схожесть текстов: простое сравнение по частотам слов */
    public double getCosineSimilaritySimple() {
        return getSimpleSimilarity(f1, f2) * getSimpleSimilarity(f2, f1);
    }

    /** схожесть текстов: средее арифметическое между двумя вариантами */
    public double getCosineSimilarityAvg() {
        return (getCosineSimilarity() + getCosineSimilaritySimple())/2.0;
    }

    double getSimpleSimilarity(Map<String, Integer> map1, Map<String, Integer> map2){
        double sumOrig = map1.size();
        double sum = 0.0;

        for(String key : map1.keySet()){
            Integer value1 = map1.get(key);
            value1 = value1 == null ? 0 : value1;

            Integer value2 = map2.get(key);
            value2 = value2 == null ? 0 : value2;

            double value = (value1 == 0 ? 0.0 : Double.valueOf(value2) / Double.valueOf(value1));
            value = value > 1.0 ? 1.0 : value;

            sum += value;
        }
        double res = sumOrig == 0 ? 0 : sum/sumOrig;
        return res;
    }

    Map<String, Integer> getTermFrequencies(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, CONTENT);
        if (vector == null)
            return new HashMap<>();
        TermsEnum termsEnum = vector.iterator();
        Map<String, Integer> frequencies = new HashMap<>();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            int freq = (int) termsEnum.totalTermFreq();
            frequencies.put(term, freq);
            terms.add(term);
        }
        return frequencies;
    }

    RealVector toRealVector(Map<String, Integer> map) {
        RealVector vector = new ArrayRealVector(terms.size());
        int i = 0;
        for (String term : terms) {
            int value = map.containsKey(term) ? map.get(term) : 0;
            vector.setEntry(i++, value);
        }
        return (RealVector) vector.mapDivide(vector.getL1Norm());
    }
}
