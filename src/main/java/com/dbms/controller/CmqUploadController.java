package com.dbms.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Date;

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
import com.dbms.service.CmqBase190Service;

/**
 * @date Feb 8, 2017 8:29:12 AM
 **/
@ManagedBean
@ViewScoped
public class CmqUploadController implements Serializable {

	private static final long serialVersionUID = -3128179937516097111L;

	private static final Logger log = LoggerFactory
			.getLogger(CmqUploadController.class);

	@ManagedProperty("#{cmqBase190Service}")
	private CmqBase190Service cmqBaseService;

	private UploadedFile file;

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public void setCmqBaseService(CmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
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
					CmqBase190 base = null;
					Long id = null;
					if (StringUtils.isEmpty(ss[0])) {
						failed++;continue;
					}
					try {
						id = Long.parseLong(ss[0]);
					} catch (Exception e) {
						failed++;
						continue;
					}
					log.debug("find by id {}",id);
					base = cmqBaseService.findById(id);
					if (base != null) {
						failed++;
						continue;
					}
					base=new CmqBase190();

					base.setId(id);
					base.setName(ss[1]);
					base.setType(ss[2]);
					base.setProductName(ss[3]);
					base.setDrugProgram(ss[4]);
					base.setProtocolName(ss[5]);
					Integer level=null;
					try{
						level=Integer.parseInt(ss[6]);
					}catch(Exception e){}
					base.setLevel(level);
					base.setDesignee(ss[7]);
					base.setReleaseGroup(ss[8]);
					base.setReleaseStatus(ss[9]);
					if(StringUtils.isNotEmpty(ss[10])&&!ss[10].equalsIgnoreCase("NO GROUP")){
						base.setGroup(ss[10]);
					}
					base.setAlgorithm(ss[11]);
					if(StringUtils.isNotEmpty(ss[12])){
						if(ss[12].equalsIgnoreCase("pending")){
							base.setStatus("P");
						}else if(ss[12].equalsIgnoreCase("active")){
							base.setStatus("A");
						}else if(ss[12].equalsIgnoreCase("inactive")){
							base.setStatus("I");
						}
					}
					base.setCriticalEvent(ss[13]);
					if(StringUtils.isNotEmpty(ss[14])){
						if(ss[14].equalsIgnoreCase("no")){
							base.setScope("N");
						}else if(ss[14].equals("yes")){
							base.setScope("Y");
						}
					}
					base.setState(ss[15]);
					base.setCreatedBy(ss[16]);
					Date created = null;
					if (StringUtils.isNotEmpty(ss[17])
							&& ss[17].equalsIgnoreCase("SYSTEM DATE")) {
						created = new Date();
						base.setCreationDate(created);
					}
					log.debug("save entity {}", base);
					cmqBaseService.create(base);
					success++;
				}
				FacesMessage msg = new FacesMessage("Successful import "
						+ file.getFileName()+", total="+total+",success="+success+",failed="+failed, null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} catch (Exception e) {
				e.printStackTrace();
				FacesMessage msg = new FacesMessage("Failed to import "
						+ file.getFileName() +", total="+total+",success="+success+",failed="+failed + " Reasion " + e.getMessage(),
						null);
				FacesContext.getCurrentInstance().addMessage(null, msg);

			}
		}

	}
}
