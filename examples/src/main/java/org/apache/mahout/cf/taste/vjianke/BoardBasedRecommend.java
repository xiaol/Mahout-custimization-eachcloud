package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.MostSimilarItemsCandidateItemsStrategy;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-29
 * Time: 下午1:04
 * To change this template use File | Settings | File Templates.
 */
public class BoardBasedRecommend extends GenericItemBasedRecommender{
    public BoardBasedRecommend(DataModel dataModel, ItemSimilarity similarity, CandidateItemsStrategy candidateItemsStrategy, MostSimilarItemsCandidateItemsStrategy mostSimilarItemsCandidateItemsStrategy) {
        super(dataModel, similarity, candidateItemsStrategy, mostSimilarItemsCandidateItemsStrategy);
    }

    public BoardBasedRecommend(DataModel dataModel, ItemSimilarity similarity) {
        super(dataModel, similarity);
    }
}
