package kr.co.shineware.ml.hmm.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import kr.co.shineware.ml.hmm.interfaces.FileAccessible;

public class Observation implements FileAccessible{
	private Map<String,Map<Integer,Double>> featureLabelScore;
	public Observation(){
		this.init();
	}
	private void init() {
		this.featureLabelScore = null;
		this.featureLabelScore = new HashMap<String, Map<Integer,Double>>();
	}
	public void put(String feature, int labelId, double observationScore) {
		Map<Integer,Double> labelIdScoreMap = this.featureLabelScore.get(feature);
		if(labelIdScoreMap == null){
			labelIdScoreMap = new HashMap<>();
		}
		labelIdScoreMap.put(labelId, observationScore);
		featureLabelScore.put(feature, labelIdScoreMap);
	}
	public Map<Integer, Double> get(String feature) {
		return featureLabelScore.get(feature);	
	}
	@Override
	public void save(String filename) {
		ObjectOutputStream dos;
		try {
			dos = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename))));
			dos.writeObject(featureLabelScore);
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	@SuppressWarnings("unchecked")
	@Override
	public void load(String filename) {
		ObjectInputStream dis;
		try {
			dis = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(filename))));
			featureLabelScore = (Map<String,Map<Integer,Double>>) dis.readObject();
			dis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	

}
