package com.dbms.entity.cqt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dbms.entity.BaseEntity;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CMQ_PARENT_CHILD_TARGET")
public class CmqParentChildTarget extends BaseEntity {

	private static final long serialVersionUID = 4685729356569760611L;

	@Id
	@GeneratedValue(generator = "CMQ_RELATION_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "CMQ_RELATION_ID_SEQ", sequenceName = "CMQ_RELATION_ID_SEQ", allocationSize = 1)
	@Column(name = "CMQ_PARENT_CHILD_RELATION_ID", unique = true, nullable = false)
	private Long cmqRelationId;

	@Column(name = "CMQ_PARENT_CODE", precision = 38, scale = 0)
	private Long cmqParentCode;

	@Column(name = "CMQ_PARENT_NAME", length = 200)
	private String cmqParentName;

	@Column(name = "CMQ_CHILD_CODE", precision = 38, scale = 0)
	private Long cmqChildCode;

	@Column(name = "CMQ_CHILD_NAME", length = 200)
	private String cmqChildName;

	@Column(name = "CMQ_PARENT_TYPE_CD", nullable = false, length = 6)
	private String cmqParentTypeCd;

	@Column(name = "CMQ_CHILD_TYPE_CD", nullable = false, length = 6)
	private String cmqChildTypeCd;

	@Column(name = "PARENT_CMQ_ID", nullable = false)
	private Long parentCmqId;

	@Column(name = "CHILD_CMQ_ID", nullable = false)
	private Long childCmqId;

	@Override
	public Long getId() {
		return cmqRelationId;
	}

	public Long getCmqRelationId() {
		return cmqRelationId;
	}

	public void setCmqRelationId(Long cmqRelationId) {
		this.cmqRelationId = cmqRelationId;
	}

	public Long getCmqParentCode() {
		return cmqParentCode;
	}

	public void setCmqParentCode(Long cmqParentCode) {
		this.cmqParentCode = cmqParentCode;
	}

	public String getCmqParentName() {
		return cmqParentName;
	}

	public void setCmqParentName(String cmqParentName) {
		this.cmqParentName = cmqParentName;
	}

	public Long getCmqChildCode() {
		return cmqChildCode;
	}

	public void setCmqChildCode(Long cmqChildCode) {
		this.cmqChildCode = cmqChildCode;
	}

	public String getCmqChildName() {
		return cmqChildName;
	}

	public void setCmqChildName(String cmqChildName) {
		this.cmqChildName = cmqChildName;
	}

	public String getCmqParentTypeCd() {
		return cmqParentTypeCd;
	}

	public void setCmqParentTypeCd(String cmqParentTypeCd) {
		this.cmqParentTypeCd = cmqParentTypeCd;
	}

	public String getCmqChildTypeCd() {
		return cmqChildTypeCd;
	}

	public void setCmqChildTypeCd(String cmqChildTypeCd) {
		this.cmqChildTypeCd = cmqChildTypeCd;
	}

	public Long getParentCmqId() {
		return parentCmqId;
	}

	public void setParentCmqId(Long parentCmqId) {
		this.parentCmqId = parentCmqId;
	}

	public Long getChildCmqId() {
		return childCmqId;
	}

	public void setChildCmqId(Long childCmqId) {
		this.childCmqId = childCmqId;
	}

}
