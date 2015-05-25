package logic.flow;

import java.sql.SQLException;
import java.util.ArrayList;

import presentation.mail.SendMailService;
import logic.SmtpListener;
import logic.parse.User;
import logic.service.FlowService;
import dao.PropfileDao;
import data.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferFlow {
	//メンバ変数
	private Message oMsg;
	private PropfileDao envDao;
	private User user;
	private static final Logger log = LoggerFactory.getLogger(TransferFlow.class);
	
	//コンストラクタ
	public TransferFlow(Message oMsg, User user) {
		this.oMsg = oMsg;
		this.envDao = SmtpListener.envDao;
		this.user = user;
	}
	
	public void startTransfer() throws ClassNotFoundException, SQLException{
		// 1. 送信用メッセージ
		ArrayList<Message> sMsgs = FlowService.getTransferMails(user, oMsg);
		
		for(int i=0;i<sMsgs.size();i++) {
			System.out.println(sMsgs.get(i).getRecipient());
		}
		
		// 2. メールの送信
		log.info("Mail:{} -> {}", oMsg.getSender(), oMsg.getRecipient());
		SendMailService sm = new SendMailService();
		for(int i=0;i<sMsgs.size();i++)
			sm.sendMail(sMsgs.get(i), envDao);
	}
}
