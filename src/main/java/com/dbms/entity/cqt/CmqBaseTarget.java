package com.dbms.entity.cqt;

import com.dbms.csmq.CSMQBean;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dbms.entity.BaseEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CMQ_BASE_TARGET")
public class CmqBaseTarget extends BaseEntity {

	private static final long serialVersionUID = -2129921300717382258L;
	
	public static final String CMQ_STATUS_VALUE_ACTIVE = "A";
	public static final String CMQ_STATUS_VALUE_LABEL_ACTIVE = "ACTIVE";
	public static final String CMQ_STATE_PENDING_IA = "PENDING IA";
    public static final String CMQ_STATE_REVIEWED_IA = "REVIEWED IA";
	public static final String CMQ_STATE_APPROVED_IA = "APPROVED IA";
    public static final String CMQ_STATE_PUBLISHED = "PUBLISHED";
	public static final String CMQ_STATE_PUBLISHED_IA = "PUBLISHED IA";


	@Id
	@GeneratedValue(generator = "CMQ_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "CMQ_ID_SEQ", sequenceName = "CMQ_ID_SEQ", allocationSize = 1)
	@Column(name = "CMQ_ID", unique = true, nullable = false)
	private Long cmqId;

	@Column(name = "CMQ_CODE", unique = true, nullable = false, precision = 38)
	private Long cmqCode;

	@Column(name = "CMQ_NAME", nullable = false, length = 200)
	private String cmqName;

	@Column(name = "CMQ_TYPE_CD", nullable = false, length = 6)
	private String cmqTypeCd;

	@Column(name = "CMQ_LEVEL", nullable = false, precision = 1)
	private Integer cmqLevel;

	@Column(name = "CMQ_PARENT_CODE", precision = 38, scale = 0)
	private Long cmqParentCode;

	@Column(name = "CMQ_PARENT_NAME", length = 200)
	private String cmqParentName;

	@Column(name = "CMQ_DESCRIPTION", nullable = false, length = 4000)
	private String cmqDescription;

	@Column(name = "CMQ_STATUS", nullable = false, length = 1)
	private String cmqStatus;

	@Column(name = "CMQ_STATE", nullable = false, length = 15)
	private String cmqState;

	@Column(name = "CMQ_CRITICAL_EVENT", length = 5)
	private String cmqCriticalEvent;

	@Column(name = "CMQ_ALGORITHM", length = 300)
	private String cmqAlgorithm;

	@Column(name = "CMQ_SOURCE", length = 4000)
	private String cmqSource;

	@Column(name = "CMQ_NOTE")
	@Lob
	private String cmqNote = null;

	@Column(name = "CMQ_PROGRAM_CD", nullable = false, length = 200)
	private String cmqProgramCd;

	@Column(name = "CMQ_PROTOCOL_CD", nullable = false, length = 100)
	private String cmqProtocolCd;

	@Column(name = "CMQ_DESIGNEE", nullable = false, length = 100)
	private String cmqDesignee;

	@Column(name = "CMQ_GROUP", length = 100)
	private String cmqGroup;

	@Temporal(TemporalType.DATE)
	@Column(name = "CMQ_DUE_DATE", length = 7)
	private Date cmqDueDate;

	/**
	 * Request for reason.
	 */
	@Column(name = "CMQ_WF_DESC", length = 200)
	private String cmqWfDesc;

	@Column(name = "IMPACT_TYPE", length = 15)
	private String impactType;

	@Column(name = "CREATED_BY", nullable = false, length = 4000)
	private String createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE", nullable = false, length = 7)
	private Date creationDate;

	@Column(name = "LAST_MODIFIED_BY", length = 4000)
	private String lastModifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_MODIFIED_DATE", length = 7)
	private Date lastModifiedDate;

	@Column(name = "ACTIVATED_BY", length = 30)
	private String activatedBy;

	@Temporal(TemporalType.DATE)
	@Column(name = "ACTIVATION_DATE", length = 7)
	private Date activationDate;

	@Column(name = "DICTIONARY_NAME", nullable = false, length = 10)
	private String dictionaryName;

	@Column(name = "DICTIONARY_VERSION", nullable = false, length = 5)
	private String dictionaryVersion;

	@Column(name = "CMQ_SUBVERSION", nullable = false, precision = 10)
	private BigDecimal cmqSubversion;

	@Column(name = "CMQ_DESIGNEE2", length = 100)
	private String cmqDesignee2;

	@Column(name = "CMQ_DESIGNEE3", length = 100)
	private String cmqDesignee3;

	@Column(name = "CMQ_APPROVE_REASON", length = 4000)
	private String cmqApproveReason;
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="cmqBaseTarget", fetch=FetchType.EAGER, orphanRemoval=true)
	private List<CmqProductBaseTarget> productsList;

