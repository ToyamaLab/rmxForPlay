package api.select;

import java.util.ArrayList;

import data.RmxAPIStatus;
import logic.api.JsonOutputModel;
import logic.interfaces.APIInterface;

public class SelectAPI implements APIInterface{
	@Override
	public JsonOutputModel getJson(String jsonIn, String domain) {
		SelectAPIService selectAPI = new SelectAPIService(jsonIn, domain);
		
		return selectAPI.getSelectAPIJson();
	}

	@Override
	public ArrayList<String> getType() {
		ArrayList<String> types = new ArrayList<String>();
		types.add("select");
		return types;
	}

}
