package com.dbms.view;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.primefaces.component.selectcheckboxmenu.SelectCheckboxMenu;

/**
 *
 * @author Andrius Mielkus
 */
@FacesValidator("mySelectCheckboxMenuLimitValidator")
public class SelectCheckboxMenuLimitValidator implements Validator {

   public SelectCheckboxMenuLimitValidator() {

   }

   @Override
   public void validate(FacesContext context, UIComponent component,
           Object value) throws ValidatorException { 
        //get limit
        Integer maxLimit = 0;
        Integer minLimit = 0;
        try {
            maxLimit = Integer.parseInt(component.getAttributes().get("maxLimit").toString());
        } catch(Exception e) {
            maxLimit = 0;
        }
        try {
            minLimit = Integer.parseInt(component.getAttributes().get("minLimit").toString());
        } catch(Exception e) {
            minLimit = 0;
        }
        
        String label = (String)component.getAttributes().get("label");
        String label1 = (String)component.getAttributes().get("validatorLabel");
        if(label1 != null)
            label = label1;
        if(label == null)
            label = "";
        
        SelectCheckboxMenu myComponent = (SelectCheckboxMenu)component;

        if (maxLimit>0 && ((String[])myComponent.getSubmittedValue()).length > maxLimit) {
            FacesMessage msg
                    = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Can not select more than " + maxLimit + " " + label, "");
            throw new ValidatorException(msg);
        }
        
        if (minLimit>0 && ((String[])myComponent.getSubmittedValue()).length < minLimit) {
            FacesMessage msg
                    = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Must select at least " + minLimit + " " + label, "");
            if(minLimit == 1)
                msg.setSummary(label + " is required");
            throw new ValidatorException(msg);
        } 
   }
}