	public Long getId() {
		return cmqId;
	}

	public void setId(Long cmqId) {
		this.cmqId = cmqId;
	}

	public Long getCmqCode() {
		return cmqCode;
	}

	public void setCmqCode(Long cmqCode) {
		this.cmqCode = cmqCode;
	}

	public String getCmqName() {
		return cmqName;
	}

	public void setCmqName(String cmqName) {
		this.cmqName = cmqName;
	}

	public String getCmqTypeCd() {
		return cmqTypeCd;
	}

	public void setCmqTypeCd(String cmqTypeCd) {
		this.cmqTypeCd = cmqTypeCd;
	}

	public Integer getCmqLevel() {
		return cmqLevel;
	}

	public void setCmqLevel(Integer cmqLevel) {
		this.cmqLevel = cmqLevel;
	}

	public String getCmqParentName() {
		return cmqParentName;
	}

	public void setCmqParentName(String cmqParentName) {
		this.cmqParentName = cmqParentName;
	}

	public String getCmqDescription() {
		return cmqDescription;
	}

	public void setCmqDescription(String cmqDescription) {
		this.cmqDescription = cmqDescription;
	}

	public String getCmqStatus() {
		return cmqStatus;
	}

	public void setCmqStatus(String cmqStatus) {
		this.cmqStatus = cmqStatus;
	}

	public String getCmqState() {
		return StringUtils.upperCase(cmqState);
	}

	public void setCmqState(String cmqState) {
		this.cmqState = StringUtils.upperCase(cmqState);
	}

	public String getCmqCriticalEvent() {
		return cmqCriticalEvent;
	}

	public void setCmqCriticalEvent(String cmqCriticalEvent) {
		this.cmqCriticalEvent = cmqCriticalEvent;
	}

	public String getCmqAlgorithm() {
		return cmqAlgorithm;
	}

	public void setCmqAlgorithm(String cmqAlgorithm) {
		this.cmqAlgorithm = cmqAlgorithm;
	}

	public String getCmqSource() {
		return cmqSource;
	}

	public void setCmqSource(String cmqSource) {
		this.cmqSource = cmqSource;
	}

	public String getCmqNote() {
		return cmqNote;
	}

	public void setCmqNote(String cmqNote) {
		this.cmqNote = cmqNote;
	}

	public String getCmqProgramCd() {
		return cmqProgramCd;
	}

	public void setCmqProgramCd(String cmqProgramCd) {
		this.cmqProgramCd = cmqProgramCd;
	}

	public String getCmqProtocolCd() {
		return cmqProtocolCd;
	}

	public void setCmqProtocolCd(String cmqProtocolCd) {
		this.cmqProtocolCd = cmqProtocolCd;
	}

	public String getCmqDesignee() {
		return cmqDesignee;
	}

	public void setCmqDesignee(String cmqDesignee) {
		this.cmqDesignee = cmqDesignee;
	}

	public String getCmqGroup() {
		return cmqGroup;
	}

	public void setCmqGroup(String cmqGroup) {
		this.cmqGroup = cmqGroup;
	}

	public Date getCmqDueDate() {
		return cmqDueDate;
	}

	public void setCmqDueDate(Date cmqDueDate) {
		this.cmqDueDate = cmqDueDate;
	}

	public String getCmqWfDesc() {
		return cmqWfDesc;
	}

	public void setCmqWfDesc(String cmqWfDesc) {
		this.cmqWfDesc = cmqWfDesc;
	}

	public String getImpactType() {
		return impactType;
	}

