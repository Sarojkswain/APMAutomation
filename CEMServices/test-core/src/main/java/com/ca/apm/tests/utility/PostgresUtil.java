package com.ca.apm.tests.utility;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

public class PostgresUtil extends DBUtil{
	
	private Connection conn;
	
	public PostgresUtil(String host, String port, String db, String user, String pwd){
		//load driver
		try{
			Driver driver = (Driver) Class.forName("org.postgresql.Driver").newInstance();
			DriverManager.registerDriver(driver);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//Build connection url
		String url = "jdbc:postgresql://"+host+":"+port+"/"+db;
		try{
			conn = DriverManager.getConnection(url, user, pwd);
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void deleteAllWebFilters(){
		try{
			PreparedStatement st = conn.prepareStatement("delete from ts_web_servers");
			st.execute();
			st.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void setUpgradeToTimMonitoring(Boolean value){
		try{
			PreparedStatement st = conn.prepareStatement("update ts_settings set ts_value = ? where ts_key = 'upgradeToTimMonitoring'");
			st.setBoolean(1, value);
			st.executeUpdate();
			st.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public Boolean getUpgradeToTimeMonitoring(){
		Boolean upgrade = false;
		try {
			PreparedStatement st = conn.prepareStatement("SELECT ts_value from ts_settings where ts_key = 'upgradeToTimMonitoring'");
			ResultSet rs = st.executeQuery();
			rs.next();
			upgrade = rs.getBoolean("ts_value");
			rs.close();
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return upgrade;
	}

	public Boolean isUserDeleted(Integer pId){
		Boolean deleted = false;
		try {
			PreparedStatement st = conn.prepareStatement("SELECT ts_soft_delete from ts_users where ts_id = ?");
			st.setInt(1, pId);
			st.executeQuery();
			ResultSet rs = st.executeQuery();
			rs.next();
			deleted = rs.getBoolean("ts_value");
			rs.close();
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return deleted;
	}
	
	public boolean executeUpdateQuery(String query){
        int i=0;
        try{
            Statement st = conn.createStatement();
            System.out.println("Executing query on database:");
            System.out.println(query);
            i = st.executeUpdate(query);
            st.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return i>0?true:false;
    }


	public String getResultStringFromQuery(String sqlStatement){
		String result = "";
		try {
			PreparedStatement st = conn.prepareStatement(sqlStatement);
			st.executeQuery();
			ResultSet rs = st.executeQuery();
			if(rs.next())
			    result = rs.getString(1);
			rs.close();
			st.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return result;
	}
	
	public ArrayList<ArrayList<String>> getArrayListStringFromQuery(String sqlStatement){
		ArrayList<ArrayList<String>> resultArrayList = new ArrayList<ArrayList<String>>();
		try {
			PreparedStatement st = conn.prepareStatement(sqlStatement);
			st.executeQuery();
			ResultSet rs = st.executeQuery();
			resultArrayList = extract(rs);
			rs.close();
			st.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return resultArrayList;
	}
	
	
	public static ArrayList<ArrayList<String>> extract(ResultSet resultSet)
			throws SQLException {
		ArrayList<ArrayList<String>> table;
		int columnCount = resultSet.getMetaData().getColumnCount();

		if (resultSet.getType() == ResultSet.TYPE_FORWARD_ONLY)
			table = new ArrayList<ArrayList<String>>();
		else {
			resultSet.last();
			table = new ArrayList<ArrayList<String>>(resultSet.getRow());
			resultSet.beforeFirst();
		}

		for (ArrayList<String> row; resultSet.next(); table.add(row)) {
			row = new ArrayList<String>(columnCount);

			for (int c = 1; c <= columnCount; ++c)
				row.add(resultSet.getString(c).intern());
		}
		return table;
	}
	
	public Vector<String> getMonitorIdForDefects(){

        Vector<String> ts_monitor_id = new Vector<String>();
        try {
            PreparedStatement st = conn.prepareStatement("select ts_monitor_id from ts_defects");
            //st.setLong(1, ts_id);
            ResultSet rs = st.executeQuery();
            while(rs.next())
            {
                ts_monitor_id.add(rs.getString("ts_monitor_id")+"   ");
            }
            rs.close();
            st.close();
            } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ts_monitor_id;
    }
    
}
