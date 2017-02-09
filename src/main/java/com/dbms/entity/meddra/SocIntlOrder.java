package com.dbms.entity.meddra;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 5:55:25 AM
 **/
@Entity
@Table(name = "CQT_SOC_INTL_ORDER")
public class SocIntlOrder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3964539834842335714L;

	@Id
	@GeneratedValue(generator = "pkGenerator")
	@GenericGenerator(name = "pkGenerator", strategy = "foreign", parameters = @Parameter(name = "property", value = "socTerm"))
	@Column(name = "SOC_CODE", nullable = false, unique = true)
	private Integer id;
	
	@Column(name = "INTL_ORD_CODE", nullable = false)
	private Integer intlOrdCode;
	
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	private SocTerm socTerm;

	public Integer getIntlOrdCode() {
		return intlOrdCode;
	}

	public void setIntlOrdCode(Integer intlOrdCode) {
		this.intlOrdCode = intlOrdCode;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SocTerm getSocTerm() {
		return socTerm;
	}

	public void setSocTerm(SocTerm socTerm) {
		this.socTerm = socTerm;
	}

}
