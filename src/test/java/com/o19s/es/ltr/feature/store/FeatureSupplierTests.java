package com.o19s.es.ltr.feature.store;

import com.o19s.es.ltr.feature.FeatureSet;
import com.o19s.es.ltr.ranker.DenseFeatureVector;
import com.o19s.es.ltr.ranker.LtrRanker;
import com.o19s.es.ltr.utils.Suppliers;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;

public class FeatureSupplierTests extends LuceneTestCase {
    public static FeatureSet getFeatureSet() {
        String matchQuery = QueryBuilders.matchQuery("test", "{{query_string}}").toString();
        StoredFeature feature = new StoredFeature("test", singletonList("query_string"), "mustache", matchQuery);
        Map<String, Object> params = new HashMap<>();
        params.put("query_string", "a query");
        return new StoredFeatureSet("my_feature_set", Arrays.asList(feature));
    }

    public void testGetWhenFeatureVectorNotSet() {
        FeatureSupplier featureSupplier = new FeatureSupplier(getFeatureSet());
        expectThrows(NullPointerException.class, () -> featureSupplier.get()).getMessage();
    }

    public void testGetWhenFeatureVectorSet() {
        FeatureSupplier featureSupplier = new FeatureSupplier(getFeatureSet());
        Suppliers.MutableSupplier<LtrRanker.FeatureVector> vectorSupplier = new Suppliers.MutableSupplier<>();
        LtrRanker.FeatureVector featureVector = new DenseFeatureVector(1);
        vectorSupplier.set(featureVector);
        featureSupplier.set(vectorSupplier);
        assertEquals(featureVector, featureSupplier.get());
    }

    public void testContainsKey() {
        FeatureSupplier featureSupplier = new FeatureSupplier(getFeatureSet());
        assertTrue(featureSupplier.containsKey("test"));
        assertFalse(featureSupplier.containsKey("bad_test"));
    }

    public void testGetFeatureScore() {
        FeatureSupplier featureSupplier = new FeatureSupplier(getFeatureSet());
        Suppliers.MutableSupplier<LtrRanker.FeatureVector> vectorSupplier = new Suppliers.MutableSupplier<>();
        LtrRanker.FeatureVector featureVector = new DenseFeatureVector(1);
        featureVector.setFeatureScore(0, 10.0f);
        vectorSupplier.set(featureVector);
        featureSupplier.set(vectorSupplier);
        assertEquals(10.0f, featureSupplier.get("test"), 0.0f);
        assertNull(featureSupplier.get("bad_test"));
    }

    public void testEntrySetWhenFeatureVectorNotSet(){
        FeatureSupplier featureSupplier = new FeatureSupplier(getFeatureSet());
        Set<Map.Entry<String, Double>> entrySet = featureSupplier.entrySet();
        assertTrue(entrySet.isEmpty());
        Iterator<Map.Entry<String, Double>> iterator = entrySet.iterator();
        assertFalse(iterator.hasNext());
        assertNull(iterator.next());
        assertEquals(0, entrySet.size());
    }

    public void testEntrySetWhenFeatureVectorIsSet(){
        FeatureSupplier featureSupplier = new FeatureSupplier(getFeatureSet());
        Suppliers.MutableSupplier<LtrRanker.FeatureVector> vectorSupplier = new Suppliers.MutableSupplier<>();
        LtrRanker.FeatureVector featureVector = new DenseFeatureVector(1);
        featureVector.setFeatureScore(0, 10.0f);
        vectorSupplier.set(featureVector);
        featureSupplier.set(vectorSupplier);

        Set<Map.Entry<String, Double>> entrySet = featureSupplier.entrySet();
        assertFalse(entrySet.isEmpty());
        Iterator<Map.Entry<String, Double>> iterator = entrySet.iterator();
        assertTrue(iterator.hasNext());
        Map.Entry<String, Double> item = iterator.next();
        assertEquals("test",item.getKey());
        assertEquals(10.0f,item.getValue(), 0.0f);
        assertEquals(1, entrySet.size());
    }

}