	public void setImpactType(String impactType) {
		this.impactType = impactType;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getActivatedBy() {
		return activatedBy;
	}

	public void setActivatedBy(String activatedBy) {
		this.activatedBy = activatedBy;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public String getDictionaryName() {
		return dictionaryName;
	}

	public void setDictionaryName(String dictionaryName) {
		this.dictionaryName = dictionaryName;
	}

	public String getDictionaryVersion() {
		return dictionaryVersion;
	}

	public void setDictionaryVersion(String dictionaryVersion) {
		this.dictionaryVersion = dictionaryVersion;
	}

	public BigDecimal getCmqSubversion() {
		return cmqSubversion;
	}

	public void setCmqSubversion(BigDecimal cmqSubversion) {
		this.cmqSubversion = cmqSubversion;
	}

	public Long getCmqId() {
		return cmqId;
	}

	public Long getCmqParentCode() {
		return cmqParentCode;
	}

	public void setCmqParentCode(Long cmqParentCode) {
		this.cmqParentCode = cmqParentCode;
	}

	public String getCmqDesignee2() {
		return cmqDesignee2;
	}

	public void setCmqDesignee2(String cmqDesignee2) {
		this.cmqDesignee2 = cmqDesignee2;
	}

	public String getCmqDesignee3() {
		return cmqDesignee3;
	}

	public void setCmqDesignee3(String cmqDesignee3) {
		this.cmqDesignee3 = cmqDesignee3;
	}

	public String getCmqApproveReason() {
		return cmqApproveReason;
	}

	public void setCmqApproveReason(String cmqApproveReason) {
		this.cmqApproveReason = cmqApproveReason;
	}
    
    public String[] getCmqProductCds() {
		int s = this.productsList == null ? 0 : this.productsList.size();
        HashSet<String> upcds = new HashSet<String>();
		String[] productCds;
		if(s > 0) {
			int i=0;
			for(CmqProductBaseTarget p: productsList) {
				upcds.add(p.getCmqProductCd());
			}
		}
        productCds = new String[upcds.size()];
        upcds.toArray(productCds);
        return productCds;
	}
	
	public void setCmqProductCds(String[] productCds) {
		List<CmqProductBaseTarget> newlySetProducts = new ArrayList<CmqProductBaseTarget>();
        List<CmqProductBaseTarget> deletedProducts = new ArrayList<CmqProductBaseTarget>(productsList);
        
		for(String p: productCds) {
			newlySetProducts.add(addCmqProduct(p));
		}
        deletedProducts.removeAll(newlySetProducts);
        productsList = newlySetProducts;
        
        for(CmqProductBaseTarget p: deletedProducts) {
            p.setCmqBaseTarget(null);
        }
	}
	
	public CmqProductBaseTarget addCmqProduct(String productCd) {
		int s = this.productsList == null ? 0 : this.productsList.size();
		if(s > 0) {
			// check if it already exists
			for(CmqProductBaseTarget p: productsList) {
				if(p.getCmqProductCd().equals(productCd)) {
					p.setCmqBaseTarget(this);
					return p;
				}
			}
		}
		CmqProductBaseTarget p = new CmqProductBaseTarget() ;
		p.setCmqBaseTarget(this);
		p.setCmqProductCd(productCd);
		p.setCmqCode(cmqCode);
		p.setCmqName(cmqName);
		if(this.productsList == null)
			this.productsList = new ArrayList<>();
		this.productsList.add(p);
		return p;
	}
	
	public int removeCmqProduct(String productCd) {
		int s = this.productsList == null ? 0 : this.productsList.size();
		if(s > 0) {
			// check if it already exists
			for(CmqProductBaseTarget p: productsList) {
				if(p.getCmqProductCd().equals(productCd)) {
					p.setCmqBaseTarget(null);
					productsList.remove(p);
				}
			}
		}
		return 0;
	}

	public List<CmqProductBaseTarget> getProductsList() {
		return productsList;
	}

	public void setProductsList(List<CmqProductBaseTarget> productsList) {
		this.productsList = productsList;
	}
    
    /**
     * Checks if current CMQ_BASE_TARGET is impacted by Meddra versioning
     * @return 
     */
    public boolean isImpactedByMeddraVersioning() {
        if(CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(getImpactType())
                || CSMQBean.IMPACT_TYPE_ICC.equalsIgnoreCase(getImpactType())
                || CSMQBean.IMPACT_TYPE_IPC.equalsIgnoreCase(getImpactType()))
            return true;
        return false;
    }
}