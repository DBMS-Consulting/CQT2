package com.dbms.util;

public class MeddraDictLevelHelper {
	private MeddraDictLevelType level;
	private String label;
	private String termPrefix;
	
	MeddraDictLevelHelper(MeddraDictLevelType level, String label, String termPrefix) {
		this.level = level;
		this.label = label;
		this.termPrefix = termPrefix;
	}
	
	private static MeddraDictLevelHelper[] allLevel = new MeddraDictLevelHelper[] {
			new MeddraDictLevelHelper(MeddraDictLevelType.SOC, "SOC", "SOC_"),
			new MeddraDictLevelHelper(MeddraDictLevelType.HLGT, "HLGT", "HLGT_"),
			new MeddraDictLevelHelper(MeddraDictLevelType.HLT, "HLT", "HLT_"),
			new MeddraDictLevelHelper(MeddraDictLevelType.PT, "PT", "PT_"),
			new MeddraDictLevelHelper(MeddraDictLevelType.LLT, "LLT", "LLT_")
	};
	
	public static String getLabel(MeddraDictLevelType meddraLvl) {
		for(MeddraDictLevelHelper i: allLevel) {
			if(i.level == meddraLvl)
				return i.label;
		}
		return "";
	}
	
	public static String getTermPrefix(MeddraDictLevelType meddraLvl) {
		for(MeddraDictLevelHelper i: allLevel) {
			if(i.level == meddraLvl)
				return i.termPrefix;
		}
		return "";
	}
	
	public static MeddraDictLevelType getMeddraLevelFromLabel(String label) {
		for (MeddraDictLevelHelper i : allLevel) {
		  if(i.label.equals(label))
			  return i.level;
		}
		return null;
	}
	
	public static MeddraDictLevelHelper getByLabel(String label) {
		for (MeddraDictLevelHelper i : allLevel) {
		  if(i.label.equals(label))
			  return i;
		}
		return null;
	}
}
