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
        try {
            maxLimit = Integer.parseInt((String)component.getAttributes().get("maxLimit"));
        } catch(NumberFormatException e) {
            maxLimit = 0;
        }
       SelectCheckboxMenu myComponent = (SelectCheckboxMenu)component;

       if (maxLimit>0 && ((String[])myComponent.getSubmittedValue()).length > maxLimit) {
           FacesMessage msg
                   = new FacesMessage("Can not select more than " + maxLimit + " designees", "");
           msg.setSeverity(FacesMessage.SEVERITY_ERROR);
           throw new ValidatorException(msg);
       } 
   }
}