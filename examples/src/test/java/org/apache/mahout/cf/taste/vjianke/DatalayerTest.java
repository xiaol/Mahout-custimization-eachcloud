package org.apache.mahout.cf.taste.vjianke;

import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-4-15
 * Time: 下午12:57
 * To change this template use File | Settings | File Templates.
 */
public class DatalayerTest extends TestCase {
    public void testGetActiveUsers() throws Exception {
        Datalayer layer = new Datalayer();
        layer.getActiveUsers(1);

    }
}
