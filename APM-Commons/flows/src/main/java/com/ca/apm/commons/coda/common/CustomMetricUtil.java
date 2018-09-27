package com.ca.apm.commons.coda.common;

import java.util.*;

import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;

public class CustomMetricUtil extends MetricUtil
{


    public CustomMetricUtil(String absoluteMetricPath, CLWBean clw)
    {
        super(absoluteMetricPath,clw);        
    }

        public boolean metricExists()
    {
        return getMetricValue() != null;
    }

       public List<String[]> getLastNMinutesMetricValuesWithTimestamp(int n)
    {
    	List<String[]> metricDataWithTimestamp = null;
        String agentRegularExpression = (new StringBuilder("\".*")).append(escapeSymbols(getAgent())).append(".*\"").toString();
        String metricRegularExpression = (new StringBuilder("\\\"")).append(getMetric().replace("(", "\\(").replace(")", "\\)").replace("%", "\\%")).append("\\\"").toString();
        metricRegularExpression = metricRegularExpression.replaceAll("\\|", "\\\\|").replace(":", "\\:").replace("?", "\\?");
        if(getClw() != null)
        {
            String command = (new StringBuilder("get historical data from agents matching ")).append(agentRegularExpression).append(" and metrics matching ").append(metricRegularExpression).append(" for past ").append(n).append(" minutes with frequency of 15 seconds").toString();
            System.out.println("CLW Query: " + command);
            String clwOutputStrings[] = (String[])null;
            try
            {
                clwOutputStrings = getClw().runCLW(command);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            metricDataWithTimestamp = getMetricDataFromResultSetWithTimestamp(clwOutputStrings);
            
        }
               return metricDataWithTimestamp;
    }

    private int getValueTypeColumn(String temp)
    {
        String values[] = temp.split(",");
        for(int i = 0; i < values.length; i++)
            if(values[i].equals("Value Type"))
                return i;

        return 12;
    }

    private int getMetricValueColumn(String temp, int valueTypeColumn)
    {
        String tmpArray[] = temp.split(",");
        if(tmpArray[valueTypeColumn].equals("Integer"))
            return 13;
        if(tmpArray[valueTypeColumn].equals("Long"))
            return 13;
        if(tmpArray[valueTypeColumn].equals("Float"))
            return 16;
        if(tmpArray[valueTypeColumn].equals("String"))
            return 19;
        return !tmpArray[valueTypeColumn].equals("Date") ? 13 : 20;
    }

    private List<String[]> getMetricDataFromResultSetWithTimestamp(String temp[])
    {
        int valueTypeColumn = getValueTypeColumn(temp[0]);
        List<String[]> listOutput = new ArrayList<String[]>();
        for(int i = 1; i < temp.length; i++)
            if(!temp[i].startsWith("Domain, Host, Process, AgentName, Resource, MetricName, Record Type,"))
            {
                int metricValueColumn = getMetricValueColumn(temp[i], valueTypeColumn);
                String tmpArray[] = temp[i].split(",");
                if(tmpArray.length > 21 && metricValueColumn == 19){
                	String[] arr = new String[3];
                	arr[0] = temp[i].substring(nthIndexOf(temp[i], ',', 19) + 1, temp[i].lastIndexOf(","));
                	arr[1] = tmpArray[9];
                	arr[2] = tmpArray[10];
                    listOutput.add(arr);
                }
                else
                if(tmpArray.length > metricValueColumn){
                	String[] arr = new String[3];
                	arr[0] = tmpArray[metricValueColumn];
                	arr[1] = tmpArray[9];
                	arr[2] = tmpArray[10];
                    listOutput.add(arr);
                }
            }

        return listOutput;
    }

    private static int nthIndexOf(String str, char delimiter, int n)
    {
        for(int i = 0; i < str.length(); i++)
            if(str.charAt(i) == delimiter && --n == 0)
                return i;

        return -1;
    }
}

    