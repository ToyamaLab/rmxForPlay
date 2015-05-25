package logic.interfaces;

import java.util.ArrayList;

import logic.api.JsonOutputModel;

public interface APIInterface {
	public JsonOutputModel getJson(String jsonIn, String domain);
	public ArrayList<String> getType();
}
