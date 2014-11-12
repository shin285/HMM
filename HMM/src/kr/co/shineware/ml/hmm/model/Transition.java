package kr.co.shineware.ml.hmm.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import kr.co.shineware.ml.hmm.interfaces.FileAccessible;

public class Transition implements FileAccessible{
	private double[][] scoreMatrix;

	public Transition(){
		;
	}
	
	public Transition(int size) {
		scoreMatrix = new double[size][size];
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				scoreMatrix[i][j] = Double.NEGATIVE_INFINITY;
			}
		}
	}

	public void put(int prevId, int curId, double transitionScore) {
		scoreMatrix[prevId][curId] = transitionScore;
	}
	public Double get(int prevId, int curId){
		if(scoreMatrix[prevId][curId] == Double.NEGATIVE_INFINITY){
			return null;
		}else{
			return scoreMatrix[prevId][curId];
		}
	}

	@Override
	public void save(String filename) {
		ObjectOutputStream dos;
		try {
			dos = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename))));
			dos.writeObject(scoreMatrix);
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	@Override
	public void load(String filename) {
		ObjectInputStream dis;
		try {
			dis = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(filename))));
			scoreMatrix = (double[][]) dis.readObject();
			dis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
}
