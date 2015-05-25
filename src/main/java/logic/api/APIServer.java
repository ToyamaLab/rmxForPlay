package logic.api;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import presentation.api.APIReader;
import presentation.api.APIWriter;
import logic.APIListener;
import logic.interfaces.APIInterface;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dao.PropfileDao;

public class APIServer implements Runnable{
	// メンバー変数
	private Socket socket;
	private String jsonIn;
	private String jsonOut;
	private PropfileDao envDao;
	
	// コンストラクタ
	public APIServer(Socket socket) {
		this.socket = socket;
		jsonIn = null;
		jsonOut = null;
		envDao = APIListener.envDao;
	}
	
	@Override
	public void run() {
		this.apiStart();
	}
	
	/**
	 * 
	 * */
	private void apiStart() {
		try {
			// クライント側のIPアドレスが正しいかどうかチェック
			if(!this.isCorrectIP()) 
				socket.close();
			
			// ポートを通してJSON形式の文字列を取得
			APIReader in = new APIReader(socket);
			jsonIn = in.readLine();
			
			// ツリーモデルへ変換する
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readValue(jsonIn, JsonNode.class);
			
			// typeノードを取り出して値を取り出す
			String type = rootNode.get("type").asText();
			
			String domain = rootNode.get("domain").asText();
				
			// typeからプラグインを選択
			APIPluginsHolder plugins = new APIPluginsHolder(type);
			APIInterface plugin = plugins.selectPlugin();
			
			// argsノードをJSON文字列としてプラグインへ渡す
			JsonOutputModel jsonOutObj = plugin.getJson(mapper.writeValueAsString(rootNode.get("args")), domain);
			
			// 返すJSONを得る
			jsonOut = mapper.writeValueAsString(jsonOutObj);
			
			// 送る
			APIWriter out = new APIWriter(socket);
			out.println(jsonOut);
			
		} catch (JsonParseException e) {
			// TODO: handle exception
		} catch (JsonMappingException e) {
			// TODO: handle exception
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
					
			}
		}
	}
	
	/**
	 * 
	 * */
	private boolean isCorrectIP() {
		List<String> IPs = envDao.getValueArray("API_IP");
		
		if(IPs.contains(socket.getInetAddress().getHostAddress().toString()))
			return true;
		else
			return false;
	}
}
