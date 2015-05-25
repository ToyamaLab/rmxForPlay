package logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.PropfileDao;
import data.RmxConstant;
import logic.parse.Distributor;

public class SmtpListener implements Runnable{
	
	//メンバ変数
	private static final Logger log = LoggerFactory.getLogger(SmtpListener.class);
	public static PropfileDao envDao;
	private int PORT;
	private ServerSocket sSocket = null;
	private Socket socket = null;
	
	// コンストラクタ
	public SmtpListener() {
		try {
			envDao = new PropfileDao(RmxConstant.ENV_PROPERTY_NAME.getValue()); 
			this.PORT = envDao.getInteger("RECEIVE_PORT");
		} catch (NumberFormatException e) {
			log.error("# Error: " + e.toString());
			System.exit(-1);
		} catch (MissingResourceException e) {
			log.error("# Error: " + e.toString());
			System.exit(-1);
		}
	}
	
	@Override
	public void run() {
		this.smtpListenerController();
	}
	
	//ソケットを開き、システムをスタート
	public void smtpListenerController() {
		try {
			sSocket = new ServerSocket(PORT);
			log.info("RMX System begin");
			log.debug("-----START<<PORT_NUM = "+PORT+">>-----");
			
			while(true) {
				socket = sSocket.accept();
				log.debug("S :Accepted: (" + socket.getInetAddress().getHostName() + ")");

				Thread t = new Thread(new Distributor(socket));
				t.start();
			}
		}catch (SecurityException e) {
			log.error("# Error: " + e.toString());
			System.exit(-1);
		}catch (IOException e) {
			log.error("# Error: " + e.toString());
			System.exit(-1);
		}catch (IllegalArgumentException e) {
			log.error("# Error: " + e.toString());
			System.exit(-1);	
		}finally {
			try {
				if (sSocket != null)
					sSocket.close();
			} catch (IOException e) {
				log.error("# Error: " + e.toString());
				System.exit(-1);
			}
		}
	}
}
