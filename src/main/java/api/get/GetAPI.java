package api.get;

import java.util.ArrayList;

import logic.api.JsonOutputModel;
import logic.interfaces.APIInterface;

public class GetAPI implements APIInterface{

	@Override
	public JsonOutputModel getJson(String jsonIn, String domain) {
		GetAPIService getAPI = new GetAPIService(jsonIn, domain);
		return getAPI.getGetAPIJson();
	}

	@Override
	
	public ArrayList<String> getType() {
		ArrayList<String> types = new ArrayList<String>();
		types.add("get");
		return types;
	}

}
