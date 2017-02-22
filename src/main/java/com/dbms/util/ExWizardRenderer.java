package com.dbms.util;


import java.io.IOException;
 
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
 
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.wizard.Wizard;
 
public class ExWizardRenderer extends org.primefaces.component.wizard.WizardRenderer {
     
    @Override
    protected void encodeStepStatus(FacesContext context, Wizard wizard) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String currentStep = wizard.getStep();
        boolean currentFound = false;
 
        writer.startElement("ul", null);
        writer.writeAttribute("class", Wizard.STEP_STATUS_CLASS, null);
        int i = 0;
        for(UIComponent child : wizard.getChildren()) {
            if(child instanceof Tab && child.isRendered()) {
                Tab tab = (Tab) child;
                boolean active = (!currentFound) && (currentStep == null || tab.getId().equals(currentStep));
                String titleStyleClass = active ? Wizard.ACTIVE_STEP_CLASS : Wizard.STEP_CLASS;
                if(tab.getTitleStyleClass() != null) {
                    titleStyleClass = titleStyleClass + " " + tab.getTitleStyleClass();
                }
                 
                if(active) {
                    currentFound = true;
                }
 
                writer.startElement("li", null);
                writer.writeAttribute("class", titleStyleClass, null);
                if(tab.getTitleStyle() != null) writer.writeAttribute("style", tab.getTitleStyle(), null);
                 
                writer.startElement("a", null);
                final String wiz = wizard.resolveWidgetVar();
                writer.writeAttribute("href", "javascript:"+"PF('" + wiz + "')"+".loadStep("+"PF('" + wiz + "')"+".cfg.steps["+i+"], false)", null);
                if (tab.getTitletip() != null) writer.writeAttribute("title", tab.getTitletip(), null);
                writer.write(tab.getTitle());
                writer.endElement("a");
                 
                writer.endElement("li");
                i++;
            }
        }
 
        writer.endElement("ul");
    }
     
} 