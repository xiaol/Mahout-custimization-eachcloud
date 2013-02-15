package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-2-15
 * Time: 下午5:12
 * To change this template use File | Settings | File Templates.
 */
public class CustomizeDataModel implements DataModel{
    @Override
    public LongPrimitiveIterator getUserIDs() throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PreferenceArray getPreferencesFromUser(long userID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LongPrimitiveIterator getItemIDs() throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PreferenceArray getPreferencesForItem(long itemID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Float getPreferenceValue(long userID, long itemID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Long getPreferenceTime(long userID, long itemID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumItems() throws TasteException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumUsers() throws TasteException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumUsersWithPreferenceFor(long itemID) throws TasteException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumUsersWithPreferenceFor(long itemID1, long itemID2) throws TasteException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPreference(long userID, long itemID, float value) throws TasteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removePreference(long userID, long itemID) throws TasteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPreferenceValues() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getMaxPreference() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getMinPreference() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void refresh(Collection<Refreshable> alreadyRefreshed) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
