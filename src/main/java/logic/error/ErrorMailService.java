package logic.error;

import data.Message;
/*
 * 受信メールがRMX形式およびプラグインにも対応していないとき
 */
public class ErrorMailService {
	// メンバ変数
	private Exception error;
	private Message oMsg;
	
	// コンストラクタ
	public ErrorMailService(Exception error, Message oMsg) {
		this.error = error;
		this.oMsg = oMsg;
	}
	
	/**
	 * 
	 * */
	public void sendErrorMessage() {
		Message eMsg;
		
		if(error.getClass().toString().equalsIgnoreCase("java.util.MissingResourceException"))
			eMsg = this.getMissingResourceExceptionMessage();
		else if(error.getClass().toString().equalsIgnoreCase("java.lang.ClassNotFoundException"))
			eMsg = this.getClassNotFoundExceptionMessage();
		else if(error.getClass().toString().equalsIgnoreCase("java.sql.SQLException"))
			eMsg = this.getSQLExceptionMessage();
		else if(error.getClass().toString().equalsIgnoreCase("logic.parse.SOP.ParseException"))
			eMsg = this.getParseExceptionMessage();
	}
	
	/**
	 * 
	 * */
	private Message getMissingResourceExceptionMessage() {
		Message eMsg = new Message();
		
		eMsg.setRecipient(oMsg.getRecipient());
		eMsg.setSender(oMsg.getRecipient());
		for(int i=0;i<oMsg.getHeader().size();i++)
			eMsg.addHeader(oMsg.getHeader().get(i));
		return null;
	}
	
	/**
	 * 
	 * */
	private Message getClassNotFoundExceptionMessage() {
		Message eMsg = new Message();
		
		eMsg.setRecipient(oMsg.getRecipient());
		eMsg.setSender(oMsg.getRecipient());
		for(int i=0;i<oMsg.getHeader().size();i++)
			eMsg.addHeader(oMsg.getHeader().get(i));
		return null;
	}
	
	/**
	 * 
	 * */
	private Message getSQLExceptionMessage() {
		Message eMsg = new Message();
		
		eMsg.setRecipient(oMsg.getRecipient());
		eMsg.setSender(oMsg.getRecipient());
		for(int i=0;i<oMsg.getHeader().size();i++)
			eMsg.addHeader(oMsg.getHeader().get(i));
		return null;
	}
	
	/**
	 * 
	 * */
	private Message getParseExceptionMessage() {
		Message eMsg = new Message();
		
		eMsg.setRecipient(oMsg.getRecipient());
		eMsg.setSender(oMsg.getRecipient());
		for(int i=0;i<oMsg.getHeader().size();i++)
			eMsg.addHeader(oMsg.getHeader().get(i));
		return null;
	}
}
