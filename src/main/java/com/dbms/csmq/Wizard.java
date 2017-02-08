package com.dbms.csmq;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.event.FlowEvent;


@ManagedBean
@ViewScoped
public class Wizard implements Serializable {
 
     
    private boolean skip;
   
     
    
     
    public boolean isSkip() {
        return skip;
    }
 
    public void setSkip(boolean skip) {
        this.skip = skip;
    }
     
    public String onFlowProcess(FlowEvent event) {
    	System.out.println("**************  event flow ::" + event.getNewStep());
    	System.out.println("**************  event old flow ::" + event.getOldStep());

    	return event.getNewStep();
    }
    
    public String onFlowProcess2(FlowEvent event) {
    	System.out.println("**************  event flow ::" + event.getNewStep());
    	System.out.println("**************  event old flow ::" + event.getOldStep());

    	return event.getNewStep();
    }
}