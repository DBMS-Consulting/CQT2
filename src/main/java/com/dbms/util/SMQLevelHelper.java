package com.dbms.util;

public class SMQLevelHelper {
	private int level;
	private String label;
	
	SMQLevelHelper(int level, String label) {
		this.level = level;
		this.label = label;
	}
	
	private static SMQLevelHelper[] allLevel = new SMQLevelHelper[] {
			new SMQLevelHelper(1, "SMQ1"),
			new SMQLevelHelper(2, "SMQ2"),
			new SMQLevelHelper(3, "SMQ3"),
			new SMQLevelHelper(4, "SMQ4"),
			new SMQLevelHelper(5, "SMQ5")
	};
	
	public static String getLabel(int level) {
		for(SMQLevelHelper i: allLevel) {
			if(i.level == level)
				return i.label;
		}
		return "";
	}
	
	public static int getMeddraLevelFromLabel(String label) {
		for (SMQLevelHelper i : allLevel) {
		  if(i.label.equals(label))
			  return i.level;
		}
		return 0;
	}
	
	public static SMQLevelHelper getByLabel(String label) {
		for (SMQLevelHelper i : allLevel) {
		  if(i.label.equals(label))
			  return i;
		}
		return null;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	
}
