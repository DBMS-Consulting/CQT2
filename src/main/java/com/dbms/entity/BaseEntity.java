package com.dbms.entity;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

/**
 * Base class for auto-generated ID classes.
 */
@MappedSuperclass
public abstract class BaseEntity implements IEntity,Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1+super.hashCode();
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
		BaseEntity other = (BaseEntity) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Entity [class=" +this.getClass()+", id="+ getId() + "]";
	}

}
