package com.dbms.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * Base class for all entity classes.
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(generator = "ID_GENERATOR", strategy = GenerationType.AUTO)
	@Column(name = "ID")
	protected Long id;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
	}

	@Override
	public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof BaseEntity)) {
            return false;
        }

        BaseEntity other = (BaseEntity) obj;

        if (id != null && other.getId() != null && id.equals(other.getId())) {
             return true;
        }
        
        return false;
	}

	@Override
	public String toString() {
		return String.format("this.class= %s,code=%s", this.getClass(), id);
	}
}
