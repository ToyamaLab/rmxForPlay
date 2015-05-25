package logic.interfaces;

import java.util.ArrayList;
import java.util.ResourceBundle;

import logic.parse.User;
import data.Message;

public interface PluginInterface {
	public ArrayList<Message> pluginStart(
			Message oMsg,
			ArrayList<String> recipients,
			User user
			);

	public ArrayList<String> getAvailableFunctionNames();
}
