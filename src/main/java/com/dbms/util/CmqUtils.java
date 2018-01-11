package com.dbms.util;

import java.util.LinkedList;
import java.util.List;

import javax.faces.component.UIComponent;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

public class CmqUtils {
	public static int SQL_IN_CLAUSE_SPLIT = 999;

	public static String getExceptionMessageChain(Throwable throwable) {
		StringBuilder sb = new StringBuilder();
		boolean firstException = true;
		while (throwable != null) {
			if (!firstException) {
				sb.append(", Caused By:").append(throwable.getMessage());
			} else {
				sb.append(throwable.getMessage());
			}
			throwable = throwable.getCause();
		}
		return sb.toString();
	}

	public static String convertArrayToTableWith(List<Long> values, String tableName, String columnName) {
		// Oracle doc says odciNumberList can accept 32767 arguments but it
		// seems it can only accept 999
		List<List<Long>> ptvs = ListUtils.partition(values, 999);
		List<String> subtables = new LinkedList<>();
		for (List<Long> ptv : ptvs) {
			subtables.add("select column_value as " + columnName + " from table(sys.odciNumberList("
					+ StringUtils.join(ptv, ",") + "))");
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
}
