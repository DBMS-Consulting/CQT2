package com.dbms.entity.cqt;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.dbms.entity.BaseEntity;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 6:13:56 AM
 **/
@Entity
@Table(name="SMQ_BASE_190")
public class SmqBase190 extends BaseEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(generator = "idGenerator")    
	@GenericGenerator(name = "idGenerator", strategy = "assigned") 
	@Column(name = "SMQ_CODE",length=10,unique=true,nullable=false)
	private Long id;
	@Column(name = "SMQ_NAME",length=300)
	private String name;
	@Column(name = "SMQ_LEVEL",length=1)
	private Integer level;
	@Lob
	@Column(name = "SMQ_DESCRIPTION",columnDefinition="CLOB")
	private String description;
	@Lob
	@Column(name = "SMQ_SOURCE",columnDefinition="CLOB")
	private String source;
	@Lob
	@Column(name = "SMQ_NOTE",columnDefinition="CLOB")
	private String note;
	@Column(name = "SMQ_STATUS",length=10)
	private String status;
	
	@Column(name = "SMQ_ALGORITHM",columnDefinition="CLOB")
	@Lob
	private String algorithm;
	@Column(name = "DICTIONARY_VER_DATE")
	private Date dictionaryVerDate;
	@Column(name = "DICTIONARY_VERSION",length=5)
	private String dictionaryVersion;
	
	@OneToMany(cascade=CascadeType.ALL)
	private Set<SmqBase190> smqBase190=new HashSet<>();
	
	@OneToMany(cascade=CascadeType.ALL)
	private Set<SmqRelation190> relations=new HashSet<>();
	
	@ManyToOne
	@JoinColumn(name="SMQ_PARENT_CODE")
	private SmqBase190 parent;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	public Date getDictionaryVerDate() {
		return dictionaryVerDate;
	}
	public void setDictionaryVerDate(Date dictionaryVerDate) {
		this.dictionaryVerDate = dictionaryVerDate;
	}
	public String getDictionaryVersion() {
		return dictionaryVersion;
	}
	public void setDictionaryVersion(String dictionaryVersion) {
		this.dictionaryVersion = dictionaryVersion;
	}
	public Set<SmqBase190> getSmqBase190() {
		return smqBase190;
	}
	public void setSmqBase190(Set<SmqBase190> smqBase190) {
		this.smqBase190 = smqBase190;
	}
	public SmqBase190 getParent() {
		return parent;
	}
	public void setParent(SmqBase190 parent) {
		this.parent = parent;
	}
	public Set<SmqRelation190> getRelations() {
		return relations;
	}
	public void setRelations(Set<SmqRelation190> relations) {
		this.relations = relations;
	}
	public void addRelation(SmqRelation190 relation){
		this.relations.add(relation);
		relation.setBase(this);
	}
	
}

