package jgibblda;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-4-13
 * Time: 上午11:43
 * To change this template use File | Settings | File Templates.
 */
public class DumpData {
    public static void main(String args[]){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                    "dataset.dat"), true));

            LDADataset.writeDataSet(bw,null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> loadData(){
        return null;
    }

}
