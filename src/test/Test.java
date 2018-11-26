package test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import android.R.integer;
import android.util.Log;

import com.game.chess.model.AIPosition;

public class Test {
	public static void main(String[] args) {
		int[] xy = new int[2];
		int postion = 105;
		xy[0] = postion / 14;
		xy[1] = postion % 14;
		System.out.println("x: "+xy[0] + ", y: " + xy[1]);

        int i = 5;
        float level = i;
        	level += 0.1;
        System.out.println(level);
	}

	/**
	 * 将XY坐标转化为position
	 */
	public int xy2Position(int[] xy) {
		int position = xy[0] * 14 + xy[1];
		return position;
	}

}
