package api.send;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import api.select.SelectAPIService;
import data.RmxAPIStatus;
import logic.api.JsonOutputModel;
import logic.interfaces.APIInterface;

public class SendAPI implements APIInterface{

	@Override
	public JsonOutputModel getJson(String jsonIn, String domain) {
		SendAPIService sendAPI = new SendAPIService(jsonIn, domain);
		return sendAPI.getSendAPIJson();
	}

	@Override
	public ArrayList<String> getType() {
		ArrayList<String> types = new ArrayList<String>();
		types.add("send");
		return types;
	}
}
