package com.ca.apm.commons.coda.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Utility class defined to make use of common object for both Oracle and Postgres using Runtime ploymorphism
 *
 * @author: Suresh Votla (votsu01)
 */

public class DBUtil {

public void deleteAllWebFilters(){};
public void setUpgradeToTimMonitoring(Boolean value){};

public String getResultStringFromQuery(String sqlStatement){
		return " ";
		};
		
		public boolean executeUpdateQuery(String query){
	        return false;
	    };
public ArrayList<ArrayList<String>> getArrayListStringFromQuery(String sqlStatement){
		return new ArrayList<ArrayList<String>>();
		};
public static ArrayList<ArrayList<String>> extract(ResultSet resultSet)	throws SQLException{
		return new ArrayList<ArrayList<String>>();
		};
}
