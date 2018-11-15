package com.dbms.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import javax.faces.component.UIComponent;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

public class CmqUtils {
    public static int SQL_IN_CLAUSE_SPLIT = 999;
    
    private static final Map<String, String> timeZoneMap = createMap();
    private static Map<String, String> createMap()
    {
        Map<String,String> timeZoneMap = new HashMap<>();
        timeZoneMap.put("EST", "America/New_York");
        timeZoneMap.put("PST", "America/Los_Angeles");
        timeZoneMap.put("CST", "America/Chicago");
        timeZoneMap.put("IST", "Asia/Kolkata");
        return timeZoneMap;
    }
    
	public static String getExceptionMessageChain(Throwable throwable) {
	    StringBuilder sb = new StringBuilder();
	    boolean firstException = true;
	    while (throwable != null) {
	    	if(!firstException) {
	    		sb.append(", Caused By:").append(throwable.getMessage());
	    	} else {
	    		sb.append(throwable.getMessage());
	    	}
	    	throwable = throwable.getCause();
	    }
	    return sb.toString();
	}
    
    public static String convertArrayToTableWith(List<Long> values, String tableName, String columnName) {
        // Oracle doc says odciNumberList can accept 32767 arguments but it seems it can only accept 999
        List<List<Long>> ptvs = ListUtils.partition(values, 999);
        List<String> subtables = new LinkedList<>();
        for(List<Long> ptv: ptvs) {
            subtables.add("select column_value as " + columnName
                + " from table(sys.odciNumberList(" + StringUtils.join(ptv, ",") + "))");
        }
        
        return "with " + tableName + " as (" + StringUtils.join(subtables, " union all ") + ")";
    }

	public static UIComponent findComponent(UIComponent root, String id) {
		UIComponent result = null;
		if (root.getId().equals(id)) {
			return root;
		}

		for (UIComponent child : root.getChildren()) {
			if (child.getId().equals(id)) {
				result = child;
				break;
			}
			result = findComponent(child, id);
			if (result != null)
				break;
		}
		return result;
	}
	
	public static String convertimeZone(String inputDatePattern,String inputDateString,String inputTimezone,String outputDatePattern,String outputTimezone) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(inputDatePattern).toFormatter();
		//Input date time from UI for a given timezone
		//"17-SEP-2018:10:39:27 AM PST"
		LocalDateTime inputDateTime = LocalDateTime.parse(inputDateString, formatter);
		
		// Input date time from UI timezone
		getDateZoneId(inputTimezone);
		ZoneId fromTimeZone = getDateZoneId(inputTimezone);    //Source timezone
		
		// Output date time is always EST since DB is in EST
        ZoneId toTimeZone = getDateZoneId(outputTimezone);  //Target timezone
         
         
        //Zoned date time at source timezone
        ZonedDateTime inputZoneTime = inputDateTime.atZone(fromTimeZone);      
         
        //Zoned date time at target timezone
        ZonedDateTime outputZoneTime = inputZoneTime.withZoneSameInstant(toTimeZone);
		
		DateTimeFormatter formatter2 = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(outputDatePattern).toFormatter();
         
        return formatter2.format(outputZoneTime);
	}

	private static ZoneId getDateZoneId(String inputTimezone) {
		ZoneId id = null;
		if(StringUtils.isEmpty(inputTimezone)) {
			id = ZoneId.of(timeZoneMap.get("EST"));
		} else {
			id = ZoneId.of(timeZoneMap.get(inputTimezone));
		}
		return id;
		 
		
	}

	public static String getFormattedDate(String timezone, Date dateToFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a z");
		sdf.setTimeZone(TimeZone.getTimeZone(timezone));
		String formattedDate = sdf.format(dateToFormat);

    	return formattedDate;
	}
}
