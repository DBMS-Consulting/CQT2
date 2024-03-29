package com.dbms.view;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
 
 
@ManagedBean
public class DummyView {
     
    private String console;
   
    private String car;  
    private List<SelectItem> cars;
 
    private String city;  
    private Map<String,String> cities = new HashMap<String, String>();
  
    private String value;
    
    //private Theme theme;   
    //private List<Theme> themes;
     
    //@ManagedProperty("#{themeService}")
    //private ThemeService service;
     
    @PostConstruct
    public void init() {
        //cars
        SelectItemGroup g1 = new SelectItemGroup("German Cars");
        g1.setSelectItems(new SelectItem[] {new SelectItem("BMW", "BMW"), new SelectItem("Mercedes", "Mercedes"), new SelectItem("Volkswagen", "Volkswagen")});
         
        SelectItemGroup g2 = new SelectItemGroup("American Cars");
        g2.setSelectItems(new SelectItem[] {new SelectItem("Chrysler", "Chrysler"), new SelectItem("GM", "GM"), new SelectItem("Ford", "Ford")});
         
        cars = new ArrayList<SelectItem>();
        cars.add(g1);
        cars.add(g2);
         
        //cities
        cities = new HashMap<String, String>();
        cities.put("New York", "New York");
        cities.put("London","London");
        cities.put("Paris","Paris");
        cities.put("Barcelona","Barcelona");
        cities.put("Istanbul","Istanbul");
        cities.put("Berlin","Berlin");
         
        //themes
        //themes = service.getThemes();
    }
 
    public String getConsole() {
        return console;
    }
 
    public void setConsole(String console) {
        this.console = console;
    }
 
    public String getCar() {
        return car;
    }
 
    public void setCar(String car) {
        this.car = car;
    }
 
    public String getCity() {
        return city;
    }
 
    public void setCity(String city) {
        this.city = city;
    }
 
    public List<SelectItem> getCars() {
        return cars;
    }
 
    public Map<String, String> getCities() {
        return cities;
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
 
}