package org.apache.mahout.cf.taste.vjianke;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-4-16
 * Time: 上午10:59
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
    public static Map<String, String> sortMapByValue(Map<String, String> oriMap) {
        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        if (oriMap != null && !oriMap.isEmpty()) {
            List<Map.Entry<String, String>> entryList =
                    new ArrayList<Map.Entry<String, String>>(oriMap.entrySet());
            Collections.sort(entryList,
                    new Comparator<Map.Entry<String, String>>() {
                        public int compare(Map.Entry<String, String> entry1,
                                           Map.Entry<String, String> entry2) {
                            int value1 = 0, value2 = 0;
                            try {
                                value1 = Integer.parseInt(entry1.getValue());
                                value2 = Integer.parseInt(entry2.getValue());
                            } catch (NumberFormatException e) {
                                value1 = 0;
                                value2 = 0;
                            }
                            return value2 - value1;
                        }
                    });
            Iterator<Map.Entry<String, String>> iter = entryList.iterator();
            Map.Entry<String, String> tmpEntry = null;
            while (iter.hasNext()) {
                tmpEntry = iter.next();
                sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
            }
        }
        return sortedMap;
    }

    public static Map<Integer, Integer> sortMapByValueInt(Map<Integer, Integer> oriMap) {
        Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
        if (oriMap != null && !oriMap.isEmpty()) {
            List<Map.Entry<Integer, Integer>> entryList =
                    new ArrayList<Map.Entry<Integer, Integer>>(oriMap.entrySet());
            Collections.sort(entryList,
                    new Comparator<Map.Entry<Integer, Integer>>() {
                        public int compare(Map.Entry<Integer, Integer> entry1,
                                           Map.Entry<Integer, Integer> entry2) {
                            int value1 = 0, value2 = 0;
                            try {
                                value1 = entry1.getValue();
                                value2 = entry2.getValue();
                            } catch (NumberFormatException e) {
                                value1 = 0;
                                value2 = 0;
                            }
                            return value2 - value1;
                        }
                    });
            Iterator<Map.Entry<Integer, Integer>> iter = entryList.iterator();
            Map.Entry<Integer, Integer> tmpEntry = null;
            while (iter.hasNext()) {
                tmpEntry = iter.next();
                sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
            }
        }
        return sortedMap;
    }

    public static String arrayToString(String[] a, String separator) {
        StringBuffer result = new StringBuffer();
        result.append("[");
        if (a.length > 0) {
            result.append(a[0]);
            for (int i=1; i<a.length; i++) {
                result.append(separator);
                result.append(a[i]);
            }
        }
        result.append("]");
        return result.toString();
    }
}
