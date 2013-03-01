package org.apache.mahout.cf.taste.vjianke;

import com.google.common.base.Preconditions;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.model.BooleanPreference;
import org.apache.mahout.cf.taste.impl.model.BooleanUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.EstimatedPreferenceCapper;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansClusterer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.sql.*;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-26
 * Time: 下午12:35
 * To change this template use File | Settings | File Templates.
 */
public class IntrestBasedRecommend extends GenericUserBasedRecommender {
    private final String _connectionString =
            "jdbc:sqlserver://llwko2tjlq.database.windows.net" + ";" +
                    "database=demo1" + ";" +
                    "user=eachcloud@llwko2tjlq" + ";" +
                    "password=IONisgreat!";

    private static final Logger log = LoggerFactory.getLogger(GenericUserBasedRecommender.class);

    public IntrestBasedRecommend(DataModel dataModel, UserNeighborhood neighborhood, UserSimilarity similarity) {
        super(dataModel, neighborhood, similarity);
    }


    public List<RecommendedItem> recommend(String strUuid,ArrayList<UUID> users,
                                           int howMany, List<Long> nearestNUsers) throws TasteException {


        int userIndex = users.indexOf(UUID.fromString(strUuid/*"07221718-b190-4536-8191-a0410029de34")*/));

        long[] theNeighborhood = neighborhood.getUserNeighborhood(userIndex);

        if (theNeighborhood.length == 0) {
            return Collections.emptyList();
        }

        FastIDSet allItemIDs = getAllOtherItems(theNeighborhood, userIndex);

        TopItems.Estimator<Long> estimator = new Estimator(userIndex, theNeighborhood);

        List<RecommendedItem> topItems = TopItems
                .getTopItems(howMany, allItemIDs.iterator(), null, estimator);


        for(long entity: neighborhood.getUserNeighborhood(userIndex)){
            nearestNUsers.add(entity);
        }

        //System.out.println("user: " +users.get(userIndex));
        for(RecommendedItem item : topItems ){
            //System.out.println("http://vjianke.com/"+Long.toString(item.getItemID(),36).toUpperCase()+".clip");
        }

        /*System.out.println("user: " +users.get(userIndex));
        for(long item : nearestNUsers ){
            System.out.println(users.get((int)item));
        }*/
        return topItems;
    }

    private final class Estimator implements TopItems.Estimator<Long> {

        private final long theUserID;
        private final long[] theNeighborhood;

        Estimator(long theUserID, long[] theNeighborhood) {
            this.theUserID = theUserID;
            this.theNeighborhood = theNeighborhood;
        }

        @Override
        public double estimate(Long itemID) throws TasteException {
            return doEstimatePreference(theUserID, theNeighborhood, itemID);
        }
    }

}
