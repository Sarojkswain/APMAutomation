/**
 * 
 */
package com.ca.tas.flow.tess.reports;


/**
 * @author keyja01
 *
 */
public class TessReportSchedule {
    public static final String DEFAULT_CEM_REPORT_FROM_EMAIL_ADDRESS = "support@ca.com"; 
    public static final String DEFAULT_CEM_REPORT_TO_EMAIL_ADDRESS = "somebody@ca.com";

    public Period period = Period.Daily;
    public DayOfWeek dayOfWeek = DayOfWeek.Unknown;
    public int dayOfMonth = 1;
    /** 0..23 */
    public int hour = 0;
    /** 0..59 */
    public int minute = 0;
    
    public String fromAddress = DEFAULT_CEM_REPORT_FROM_EMAIL_ADDRESS;
    public String toAddress = DEFAULT_CEM_REPORT_TO_EMAIL_ADDRESS;
    public String fromName  = "CEM Administrator";
    public String subject = "CA CEM Report";
    public String message = "This email was configured as part of the FLD test";

    public static enum Period {
        Daily("0"), Weekly("1"), Monthly("2");
        private String code;

        private Period(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    public static enum DayOfWeek {
        Unknown("0"), Sunday("1"), Monday("2"), Tuesday("3"), Wednesday("4"), Thursday("5"), Friday("6"), Saturday("7");
        private String code;

        private DayOfWeek(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
        
    }
    
    public static TessReportSchedule daily(int hour, int minute) {
        TessReportSchedule r = new TessReportSchedule();
        r.period = Period.Daily;
        r.hour = hour;
        r.minute = minute;
        
        return r;
    }
    
    public static TessReportSchedule weekly(DayOfWeek dayOfWeek, int hour, int minute) {
        TessReportSchedule r = new TessReportSchedule();
        r.period = Period.Weekly;
        r.dayOfWeek = dayOfWeek;
        r.hour = hour;
        r.minute = minute;
        
        return r;
    }
    
    public static TessReportSchedule monthly(int dayOfMonth, int hour, int minute) {
        TessReportSchedule r = new TessReportSchedule();
        r.period = Period.Monthly;
        r.dayOfMonth = dayOfMonth;
        r.hour = hour;
        r.minute = minute;
        
        return r;
    }

	@Override
	public String toString() {
		return "TessReportSchedule [period=" + period + ", dayOfWeek="
				+ dayOfWeek + ", dayOfMonth=" + dayOfMonth + ", hour=" + hour
				+ ", minute=" + minute + ", fromAddress=" + fromAddress
				+ ", toAddress=" + toAddress + ", fromName=" + fromName
				+ ", subject=" + subject + ", message=" + message + "]";
	}
    
    
}
