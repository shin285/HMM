package kr.co.shineware.ml.hmm.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kr.co.shineware.ml.hmm.constant.FILENAME;
import kr.co.shineware.ml.hmm.constant.SYMBOL;
import kr.co.shineware.ml.hmm.core.lattice.Lattice;
import kr.co.shineware.ml.hmm.model.Dictionary;
import kr.co.shineware.ml.hmm.model.Grammar;
import kr.co.shineware.ml.hmm.model.LabelTable;
import kr.co.shineware.ml.hmm.model.Observation;
import kr.co.shineware.ml.hmm.model.Transition;

public class HMM {
	private Grammar grammar;
	private Dictionary dic;
	private Observation observation;
	private Transition transition;
	private LabelTable table;
	private Lattice lattice;
	
	public HMM(){
		this.init();
	}
	
	private void init() {
		this.grammar = null;
		this.dic = null;
		this.observation = null;
		this.transition = null;
		this.table = null;
		
		this.grammar = new Grammar();
		this.dic = new Dictionary();
		this.observation = new Observation();
		this.transition = new Transition();
		this.table = new LabelTable();
	}

	public void train(String filename){
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			String curLabel = null, prevLabel = null;
			String curFeature = null;
			while((line = br.readLine()) != null){
				line = line.trim();
				if(line.length() == 0){
					if(prevLabel == null){
						continue;
					}else{
						this.grammar.append(prevLabel, SYMBOL.END);
						prevLabel = null;
						continue;
					}
				}
				
				String[] tokens = line.split("\t");
				curFeature = tokens[0];
				curLabel = tokens[1];
				if(prevLabel == null){
					prevLabel = SYMBOL.START;
				}
				this.grammar.append(prevLabel, curLabel);
				this.dic.append(curFeature, curLabel);
				
				prevLabel = curLabel;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void calObservation(Map<String, Integer> totalPrevLabelTf) {

		this.observation = new Observation();

		Set<Entry<String, Map<String,Integer>>> featureDicEntrySet = dic.getDictionary().entrySet();
		for (Entry<String, Map<String, Integer>> featureLabelTfEntry : featureDicEntrySet) {
			String feature = featureLabelTfEntry.getKey();
			Set<Entry<String,Integer>> labelTfSet = featureLabelTfEntry.getValue().entrySet();
			for (Entry<String, Integer> labelTf : labelTfSet) {
				int totalLabelTf = totalPrevLabelTf.get(labelTf.getKey());
				double observationScore = (double)labelTf.getValue()/totalLabelTf;
				observationScore = Math.log10(observationScore);
				this.observation.put(feature,this.table.getId(labelTf.getKey()),observationScore);
			}
		}
	}
	
	private void calTransition(Map<String, Integer> totalPrevLabelTf) {

		this.transition  = new Transition(this.table.size());

		Set<String> prevLabelSet = grammar.getGrammar().keySet();
		for (String prevLabel : prevLabelSet) {
			Map<String,Integer> curLabelMap = grammar.getGrammar().get(prevLabel);
			Set<String> curLabelSet = curLabelMap.keySet();
			for (String curLabel : curLabelSet) {
				int prev2CurTf = curLabelMap.get(curLabel);
				int prevTf = totalPrevLabelTf.get(prevLabel);
				double transitionScore = prev2CurTf/(double)prevTf;
				transitionScore = Math.log10(transitionScore);
				this.transition.put(this.table.getId(prevLabel),this.table.getId(curLabel),transitionScore);
			}
		}
	}
	private void buildLabelTable(Map<String, Integer> totalPrevLabelTf) {
		this.table = new LabelTable();
		Set<String> labelSet = totalPrevLabelTf.keySet();
		for (String label : labelSet) {
			this.table.put(label);
		}
		this.table.put(SYMBOL.END);		
	}

	private Map<String, Integer> getTotalPrevLabelCount() {
		Map<String,Integer> posCountMap = new HashMap<String, Integer>();
		Set<String> prevPosSet = grammar.getGrammar().keySet();
		for (String prevPos : prevPosSet) {
			Map<String,Integer> prev2curPosMap = grammar.getGrammar().get(prevPos);
			Set<String> curPosSet = prev2curPosMap.keySet();
			for (String curPos : curPosSet) {
				Integer tf = posCountMap.get(prevPos);
				if(tf == null){
					tf = 0;
				}
				tf += prev2curPosMap.get(curPos);
				posCountMap.put(prevPos, tf);
			}
		}
		return posCountMap;
	}

	public void save(String path){
		
		Map<String,Integer> totalPrevLabelTf = this.getTotalPrevLabelCount();

		//build POS table
		this.buildLabelTable(totalPrevLabelTf);

		//build transition
		this.calTransition(totalPrevLabelTf);

		//build observation
		this.calObservation(totalPrevLabelTf);		
		
		File rootPath = new File(path);
		if(!rootPath.isDirectory()){
			rootPath.mkdirs();
		}
//		this.grammar.save(path + File.separator + FILENAME.GRAMMAR);
//		this.dic.save(path + File.separator + FILENAME.DICTIONARY);
		this.observation.save(path+File.separator + FILENAME.OBSERVATION);
		this.transition.save(path+File.separator + FILENAME.TRANSITION);
		this.table.save(path+File.separator + FILENAME.LABEL_TABLE);
	}
	public List<String> labeling(List<String> featureList){
		if(featureList.size() == 0)return null;
		lattice = null;
		lattice = new Lattice(this.table);
		lattice.setTransition(this.transition);
		for(int i=0;i<featureList.size();i++){
			String feature = featureList.get(i);
			Map<Integer,Double> labelIdScoreMap = this.observation.get(feature);
			if(labelIdScoreMap == null){
				labelIdScoreMap = makeOOVDatas();				
			}
			this.insertLattice(i,i+1,feature,labelIdScoreMap);
		}
		return lattice.getMax(featureList.size());
	}

	private Map<Integer, Double> makeOOVDatas() {
		Map<Integer,Double> labelIdScoreMap = new HashMap<>();
		Set<Integer> labelIdSet = this.table.getIdLabelTable().keySet();
		for (Integer labelId : labelIdSet) {
			labelIdScoreMap.put(labelId, -10000.0);
		}
		return labelIdScoreMap;
	}

	private void insertLattice(int beginIdx, int endIdx, String feature, Map<Integer, Double> labelIdScoreMap) {
		Set<Entry<Integer,Double>> sentrySet = labelIdScoreMap.entrySet();
		for (Entry<Integer, Double> entry : sentrySet) {
			this.lattice.put(beginIdx, endIdx, feature, entry.getKey(), entry.getValue());
		}		
	}

	public void load(String path) {
		this.init();
		this.observation.load(path+File.separator + FILENAME.OBSERVATION);
		this.transition.load(path+File.separator + FILENAME.TRANSITION);
		this.table.load(path+File.separator + FILENAME.LABEL_TABLE);
	}
}
