package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-3-15
 * Time: 上午11:39
 * To change this template use File | Settings | File Templates.
 */
public class IntrestBasedRecommendEvaluatorRunner {

    public static double evaluate(DataModel model) throws IOException, TasteException {

        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();

        RecommenderBuilder builder = new RecommenderBuilder() {
            @Override
            public Recommender buildRecommender(DataModel model)
                    throws TasteException {
                UserSimilarity similarity =
                        new LogLikelihoodSimilarity(model);
                UserNeighborhood neighborhood =
                        new NearestNUserNeighborhood(2, similarity, model);
                IntrestBasedRecommend recommend = new IntrestBasedRecommend(model, neighborhood, similarity);
                return recommend;
            }
        };

        double evaluation = evaluator.evaluate(builder,null,model,0.7,1);
        return evaluation;
    }
}
