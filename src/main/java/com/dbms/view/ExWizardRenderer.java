package com.dbms.view;


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
        final String wiz = wizard.resolveWidgetVar();
        final String pfWiz = "PF('" + wiz + "')";
 
        writer.startElement("ul", null);
        writer.writeAttribute("class", Wizard.STEP_STATUS_CLASS, null);
        int i = 0;
        for(UIComponent child : wizard.getChildren()) {
            if(child instanceof Tab && child.isRendered()) {
                Tab tab = (Tab) child;
                boolean active = (!currentFound) && (currentStep == null || tab.getId().equals(currentStep));
                Object isBack = tab.getAttributes().get("isBack");
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
                
                final String tabhdr = wizard.getId() + ":tabhdr" + i;
                writer.startElement("a", null);
                writer.writeAttribute("href", "#", null);
                writer.writeAttribute("type", "button", null);
                writer.writeAttribute("id", tabhdr, null);
                //writer.writeAttribute("onclick", pfWiz+".loadStep("+pfWiz+".cfg.steps["+i+"], false);PrimeFaces.ab({s:'" + tabhdr + "',p:'" + tabhdr + "',u:'" + tabhdr + "'});return false;", null);
                if(isBack instanceof String && "true".equals(isBack))
                    writer.writeAttribute("onclick", pfWiz+".loadStep("+pfWiz+".cfg.steps["+i+"], true);return false;", null);
                else
                    writer.writeAttribute("onclick", pfWiz+".loadStep("+pfWiz+".cfg.steps["+i+"], false);return false;", null);
                
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