/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dbms.view;

import com.dbms.service.IRefCodeListService;

/**
 *
 * @author gurejak
 */
public class SystemConfigProperties {
    
    private boolean displayScope;
    private boolean displayCategory;
    private boolean displayCategory2;
    private boolean displayWeight;

    public SystemConfigProperties(IRefCodeListService refCodeListService) {
        this.displayScope = refCodeListService.getScopeSystemConfig();
        this.displayCategory = refCodeListService.getCategorySystemConfig();
        this.displayCategory2 = refCodeListService.getCategory2SystemConfig();
        this.displayWeight = refCodeListService.getWeightSystemConfig();
    }

    public boolean isDisplayScope() {
        return displayScope;
    }

    public void setDisplayScope(boolean displayScope) {
        this.displayScope = displayScope;
    }

    public boolean isDisplayCategory() {
        return displayCategory;
    }

    public void setDisplayCategory(boolean displayCategory) {
        this.displayCategory = displayCategory;
    }

    public boolean isDisplayCategory2() {
        return displayCategory2;
    }

    public void setDisplayCategory2(boolean displayCategory2) {
        this.displayCategory2 = displayCategory2;
    }

    public boolean isDisplayWeight() {
        return displayWeight;
    }

    public void setDisplayWeight(boolean displayWeight) {
        this.displayWeight = displayWeight;
    }
    
    
}
