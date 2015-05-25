package presentation.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class APIWriter {
	// メンバ変数
	private PrintWriter out;
	// コンストラクタ
	public APIWriter(Socket socket) {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * */
	public void println(String data) {
		out.println(data);
	}
	
	/**
	 * 
	 * */
	public void close() {
		out.close();
	}
}
