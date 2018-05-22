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

import com.dbms.entity.cqt.CreateEntity;
import com.dbms.entity.cqt.User;
import com.dbms.service.CreateEntityService;
import com.dbms.service.UserService;

/**
 * @date Feb 8, 2017 8:29:12 AM
 **/
@ManagedBean
@ViewScoped
public class TmeUploadController implements Serializable {

	private static final long serialVersionUID = -3128179937516097111L;

	@ManagedProperty("#{createEntityService}")
	private CreateEntityService createEntityService;
	@ManagedProperty("#{userService}")
	private UserService userService;

	private UploadedFile file;

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public void setCreateEntityService(CreateEntityService createEntityService) {
		this.createEntityService = createEntityService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	private static final Logger log = LoggerFactory.getLogger(TmeUploadController.class);

	public void upload() {
		log.debug("run a import file...{}", file.getSize());
		if (file != null) {
			try {
				InputStreamReader isr = new InputStreamReader(file.getInputstream());
				BufferedReader reader = new BufferedReader(isr);
				String str = null;
				boolean first = true;
				while ((str = reader.readLine()) != null) {
					log.debug("read a TME : {}", str);
					String[] ss = str.split(",");
					
					if (first) {
						first = false;
						continue;
					}
					if (ss.length != 18) {
						continue;
					}
					CreateEntity createEntity = null;
					Integer code=null;
					if (StringUtils.isNotEmpty(ss[0])) {
						try {
							code=Integer.parseInt(ss[0]);
							createEntity = createEntityService.findByCode(code);
							if(createEntity!=null){
								continue;
							}
						} 
						
						catch (Exception e) {
							continue;
						}
					}
					
					else{
						continue;
					}
					createEntity=new CreateEntity();

					User creator = null;
					if (StringUtils.isNotEmpty(ss[16])) {
						creator = userService.findByName(ss[16]);
						if (creator == null) {
							creator = new User();
							creator.setName(ss[16]);
							userService.save(creator);
						}
						createEntity.setCreator(creator);
					}

					createEntity.setCode(code);
					createEntity.setName(ss[1]);
					createEntity.setExtension(ss[2]);
					createEntity.setProduct(ss[3]);
					createEntity.setDrugProgram(ss[4]);
					createEntity.setProtocol(ss[5]);
					createEntity.setLevel(ss[6]);
					createEntity.setDesignee(ss[7]);
					createEntity.setReleaseGroup(ss[8]);
					createEntity.setReleaseStatus(ss[9]);
					createEntity.setGroup(ss[10]);
					createEntity.setAlgorithm(ss[11]);
					createEntity.setStatus(ss[12]);
					createEntity.setCriticalEvent(ss[13]);
					createEntity.setScope(ss[14]);
					createEntity.setState(ss[15]);
					if (StringUtils.isNotEmpty(ss[17]) && ss[17].equalsIgnoreCase("SYSTEM DATE")) {
						createEntity.setCreated(new Date());
					}
					log.debug("save entity {}",createEntity);
					createEntityService.save(createEntity);
				}
				FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Successful import " + file.getFileName(), null));
			} 
			
			catch (Exception e) {
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to import " + file.getFileName(),
                                "Reasion " + e.getMessage()));

			}
		}

	}
}
