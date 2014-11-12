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

public class Dictionary implements FileAccessible{

	private Map<String,Map<String,Integer>> dic;
	
	public Dictionary() {
		this.dic = new HashMap<String, Map<String,Integer>>();
	}
	public Dictionary(String filename) {
		this.load(filename);
	}	
	
	@Override
	public void save(String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			Set<Entry<String, Map<String,Integer>>> entrySet = dic.entrySet();
			for (Entry<String, Map<String, Integer>> entry : entrySet) {

				bw.write(entry.getKey());
				bw.write("\t");

				Set<String> posSet = entry.getValue().keySet();
				int posSize = posSet.size();
				int count = 0;
				for (String pos : posSet) {
					bw.write(pos);
					bw.write(":");
					Integer tf = entry.getValue().get(pos);
					bw.write(""+tf);
					count++;
					if(count != posSize)
						bw.write("\t");
				}
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void load(String filename){
		this.dic = null;
		this.dic = new HashMap<String, Map<String,Integer>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();
				String[] tokens = line.split("\t");				
				//단어
				String word = tokens[0];
				
				//품사 및 빈도 정보
				Map<String, Integer> posTfMap = new HashMap<String,Integer>();
				for(int i=1;i<tokens.length;i++){
					String token = tokens[i];
					int separatorIdx = token.lastIndexOf(':');
					String pos = token.substring(0, separatorIdx);
					int tf = Integer.parseInt(token.substring(separatorIdx+1));
					posTfMap.put(pos, tf);
				}
				dic.put(word, posTfMap);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void append(String word,String pos){
		Map<String,Integer> posTfMap = dic.get(word);
		if(posTfMap == null){
			posTfMap = new HashMap<String, Integer>();
		}
		Integer tf = posTfMap.get(pos);
		if(tf == null){
			tf = 0;
		}
		tf++;
		posTfMap.put(pos, tf);
		dic.put(word, posTfMap);
	}
	
	public Map<String, Map<String, Integer>> getDictionary(){
		return dic;
	}
}
