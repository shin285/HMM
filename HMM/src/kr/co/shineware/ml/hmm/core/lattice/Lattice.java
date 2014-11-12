package kr.co.shineware.ml.hmm.core.lattice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kr.co.shineware.ml.hmm.constant.SYMBOL;
import kr.co.shineware.ml.hmm.model.LabelTable;
import kr.co.shineware.ml.hmm.model.Transition;

public class Lattice {
	//key = endIdx
	//value.key = prev lattice node's hashcode
	//value.value = prev lattice node that matched hashcode
	private Map<Integer,Map<Integer,LatticeNode>> lattice;
	private Transition transition = null;
	private LabelTable table;

	public Lattice(LabelTable table){
		this.table = table;
		this.init();
	}
	public void init() {


		this.lattice = null;
		this.lattice = new HashMap<Integer, Map<Integer,LatticeNode>>();
		Map<Integer,LatticeNode> initNodeMap = new HashMap<>();
		LatticeNode initNode = new LatticeNode();
		initNode.setMorph(SYMBOL.START);
		initNode.setPosId(this.table.getId(SYMBOL.START));
		initNode.setScore(0);
		initNodeMap.put(initNode.hashCode(), initNode);
		this.lattice.put(0, initNodeMap);

		//init
		initNode = null;
	}

	public boolean put(int beginIdx,int endIdx,String morph,int posId,double score){
		LatticeNode prevMaxNode = this.getPrevMaxNode(beginIdx,posId);
		if(prevMaxNode != null){
			LatticeNode curNode = new LatticeNode();
			curNode.setMorph(morph);
			curNode.setPosId(posId);
			curNode.setPrevHashcode(prevMaxNode.hashCode());
			curNode.setPrevIdx(beginIdx);
			curNode.setScore(prevMaxNode.getScore()+this.transition.get(prevMaxNode.getPosId(), posId)+score);
			this.insertLattice(endIdx,curNode);
			return true;
		}
		return false;
	}

	private void insertLattice(int endIdx, LatticeNode curNode) {
		Map<Integer,LatticeNode> latticeNodeMap = this.lattice.get(endIdx);
		if(latticeNodeMap == null){
			latticeNodeMap = new HashMap<>();
		}

		latticeNodeMap.put(curNode.hashCode(), curNode);
		this.lattice.put(endIdx, latticeNodeMap);
	}

	private LatticeNode getPrevMaxNode(int beginIdx, int posId) {
		Map<Integer,LatticeNode> prevNodes = this.lattice.get(beginIdx);
		if(prevNodes == null){
			return null;
		}
		Set<Entry<Integer,LatticeNode>> prevNodeSet = prevNodes.entrySet();
		double maxScore = Double.NEGATIVE_INFINITY;
		LatticeNode maxNode = null;
		for (Entry<Integer, LatticeNode> prevNode : prevNodeSet) {
			int prevPosId = prevNode.getValue().getPosId();
			Double transitionScore = this.transition.get(prevPosId, posId);
			if(transitionScore == null){
				continue;
			}
			double score = prevNode.getValue().getScore()+transitionScore;
			if(score > maxScore){
				maxScore = score;
				maxNode = prevNode.getValue();
			}
		}
		return maxNode;
	}

	public void setTransition(Transition transition) {
		this.transition = transition;
	}
	//	public void print(int maxIdx) {
	//		for(int i=0;i<maxIdx;i++){
	//			Map<Integer,LatticeNode> nodeMap = this.lattice.get(i);
	//			Set<Entry<Integer,LatticeNode>> entrySet = nodeMap.entrySet();
	//			for (Entry<Integer, LatticeNode> entry : entrySet) {
	//				System.out.println("["+entry.getValue().getPrevIdx()+","+i+"]"+entry.getValue());
	//			}
	//			System.out.println();
	//		}
	//	}
	public void print(int idx){
		Map<Integer,LatticeNode> nodeMap = this.lattice.get(idx);
		if(nodeMap == null){
			System.out.println("[?,"+idx+"]"+null);
		}else{
			Set<Entry<Integer,LatticeNode>> entrySet = nodeMap.entrySet();
			for (Entry<Integer, LatticeNode> entry : entrySet) {
				System.out.println("["+entry.getValue().getPrevIdx()+","+idx+"]"+entry.getValue());
			}
		}
		System.out.println();
	}
	public void printMax(int length) {
		List<String> resultList = this.getMax(length);
		StringBuffer sb = new StringBuffer();
		for (String result : resultList) {
			sb.append(result);
			sb.append(" ");
		}
		System.out.println(sb.toString().trim());
	}

	public List<String> getMax(int length) {
		LatticeNode lastNode = this.getPrevMaxNode(length, table.getId(SYMBOL.END));
		if(lastNode == null){
			Map<Integer,LatticeNode> newLastNodeMap = new HashMap<Integer, LatticeNode>(); 
			Map<Integer,LatticeNode> lastNodeMap = this.lattice.get(length);
			Set<Integer> nodeHashcodeSet = lastNodeMap.keySet();
			for (Integer nodeHashcode : nodeHashcodeSet) {
				LatticeNode lastNodeCandidate = lastNodeMap.get(nodeHashcode);
				Set<Integer> posIdSet = this.table.getIdLabelTable().keySet();
				for (Integer posId : posIdSet) {
					LatticeNode newLastNode = new LatticeNode();
					newLastNode.setMorph(lastNodeCandidate.getMorph());
					newLastNode.setPosId(posId);
					newLastNode.setPrevHashcode(lastNodeCandidate.getPrevHashcode());
					newLastNode.setPrevIdx(lastNodeCandidate.getPrevIdx());
					newLastNode.setScore(lastNodeCandidate.getScore());
					newLastNodeMap.put(newLastNode.hashCode(), newLastNode);
				}				
			}
			this.lattice.put(length, newLastNodeMap);
			lastNode = this.getPrevMaxNode(length, table.getId(SYMBOL.END));
		}

		List<String> result = new ArrayList<>();
		while(true){
			if(lastNode.getPosId() == this.table.getId(SYMBOL.START))break;
			String token = lastNode.getMorph()+"/"+table.getLabel(lastNode.getPosId());
			result.add(token);
			lastNode = this.lattice.get(lastNode.getPrevIdx()).get(lastNode.getPrevHashcode());
		}

		Collections.reverse(result);
		return result;
	}

}
