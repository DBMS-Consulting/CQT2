package com.dbms.util;

public class MeddraDictLevelHelper {
	public static final int SEARCH_MEDDRA_BASE = 1;
	public static final int SEARCH_MEDDRA_BASE_REVERSE = 2;
    
    public static final MeddraDictLevelHelper MeddraDictLevel_SOC = new MeddraDictLevelHelper(MeddraDictLevelType.SOC, "SOC", "SOC_", SEARCH_MEDDRA_BASE);
    public static final MeddraDictLevelHelper MeddraDictLevel_HLGT = new MeddraDictLevelHelper(MeddraDictLevelType.SOC, "HLGT", "HLGT_", SEARCH_MEDDRA_BASE);
    public static final MeddraDictLevelHelper MeddraDictLevel_HLT = new MeddraDictLevelHelper(MeddraDictLevelType.HLT, "HLT", "HLT_", SEARCH_MEDDRA_BASE);
    public static final MeddraDictLevelHelper MeddraDictLevel_PT = new MeddraDictLevelHelper(MeddraDictLevelType.PT, "PT", "PT_", SEARCH_MEDDRA_BASE_REVERSE);
    public static final MeddraDictLevelHelper MeddraDictLevel_LLT = new MeddraDictLevelHelper(MeddraDictLevelType.LLT, "LLT", "LLT_", SEARCH_MEDDRA_BASE_REVERSE);
    
	
	private MeddraDictLevelType myLevel;
	private String myLabel;
	private String myTermPrefix;
	private int mySearchFrom;
	
	MeddraDictLevelHelper(MeddraDictLevelType level, String label, String termPrefix, int searchFrom) {
		this.myLevel = level;
		this.myLabel = label;
		this.myTermPrefix = termPrefix;
		this.mySearchFrom = searchFrom;
	}
	
	private static MeddraDictLevelHelper[] allLevel = new MeddraDictLevelHelper[] {
        MeddraDictLevel_SOC,
		MeddraDictLevel_HLGT,
		MeddraDictLevel_HLT,
		MeddraDictLevel_PT,
		MeddraDictLevel_LLT
	};
	
	public static String getLabel(MeddraDictLevelType meddraLvl) {
		for(MeddraDictLevelHelper i: allLevel) {
			if(i.myLevel == meddraLvl)
				return i.myLabel;
		}
		return "";
	}
	
	public static String getTermPrefix(MeddraDictLevelType meddraLvl) {
		for(MeddraDictLevelHelper i: allLevel) {
			if(i.myLevel == meddraLvl)
				return i.myTermPrefix;
		}
		return "";
	}
	
	public static MeddraDictLevelType getMeddraLevelFromLabel(String label) {
		for (MeddraDictLevelHelper i : allLevel) {
		  if(i.myLabel.equals(label))
			  return i.myLevel;
		}
		return null;
	}
	
	public static MeddraDictLevelHelper getByLabel(String label) {
		for (MeddraDictLevelHelper i : allLevel) {
		  if(i.myLabel.equals(label))
			  return i;
		}
		return null;
	}

	public MeddraDictLevelType getLevel() {
		return myLevel;
	}

	public void setLevel(MeddraDictLevelType level) {
		this.myLevel = level;
	}

	public String getLabel() {
		return myLabel;
	}

	public void setLabel(String label) {
		this.myLabel = label;
	}

	public String getTermPrefix() {
		return myTermPrefix;
	}

	public void setTermPrefix(String termPrefix) {
		this.myTermPrefix = termPrefix;
	}

	public int getSearchFrom() {
		return mySearchFrom;
	}

	public void setSearchFrom(int searchFrom) {
		this.mySearchFrom = searchFrom;
	}
	
	
}
