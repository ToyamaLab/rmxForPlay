package data;

import java.util.Calendar;

public class Header {
	private String[] week_name = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
	private String[] year_name = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;
	private int week;
	
    
    public Header() {
    	Calendar calendar = Calendar.getInstance();
    	year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
        week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }
	
	public String getFrom(String from) {
		return "From: <"+from+">";
	}
	
	public String getTo(String to) {
		return "To: "+to;
	}
	
	public String getSubject(String subject) {
		return "Subject: "+subject;
	}
	
	public String getContentType() {
		return "Content-Type: text/plain; charset=ISO-2022-JP";
	}
	
	public String getContentTransferEncode() {
		return "Content-Transfer-Encoding: 7bit";
	}
	
	public String getMessageID() {
		String rtrn = String.valueOf(year)+String.valueOf(month)
				+String.valueOf(day)+String.valueOf(hour)+String.valueOf(minute)
					+String.valueOf(second)+"@db.ics.keio.ac.jp";
		return "Message-ID: <"+rtrn+">";
	}
	
	public String getDate() {
		String date = week_name[week]+" "+day+" "+year_name[month-1]+" "+year+" "+hour+":"+second+":"+minute+" +900";
		return "Date: "+date;
	}
	
	public String getMimeVer() {
		return "MIME-Version: 1.0";
	}
}
