
package com.ca.apm.commons.coda.common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SmartStorResult.java
 * @author: Spencer Tai
 * Date: March 28,2011
 **/

public class SmartStorResult{

	private ArrayList<String> domain = null;
	private ArrayList<String> host = null;
	private ArrayList<String> process= null;
	private ArrayList<String> agentName = null;
	private ArrayList<String> resource = null;
	private ArrayList<String> metricName = null;
	private ArrayList<String> recordType = null;
	private ArrayList<Integer> period = null;
	private ArrayList<Timestamp> intendedEndTimestamp = null;
	private ArrayList<Timestamp> actualStartTimestamp = null;
	private ArrayList<Timestamp> actualEndTimestamp = null;
	private ArrayList<Integer> count = null;
	private ArrayList<Integer> type = null;
	private ArrayList<Long> value = null;
	private ArrayList<Long> min = null;
	private ArrayList<Long> max = null;
	private ArrayList<String> stringValue = null;

	public SmartStorResult(ResultSet rs){
		domain = new ArrayList<String>();
		host = new ArrayList<String>();
		process = new ArrayList<String>();
		agentName = new ArrayList<String>();
		resource = new ArrayList<String>();
		metricName = new ArrayList<String>();
		recordType = new ArrayList<String>();
		period = new ArrayList<Integer>();
		intendedEndTimestamp = new ArrayList<Timestamp>();
		actualStartTimestamp = new ArrayList<Timestamp>();
		actualEndTimestamp = new ArrayList<Timestamp>();
		count = new ArrayList<Integer>();
		type = new ArrayList<Integer>();
		value = new ArrayList<Long>();
		min = new ArrayList<Long>();
		max = new ArrayList<Long>();
		stringValue = new ArrayList<String>();
		try{
			while(rs.next()){
				domain.add(rs.getString(AutomationConstants.SMARTSTOR_COLUMN_DOMAIN));
				host.add(rs.getString(AutomationConstants.SMARTSTOR_COLUMN_HOST));
				process.add(rs.getString(AutomationConstants.SMARTSTOR_COLUMN_PROCESS));
				agentName.add(rs.getString(AutomationConstants.SMARTSTOR_COLUMN_AGENT_NAME));
				resource.add(rs.getString(AutomationConstants.SMARTSTOR_COLUMN_RESOURCE));
				metricName.add(rs.getString(AutomationConstants.SMARTSTOR_COLUMN_METRIC_NAME));
				recordType.add(rs.getString(AutomationConstants.SMARTSTOR_COLUMN_RECORD_TYPE));
				period.add(rs.getInt(AutomationConstants.SMARTSTOR_COLUMN_PERIOD));
				intendedEndTimestamp.add(rs.getTimestamp(AutomationConstants.SMARTSTOR_COLUMN_INTENDED_END_TIMESTAMP));
				actualStartTimestamp.add(rs.getTimestamp(AutomationConstants.SMARTSTOR_COLUMN_ACTUAL_START_TIMESTAMP));
				actualEndTimestamp.add(rs.getTimestamp(AutomationConstants.SMARTSTOR_COLUMN_ACTUAL_END_TIMESTAMP));
				count.add(rs.getInt(AutomationConstants.SMARTSTOR_COLUMN_COUNT));
				type.add(rs.getInt(AutomationConstants.SMARTSTOR_COLUMN_TYPE));
				value.add(rs.getLong(AutomationConstants.SMARTSTOR_COLUMN_VALUE));
				min.add(rs.getLong(AutomationConstants.SMARTSTOR_COLUMN_MIN));
				max.add(rs.getLong(AutomationConstants.SMARTSTOR_COLUMN_MAX));
				stringValue.add(rs.getString(AutomationConstants.SMARTSTOR_COLUMN_STRING_VALUE));
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
	}

	public String getDomain(int index){return domain.get(index);}
	public String getHost(int index){return host.get(index);}
	public String getProcess(int index){return process.get(index);}
	public String getAgentName(int index){return agentName.get(index);}
	public String getResource(int index){return resource.get(index);}
	public String getMetricName(int index){return metricName.get(index);}
	public String getRecordType(int index){return recordType.get(index);}
	public int getPeriod(int index){return period.get(index);}
	public Timestamp getIntendedEndTimestamp(int index){return intendedEndTimestamp.get(index);}
	public Timestamp getActualStartTimestamp(int index){return actualStartTimestamp.get(index);}
	public Timestamp getActualEndTimestamp(int index){return actualEndTimestamp.get(index);}
	public int getCount(int index){return count.get(index);}
	public int getType(int index){return type.get(index);}
	public long getValue(int index){return value.get(index);}
	public long getMin(int index){return min.get(index);}
	public long getMax(int index){return max.get(index);}
	public String getStringValue(int index){return stringValue.get(index);}


	public int size(){
		return metricName.size();
	}

	public String display(){
		String text = "SmartStor Result\n";
		int size = size();
		text = text + "Domain Host Process AgentName Resource MetricName RecordType Period IETimestamp ASTimestamp AETimestamp Count Type Value Min Max StringValue\n";
		for(int i = 0; i< size;i++){
			text = text + domain.get(i) + " " +  host.get(i) + " " + process.get(i) + " " + agentName.get(i) + " " + resource.get(i) + " " + metricName.get(i) + " " + recordType.get(i) + " " + period.get(i) + " " + intendedEndTimestamp.get(i) + " " + actualStartTimestamp.get(i) + " " + actualEndTimestamp.get(i) + " " + count.get(i) + " " + type.get(i) + " " + value.get(i) + " " + min.get(i) + " " + max.get(i) + " " + stringValue.get(i) + "\n";
		}
		return text;
	}
}


/**

	Domain					Host					Process					AgentName				Resource
	java.lang.String		java.lang.String		java.lang.String		java.lang.String		java.lang.String
	DefaultCHAR				DefaultCHAR				DefaultCHAR				DefaultCHAR				DefaultCHAR

	MetricName				Record_Type				Period					Intended_End_Timestamp	Actual_Start_Timestamp
	java.lang.String		java.lang.String		java.lang.Integer		java.sql.Timestamp		java.sql.Timestamp
	DefaultCHAR				DefaultCHAR				DefaultInteger			DefaultTIMESTAMP		DefaultTIMESTAMP

	Actual_End_Timestamp	Count					Type					Value					Min
	java.sql.Timestamp		java.lang.Integer		java.lang.Integer		java.lang.Long			java.lang.Long
	DefaultTIMESTAMP		DefaultINTEGER			DefaultINTEGER			DefaultBIGINT			DefaultBIGINT

	Max						String_Value
	java.lang.Long			java.lang.String
	DefaultBIGINT			DefaultCHAR

**/