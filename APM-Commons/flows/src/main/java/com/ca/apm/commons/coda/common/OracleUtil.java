package com.ca.apm.commons.coda.common;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OracleUtil extends DBUtil{
	
    public static final  Logger log = Logger.getLogger(OracleUtil.class.getName());
	private Connection conn;
	
	public OracleUtil(String host, String port, String db, String user, String pwd){
		//load driver
		try{
			Driver driver = (Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			DriverManager.registerDriver(driver);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//Build connection url
		String url = "jdbc:oracle:thin:@"+host+":"+port+"/"+db;
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
	
	public void setUpgradeToTimMonitoring(String value){
		try{
			PreparedStatement st = conn.prepareStatement("update ts_settings set ts_value = ? where ts_key = 'upgradeToTimMonitoring'");
			st.setString(1, value);
			st.executeUpdate();
			st.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public String getUpgradeToTimeMonitoring(){
		String upgrade = "false";
		try {
			PreparedStatement st = conn.prepareStatement("SELECT ts_value from ts_settings where ts_key = 'upgradeToTimMonitoring'");
			ResultSet rs = st.executeQuery();
			rs.next();
			upgrade = rs.getString("ts_value");
			rs.close();
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return upgrade;
	}

	public String isUserDeleted(Integer pId){
		String deleted = "false";
		try {
			PreparedStatement st = conn.prepareStatement("SELECT ts_soft_delete from ts_users where ts_id = ?");
			st.setInt(1, pId);
			st.executeQuery();
			ResultSet rs = st.executeQuery();
			rs.next();
			deleted = rs.getString("ts_value");
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
	
    public String areDefectsPresent(java.util.Date Timestamp) {
        String result = null;
        try {
            //PreparedStatement st = conn.prepareStatement("SELECT CURRENT_TIMESTAMP FROM DUAL");
            PreparedStatement st = conn.prepareStatement("select count(TS_OCCUR_DATE) from ts_defects where TS_OCCUR_DATE > TO_TIMESTAMP('?','mm/dd/yyyy HH:MI:SS.FF AM'");
            
            st.setDate(1, (Date) Timestamp);

            st.executeQuery();
            ResultSet rs = st.executeQuery();
            if (rs.next()) result = rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }



	public List<String> getAgents(String sqlStatement) {
		List<String> metrics = new ArrayList<String>();

		try {
			//PreparedStatement st = conn.prepareStatement("SELECT CURRENT_TIMESTAMP FROM DUAL");
			PreparedStatement st = conn.prepareStatement("select AGENT_NAME from wt_agent");
			st.executeQuery();
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				metrics.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metrics;
	}

	public List<String> getIntegerMaxMetricsFromOracle(String sqlStatement) {
		List<String> metrics = new ArrayList<String>();

		try {
			//PreparedStatement st = conn.prepareStatement("SELECT CURRENT_TIMESTAMP FROM DUAL");
			PreparedStatement st = conn.prepareStatement("select * from wt_metric");
			st.executeQuery();
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				metrics.add(rs.getString(1)+"::"+rs.getString(14));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metrics;
	}

	public List<String> getOracleSmartStorData(String sqlStatement) {
		List<String> metrics = new ArrayList<String>();

		try {
			//PreparedStatement st = conn.prepareStatement(sqlStatement);
			PreparedStatement st = conn.prepareStatement(sqlStatement);
			st.executeQuery();
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				metrics.add(rs.getString(1)+"::"+rs.getString(2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metrics;
	}


	public void deleteAllSmartStorData(){
		try{
			PreparedStatement st7 = conn.prepareStatement("delete from WT_AGENT");
			PreparedStatement st1 = conn.prepareStatement("delete from WT_RESOURCE");
			PreparedStatement st2 = conn.prepareStatement("delete from WT_METRIC_NAME");
			PreparedStatement st3 = conn.prepareStatement("delete from WT_METRIC");
			PreparedStatement st4 = conn.prepareStatement("delete from WT_METADATA");
			PreparedStatement st5 = conn.prepareStatement("delete from WT_RESOURCE_METRIC");
			PreparedStatement st6 = conn.prepareStatement("delete from WT_RECORD_TYPE");
			st1.execute();
			st1.close();
            st2.execute();
            st2.close();
            st3.execute();
            st3.close();
            st4.execute();
            st4.close();
            st5.execute();
            st5.close();
            st6.execute();
            st6.close();
            st7.execute();
            st7.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}


	public boolean areDefectsPresent(String TIMESTAMP) {
        boolean result = false;
        
        try {

           /* st =
                conn.prepareStatement("select count(TS_OCCUR_DATE) from ts_defects where TS_OCCUR_DATE > TO_TIMESTAMP ('"
                    + TIMESTAMP
                    + "','mm/dd/yyyy HH:MI:SS.FF AM') AND TS_OCCUR_DATE < TO_TIMESTAMP ('"
                    + result
                    + "','mm/dd/yyyy HH:MI:SS.FF AM');");*/
            Statement st =
                conn.createStatement();

            //ResultSet rs = st.executeQuery("select count(TS_OCCUR_DATE) from ts_defects where TS_OCCUR_DATE > TO_TIMESTAMP ('09/09/2015 05:19:35.000000002 PM','mm/dd/yyyy HH:MI:SS.FF AM')");
            ResultSet rs = st.executeQuery("select count(TS_OCCUR_DATE) from ts_defects where TS_OCCUR_DATE > TO_TIMESTAMP ('"+TIMESTAMP+"','mm/dd/yyyy HH:MI:SS.FF AM')");
            if(rs.next())
                if(rs.getInt(1)>0){
                    result=true;
                }

            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return result;
    }
}
