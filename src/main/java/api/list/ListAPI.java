package api.list;

import java.util.ArrayList;

import logic.api.JsonOutputModel;
import logic.interfaces.APIInterface;

public class ListAPI implements APIInterface{

	@Override
	public JsonOutputModel getJson(String jsonIn, String domain) {
		ListAPIService listAPI = new ListAPIService(jsonIn, domain);
		
		return listAPI.getListAPIJson();
	}

	@Override
	public ArrayList<String> getType() {
		ArrayList<String> types = new ArrayList<String>();
		types.add("list");
		return types;
	}

}
