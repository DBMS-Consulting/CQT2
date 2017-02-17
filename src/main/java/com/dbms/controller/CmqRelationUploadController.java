package com.dbms.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;

/**
 * @date Feb 8, 2017 8:29:12 AM
 **/
@ManagedBean
@ViewScoped
public class CmqRelationUploadController implements Serializable {

	private static final long serialVersionUID = -3128179937516097111L;

	private static final Logger log = LoggerFactory
			.getLogger(CmqRelationUploadController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;
	
	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	private UploadedFile file;

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public void setCmqBaseService(ICmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	public void setCmqRelationService(ICmqRelation190Service cmqRelationService) {
		this.cmqRelationService = cmqRelationService;
	}

	public void upload() {
		log.debug("run a import file...{}", file.getFileName());
		int total=0;
		int success=0;
		int failed=0;
		if (file != null) {
			try {
				InputStreamReader isr = new InputStreamReader(
						file.getInputstream());
				BufferedReader reader = new BufferedReader(isr);
				String str = null;
				boolean first = true;
				while ((str = reader.readLine()) != null) {
					log.debug("read a line : {}", str);
					String[] ss = str.split(",");
					if (first) {
						first = false;
						continue;
					}
					total++;
					log.debug("fields length {}",ss.length);
					if(ss.length<5){
						failed++;
						break;
					}
					CmqBase190 base = null;
					Long id = null;
					
					try {
						id = Long.parseLong(ss[0]);
					} catch (Exception e) {
						log.error("Error - {}",e.getMessage(),e);
						failed++;
						break;
					}
					base = cmqBaseService.findById(id);
					if (base == null) {
						log.error("base is null with id {}",id);
						failed++;
						break;
					}
					CmqRelation190 relation=null;
					if(ss.length>1&&StringUtils.isEmpty(ss[1])){
						failed++;
						log.error("relation name is null {}",ss[1]);
						break;
					}
					relation=new CmqRelation190();
					relation.setBase(base);
					relation.setTermName(ss[1]);
					if(ss.length>2&&StringUtils.isNotEmpty(ss[2])&&ss[2].length()>3){
						failed++;
						log.error("leve is not formated {}",ss[2]);
						break;
					}
					relation.setTermDictLevel(ss[2]);
					if(ss.length>3&&StringUtils.isNotEmpty(ss[3])){
						CmqRelation190 parent=cmqRelationService.findByTermName(ss[3]);
						if(parent==null){
							log.error("no found parent Relation by {}",ss[3]);
						}
						parent.addChild(relation);
					}
					if(ss.length>5&&!StringUtils.isNotEmpty(ss[5])&&ss[5].length()==1){
						relation.setPtTermCategory(ss[5]);
					}
					if(ss.length>6)
						relation.setPtTermScope(ss[6]);
					Integer weight=null;
					if(ss.length>7&&StringUtils.isNotEmpty(ss[7])){
						try{
							weight=Integer.parseInt(ss[7]);
						}catch(Exception e){log.error("Error parse weight {}",e.getMessage(),e);}
					}
					relation.setPtTermWeight(weight);
					cmqRelationService.create(relation);
					success++;
				}
				FacesMessage msg = new FacesMessage("Successful import relation "
						+ file.getFileName()+", total="+total+",success="+success+",failed="+failed, null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} catch (Exception e) {
				e.printStackTrace();
				FacesMessage msg = new FacesMessage("Failed to import relation "
						+ file.getFileName() +", total="+total+",success="+success+",failed="+failed + " Reasion " + e.getMessage(),
						null);
				FacesContext.getCurrentInstance().addMessage(null, msg);

			}
		}

	}
}
