package kr.co.shineware.ml.hmm.test;

import java.util.ArrayList;
import java.util.List;

import kr.co.shineware.ml.hmm.core.HMM;

public class HMMTester {
	public static void main(String[] args) {
		trainHMM();
		labelingHMM();
	}

	private static void labelingHMM() {
		HMM hmm = new HMM();
		hmm.load("autospacing_model");
		
		//for test data
		String str = "$이정도면만족합니다.$";
		List<String> featureList = new ArrayList<>();
		for(int i=0;i<str.length()-1;i++){
			featureList.add(str.substring(i, i+2));
		}
		
		//labeling
		List<String> resultList = hmm.labeling(featureList);
		
		//decoding
		StringBuffer sb = new StringBuffer();
		for(int i=1;i<resultList.size();i++){
			String result = resultList.get(i);
			sb.append(result.charAt(0));
			if(result.charAt(3) == '1'){
				sb.append(" ");
			}
		}
		System.out.println(sb.toString());
	}

	private static void trainHMM() {
		HMM hmm = new HMM();
		hmm.train("hmm.train");
		hmm.save("autospacing_model");		
	}
	
}
