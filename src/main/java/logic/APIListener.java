package logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.MissingResourceException;

import logic.api.APIServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.PropfileDao;
import data.RmxConstant;

public class APIListener implements Runnable{
	//メンバ変数
	private static final Logger log = LoggerFactory.getLogger(APIListener.class);
	public static PropfileDao envDao;
	private int PORT;
	private ServerSocket sSocket = null;
	private Socket socket = null;
	
	public APIListener() {
		try {
			envDao = new PropfileDao(RmxConstant.ENV_PROPERTY_NAME.getValue());
			this.PORT = envDao.getInteger("API_PORT");
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
		this.apiListenerController();
	}

	//ソケットを開き、システムをスタート
	public void apiListenerController() {
		try {
			sSocket = new ServerSocket(PORT);
			log.info("RMX System begin");
			log.debug("-----START<<PORT_NUM = "+PORT+">>-----");

			while(true) {
				socket = sSocket.accept();

				Thread t = new Thread(new APIServer(socket));
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
