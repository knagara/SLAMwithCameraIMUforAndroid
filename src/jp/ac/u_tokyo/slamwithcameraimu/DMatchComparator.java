package jp.ac.u_tokyo.slamwithcameraimu;

import java.util.Comparator;

import org.opencv.features2d.DMatch;

public class DMatchComparator implements Comparator<DMatch> {

	    public int compare(DMatch a, DMatch b) {
	        float no1 = a.distance;
	        float no2 = b.distance;

	        if (no1 > no2) {
	            return 1;

	        } else if (no1 == no2) {
	            return 0;

	        } else {
	            return -1;

	        }
	    }

	}
