package logic.flow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import logic.SmtpListener;
import logic.interfaces.PluginInterface;
import logic.parse.User;
import logic.plugin.PluginsHolder;
import logic.service.FlowService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import presentation.mail.SendMailService;
import dao.PropfileDao;
import data.Message;

public class FunctionFlow {
	//メンバ変数
	private Message oMsg;
	private PropfileDao envDao;

	private static final Logger log = LoggerFactory.getLogger(FunctionFlow.class);
	private User user;
	private User funcUser;
	
	//コンストラクタ
	public FunctionFlow(Message oMsg, User user) {
		this.oMsg = oMsg;
		this.user = user;
		envDao = SmtpListener.envDao;
	}
	
	public void startFunction() throws ClassNotFoundException, SQLException {
		// 1. function,command,commandArgs,recipientsを得る
		//ex.#random.shuffle.50#team{rmx}@example.comのとき
		//function->random,command->shuffle,commandArgs->[50],recipients-[kita@~,matt@~,…]
		String function = user.getFunction();
		
		// 2. 2つめの#以降を切り取り、targetがあれば再びパースし、そうでなければ送信者が宛先になる
		// (ex)#~#team{rmx}@keio.com -> team{rmx}@keio.com
		// (ex)#~#@keio.com -> @keio.com
		ArrayList<String> recipients = new ArrayList<String>();
		String target = user.getTarget();
		if(target.indexOf("@")==0)//@keio.comのとき
			recipients.add(oMsg.getSender());
		else {//team{rmx}@keio.comのとき
			funcUser = new User(envDao);
			funcUser.UserStart(target);
			recipients = FlowService.getRecipients(funcUser);
		}
		
		// 3. src/main/java/pluginsの中にあるプラグインを全て入手
		PluginsHolder pHolder = new PluginsHolder();
		ArrayList<PluginInterface> plugins = pHolder.holdPlugins();
		
		// 4. function名に合ったプラグイン1つを入手
		PluginInterface plugin = pHolder.selectPlugin(plugins, function);
		System.out.println("@1"+function);
		
		// 5. 送信用メッセージを作成
		ArrayList<Message> sMsgs = new ArrayList<Message>();
		// 6. 送信用メッセージを得る
		sMsgs = plugin.pluginStart(oMsg, recipients, user);
		
		// 7. メールの送信
		log.info("Mail:{} -> {}", oMsg.getSender(), oMsg.getRecipient());
		SendMailService sm = new SendMailService();
		for(int i=0;i<sMsgs.size();i++)
			sm.sendMail(sMsgs.get(i), envDao);
	}
}
