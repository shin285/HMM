package kr.co.shineware.ml.hmm.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kr.co.shineware.ml.hmm.interfaces.FileAccessible;

public class Grammar implements FileAccessible{

	private Map<String,Map<String,Integer>> grammar;
	
	public Grammar() {
		this.grammar = new HashMap<String, Map<String,Integer>>();
	}
	public Grammar(String filename){
		this.load(filename);
	}
	
	@Override
	public void save(String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			Set<Entry<String, Map<String,Integer>>> entrySet = grammar.entrySet();
			for (Entry<String, Map<String, Integer>> entry : entrySet) {

				bw.write(entry.getKey());
				bw.write("\t");

				Set<String> nextMorphSet = entry.getValue().keySet();
				int morphSize = nextMorphSet.size();
				int count = 0;
				for (String nextMorph : nextMorphSet) {
					bw.write(nextMorph);
					bw.write(":");
					Integer tf = entry.getValue().get(nextMorph);
					bw.write(""+tf);
					count++;
					if(morphSize != count){
						bw.write(",");
					}
				}
				bw.newLine();				
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void load(String filename) {
		this.grammar = null;
		this.grammar = new HashMap<String, Map<String,Integer>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = br.readLine()) != null){
				String[] lineSplitedList = line.split("\t");

				//previous POS
				String prevPos = lineSplitedList[0];

				//next POS parsing
				Map<String, Integer> nextPosMap = new HashMap<String,Integer>();
				String nextPosChunks = lineSplitedList[1];
				String[] nextPosChunkList = nextPosChunks.split(",");
				int posChunkListSize = nextPosChunkList.length;
				String commaPos = "";
				for(int j=0;j<posChunkListSize;j++){
					String nextPosTfPair = nextPosChunkList[j];
					if(nextPosTfPair.length() == 0){
						commaPos += ",";
						continue;
					}
					int separatorIdx = nextPosTfPair.lastIndexOf(':');
					String nextPos;
					if(separatorIdx == 0){
						nextPos = "";
					}else{
						nextPos = nextPosTfPair.substring(0, separatorIdx);
					}
					Integer tf = Integer.parseInt(nextPosTfPair.substring(separatorIdx+1));
					nextPosMap.put(commaPos+nextPos,tf);
					commaPos = "";
				}
				
				//load to memory
				this.grammar.put(prevPos, nextPosMap);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void append(String prevPos,String curPos){
		Map<String,Integer> nextPosTfMap = this.grammar.get(prevPos);
		if(nextPosTfMap == null){
			nextPosTfMap = new HashMap<String, Integer>();
		}
		Integer tf = nextPosTfMap.get(curPos);
		if(tf == null){
			tf = 0;
		}
		tf++;
		nextPosTfMap.put(curPos, tf);
		this.grammar.put(prevPos, nextPosTfMap);
	}
	public Map<String, Map<String, Integer>> getGrammar(){
		return grammar;
	}

}
