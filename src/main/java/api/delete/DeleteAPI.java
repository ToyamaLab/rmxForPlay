package api.delete;

import java.util.ArrayList;

import logic.api.JsonOutputModel;
import logic.interfaces.APIInterface;

public class DeleteAPI implements APIInterface{
	@Override
	public JsonOutputModel getJson(String jsonIn, String domain) {
		DeleteAPIService deleteAPI = new DeleteAPIService(jsonIn, domain);
		return deleteAPI.getGetAPIJson();
	}

	@Override
	public ArrayList<String> getType() {
		ArrayList<String> types = new ArrayList<String>();
		types.add("delete");
		return types;
	}
}
