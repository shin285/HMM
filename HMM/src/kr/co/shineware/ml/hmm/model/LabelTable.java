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
public class LabelTable implements FileAccessible{

	//key = label
	//value = id
	private Map<String,Integer> labelIdTable;

	//key = id
	//value = label
	private Map<Integer,String> idLabelTable;

	public LabelTable(){
		this.init();
	}
	
	public Map<String, Integer> getLabelIdTable(){
		return this.labelIdTable;
	}
	
	public Map<Integer, String> getIdLabelTable(){
		return this.idLabelTable;
	}
	

	private void init() {
		this.labelIdTable = null;
		this.idLabelTable = null;
		this.labelIdTable = new HashMap<String, Integer>();
		this.idLabelTable = new HashMap<Integer,String>();
	}

	public void put(String label) {
		Integer id = labelIdTable.get(label);
		if(id == null){
			labelIdTable.put(label, labelIdTable.size());
			idLabelTable.put(idLabelTable.size(), label);
		}
	}

	public int getId(String label){
		return labelIdTable.get(label);
	}

	public String getLabel(int id){
		return idLabelTable.get(id);
	}

	public int size(){
		return labelIdTable.size();
	}

	@Override
	public void save(String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			Set<Entry<String,Integer>> posIdEntrySet = labelIdTable.entrySet();
			for (Entry<String, Integer> entry : posIdEntrySet) {
				bw.write(entry.getKey()+"\t"+entry.getValue());
				bw.newLine();
			}
			bw.close();
			bw = null;
			posIdEntrySet = null;
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void load(String filename) {
		try{
			this.init();
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = br.readLine()) != null){
				String[] tokens = line.split("\t");
				this.labelIdTable.put(tokens[0], Integer.parseInt(tokens[1]));
				this.idLabelTable.put(Integer.parseInt(tokens[1]),tokens[0]);
			}
			br.close();
			br = null;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}