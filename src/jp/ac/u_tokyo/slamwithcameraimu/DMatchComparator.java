package jp.ac.u_tokyo.slamwithcameraimu;

import java.util.Comparator;

import org.opencv.features2d.DMatch;

public class DMatchComparator implements Comparator<DMatch> {

	    //比較メソッド（データクラスを比較して-1, 0, 1を返すように記述する）
	    public int compare(DMatch a, DMatch b) {
	        float no1 = a.distance;
	        float no2 = b.distance;

	        //こうすると社員番号の昇順でソートされる
	        if (no1 > no2) {
	            return 1;

	        } else if (no1 == no2) {
	            return 0;

	        } else {
	            return -1;

	        }
	    }

	}
