package com.dbms.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.primefaces.model.TreeNode;

import com.dbms.csmq.HierarchyNode;

public class HistoricalViewrelationsComparator implements Comparator<TreeNode>{
	
	private static Map<String, Integer> sequenceMap = new HashMap<>();
	static {
		sequenceMap.put("smq1", 1);
		sequenceMap.put("smq2", 2);
		sequenceMap.put("smq3", 3);
		sequenceMap.put("smq4", 4);
		sequenceMap.put("smq5", 5);
		sequenceMap.put("soc", 6);
		sequenceMap.put("hlgt", 7);
		sequenceMap.put("hlt", 8);
		sequenceMap.put("pt", 9);
		sequenceMap.put("llt", 10);
	}
	@Override
	public int compare(TreeNode o1, TreeNode o2) {
		HierarchyNode hierarchyNode1 = (HierarchyNode) o1.getData();
		HierarchyNode hierarchyNode2 = (HierarchyNode) o2.getData();
		if(sequenceMap.containsKey(hierarchyNode1.getLevel().toLowerCase()) && sequenceMap.containsKey(hierarchyNode2.getLevel().toLowerCase())) {
			return sequenceMap.get(hierarchyNode1.getLevel().toLowerCase())-sequenceMap.get(hierarchyNode2.getLevel().toLowerCase());
		} else if(sequenceMap.containsKey(hierarchyNode1.getLevel().toLowerCase()) && !sequenceMap.containsKey(hierarchyNode2.getLevel().toLowerCase())){
			return -1;
		} else{
			return 1;
		}
		
	}

	/*@Override
	public int compare(HierarchyNode o1, HierarchyNode o2) {
		if(o1.getLevel().startsWith("SMQ") && o2.getLevel().startsWith("SMQ")) {
			return o1.getLevel().compareTo(o2.getLevel());
		} else if(o1.getLevel().startsWith("SMQ") && !o2.getLevel().startsWith("SMQ")) {
			return -1;
		} else if(!o1.getLevel().startsWith("SMQ") && o2.getLevel().startsWith("SMQ")) {
			return 1;
		} else if(!o1.getLevel().startsWith("SMQ")){
			if(o1.getLevel().equalsIgnoreCase("SOC") && o2.getLevel().equalsIgnoreCase("hlgt")) {
				return -1;
			} else if(o1.getLevel().equalsIgnoreCase("hlgt") && o2.getLevel().equalsIgnoreCase("hlt")) {
				return -1;
			} else if(o1.getLevel().equalsIgnoreCase("hlt") && o2.getLevel().equalsIgnoreCase("pt")) {
				return -1;
			} else if(o1.getLevel().equalsIgnoreCase("pt") && o2.getLevel().equalsIgnoreCase("llt")) {
				return -1;
			}  else if(o1.getLevel().equals(o2.getLevel())) {
				return 0;
			} else {
				return 1;
			}
				
		} else if(o1.getLevel().equals(o2.getLevel())) {
			return 0;
		} else {
			return 1;
		}*/
}
