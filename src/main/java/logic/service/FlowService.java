package logic.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import logic.parse.User;
import dao.DBDao;
import dao.PropfileDao;
import data.Message;

public class FlowService {
	/***/
	
	/** コンストラクタ */
	private FlowService() {}
	
	public static ArrayList<Message> getTransferMails(User user, Message oMsg) throws ClassNotFoundException, SQLException{
		// 1. 宛先用のリスト
		ArrayList<String> recipients = getRecipients(user);
		
		// 2.送信用メールの作成
		ArrayList<Message> sMsgs = new ArrayList<Message>();
		
		// 3.メール作成
		for(int i=0;i<recipients.size();i++) {
			// 4. 1通ごとにオブジェクト作成
			Message sMsg = new Message();
			
			// 5.宛先
			sMsg.setRecipient(recipients.get(i));
			// 6.ヘッダー
			for(int k=0;k<oMsg.getHeader().size();k++)
				sMsg.addHeader(oMsg.getHeader().get(k));
			// 7.タイトル
			sMsg.setSubject(oMsg.getSubject());
			// 8.本文
			for (int j=0;j<oMsg.getBody().size();j++) 
				sMsg.addBody(oMsg.getBody().get(j));
			
			// 9.送信メッセージをリストに挿入
			sMsgs.add(sMsg);
		}
		return sMsgs;
	}
	
//	public static ArrayList<Message> getAnswerMails(User user, ResourceBundle envBundle, Message oMsg){
//		
//	}
	
	
	/**
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * 
	 * */
	public static ArrayList<String> getRecipients(User user) throws ClassNotFoundException, SQLException{
		// クエリとパラメーターを得る
		String query = user.getQuery();
		ArrayList<Object> params = user.getPara();
		
		// 
		DBDao dbdao = new DBDao(user.getDomainMap());
		ResultSet rs = dbdao.read(query, params);
		
		ArrayList<String> recipients = new ArrayList<String>();
		
		try {
			while(rs.next()) {
				recipients.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		return recipients;
	}
}
