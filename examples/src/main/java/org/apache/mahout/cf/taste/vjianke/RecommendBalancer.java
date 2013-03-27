package org.apache.mahout.cf.taste.vjianke;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-30
 * Time: 上午10:43
 * To change this template use File | Settings | File Templates.
 */
public class RecommendBalancer {
    public int MAXCOUNT = 20;
    public int recommendCount;
    public int subscriptionCount;

    public RecommendBalancer(int subscriptionCount){
        this.subscriptionCount = subscriptionCount;
        recommendCount = MAXCOUNT;
    }

    public void balance(String userId){

    }

    public BalanceResult balance(int remainSubscription, Datalayer.BoardRelated relatedBoard){
        BalanceResult result = new BalanceResult();
        if(subscriptionCount >= MAXCOUNT){
            result.howMany = 1;
        }
        if(subscriptionCount < MAXCOUNT){
            result.howMany = (int)(MAXCOUNT/(float)subscriptionCount);
        }

        return result;
    }

    public class BalanceResult{
        boolean isContinue;
        int howMany;
    }

    public int balance(String boardId, float influence, int maxCount){

        return (int) (influence*maxCount);
    }

    public void reset(){
       recommendCount = MAXCOUNT;
    }
}
