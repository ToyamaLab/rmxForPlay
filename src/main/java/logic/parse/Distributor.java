package logic.parse;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import presentation.mail.IncomingMailService;
import dao.PropfileDao;
import data.Message;
import logic.SmtpListener;
import logic.flow.FunctionFlow;
import logic.flow.TransferFlow;
import logic.parse.SOP.parserVisitor;
import logic.service.CheckTypeService;

public class Distributor implements Runnable{
	private String recipient;
	
	/**  */
	private PropfileDao envDao;
	
	/**subdomain,domain,prop_file's map*/
	private Socket socket;
	private static final Logger log = LoggerFactory.getLogger(Distributor.class);

	/**
	 * tree parser created by obunai
	 */
	private User user;
	
	/** message type flag */
	private boolean transferFlg;
	private boolean answerFlg;
	private boolean functionFlg;
	
	//コンストラクタ
	public Distributor(Socket socket) {
		this.envDao = SmtpListener.envDao;
		this.socket = socket;
		this.user = new User(envDao);
	}
	
	@Override
	public void run() {
		parse();
	}
	
	//宛先に応じて処理を振り分ける
	public void parse() {
		try {
			//送られてきたメールをオブジェクトとして得る
			Message oMsg = new Message();
			IncomingMailService icm = new IncomingMailService(socket);
			oMsg = icm.getMessage();
			
			// 宛先を得る
			recipient = oMsg.getRecipient();
			
			// 宛先の構文解析
			user.UserStart(recipient);

			// エラーが存在するとき
			if(user.getErrorObj()!=null) {
				
			}
			
			// エラーが存在しないとき
			else {
				// 関数もしくは自然形式の宛先のとき
				if(user.getNormalFlg()) {
					CheckTypeService check = new CheckTypeService(user.getKeys(), user.getRuleMaps());
					transferFlg = check.checkForTransfer();
					answerFlg = check.checkForAnswer();
				}
				
				//#形式のとき
				else if(user.getFunctionFlg()) {
					functionFlg = true;
				}
				
				//それぞれのフラグに応じてflowへ飛ばす
				if(transferFlg) {
					TransferFlow t_flow = new TransferFlow(oMsg, user);
					t_flow.startTransfer();
				}else if(functionFlg) {
					FunctionFlow f_flow = new FunctionFlow(oMsg, user);
					f_flow.startFunction();
				}else {
					
				}
			}
		} catch(IOException e) {
			log.error("# Error: " + e.toString());
			System.exit(-1);
		} catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
}
