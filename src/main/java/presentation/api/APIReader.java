package presentation.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import logic.SmtpListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIReader {
	// メンバ変数
	private static final Logger log = LoggerFactory.getLogger(APIReader.class);
	private BufferedReader in;
	
	// コンストラクタ
	public APIReader(Socket socket) throws IOException{
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	/***
	 * テキスト行を読み込む
	 * @return
	 */
	public String readLine() throws IOException{
		return in.readLine();		
	}
	
	/**
	 * ストリームを閉じて、リソースを解放する
	 */
	 public void close() {
		 try {
			in.close();
		} catch (IOException e) {
			log.error("# Error: " + e.toString());
		}
	 }
}
