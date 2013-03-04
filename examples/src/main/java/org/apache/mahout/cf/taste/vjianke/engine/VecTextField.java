package org.apache.mahout.cf.taste.vjianke.engine;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-3-4
 * Time: 下午1:12
 * To change this template use File | Settings | File Templates.
 */
public class VecTextField extends Field {

    /* Indexed, tokenized, not stored. */
    public static final FieldType TYPE_NOT_STORED = new FieldType();

    /* Indexed, tokenized, stored. */
    public static final FieldType TYPE_STORED = new FieldType();

    static {
        TYPE_NOT_STORED.setIndexed(true);
        TYPE_NOT_STORED.setTokenized(true);
        TYPE_NOT_STORED.setStoreTermVectors(true);
        TYPE_NOT_STORED.setStoreTermVectorPositions(true);
        TYPE_NOT_STORED.freeze();

        TYPE_STORED.setIndexed(true);
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setStoreTermVectors(true);
        TYPE_STORED.setStoreTermVectorPositions(true);
        TYPE_STORED.freeze();
    }

// TODO: add sugar for term vectors...?

    /** Creates a new TextField with String value. */
    public VecTextField(String name, String value, Store store) {
        super(name, value, store == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
    }

    /** Creates a new un-stored TextField with TokenStream value. */
    public VecTextField(String name, TokenStream stream) {
        super(name, stream, TYPE_NOT_STORED);
    }
}
