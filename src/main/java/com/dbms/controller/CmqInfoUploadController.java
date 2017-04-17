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
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICmqBase190Service;

/**
 * @date Feb 8, 2017 8:29:12 AM
 **/
@ManagedBean
@ViewScoped
public class CmqInfoUploadController implements Serializable {

	private static final long serialVersionUID = -3128179937516097111L;

	private static final Logger log = LoggerFactory.getLogger(CmqInfoUploadController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
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

	public void upload() {
		log.debug("run a import file...{}", file.getFileName());
		int total = 0;
		int success = 0;
		int failed = 0;
		if (file != null) {
			try {
				InputStreamReader isr = new InputStreamReader(file.getInputstream());
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
						failed++;
						continue;
					}
					try {
						id = Long.parseLong(ss[0]);
					} catch (Exception e) {
						failed++;
						continue;
					}
					base = cmqBaseService.findById(id);
					if (base == null) {
						failed++;
						continue;
					}
					if (ss.length > 1 && StringUtils.isNotEmpty(ss[1]))
						base.setCmqDescription(ss[1]);
					if (ss.length > 2 && StringUtils.isNotEmpty(ss[2]))
						base.setCmqNote(ss[2]);
					if (ss.length > 3 && StringUtils.isNotEmpty(ss[3]))
						base.setCmqSource(ss[3]);
					log.debug("save entity {}", base);
					cmqBaseService.update(base, this.authService.getUserCn()
							, this.authService.getUserGivenName(), this.authService.getUserSurName()
							, this.authService.getCombinedMappedGroupMembershipAsString());
					success++;
				}
				FacesMessage msg = new FacesMessage("Successful import " + file.getFileName() + ", total=" + total
						+ ",success=" + success + ",failed=" + failed, null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} catch (Exception e) {
				e.printStackTrace();
				FacesMessage msg = new FacesMessage("Failed to import " + file.getFileName() + ", total=" + total
						+ ",success=" + success + ",failed=" + failed, "Reasion " + e.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);

			}
		}

	}

	public AuthenticationService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	}
}
