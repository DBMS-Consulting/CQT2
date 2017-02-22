package com.dbms.csmq;

import java.io.Serializable;

public class HierarchyNode implements Serializable, Comparable<HierarchyNode> {
	 
    private String level;
     
    private String term;
     
    private String code;
     
    public HierarchyNode(String level, String term, String code) {
        this.level = level;
        this.term = term;
        this.code = code;
    }
 
  
    public String getLevel() {
		return level;
	}


	public void setLevel(String level) {
		this.level = level;
	}


	public String getTerm() {
		return term;
	}


	public void setTerm(String term) {
		this.term = term;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	//Eclipse Generated hashCode and equals
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        result = prime * result + ((term == null) ? 0 : term.hashCode());
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }
 
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HierarchyNode other = (HierarchyNode) obj;
        if (level == null) {
            if (other.level != null)
                return false;
        } else if (!level.equals(other.level))
            return false;
        if (term == null) {
            if (other.term != null)
                return false;
        } else if (!term.equals(other.term))
            return false;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        return true;
    }
 
    @Override
    public String toString() {
        return term;
    }
 
    public int compareTo(HierarchyNode document) {
        return this.getLevel().compareTo(document.getLevel());
    }
}  
