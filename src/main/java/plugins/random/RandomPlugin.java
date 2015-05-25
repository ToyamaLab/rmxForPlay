package plugins.random;

import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

import logic.interfaces.PluginInterface;
import logic.parse.User;
import data.Message;

public class RandomPlugin implements PluginInterface{

	@Override
	public ArrayList<Message> pluginStart(
			Message oMsg,
			ArrayList<String> recipients,
			User user) {

		ArrayList<Message> sMsgs = new ArrayList<Message>();
		String command = user.getCommand();
		ArrayList<String> commandArgs = user.getCommandArgs();
		if(command.equalsIgnoreCase("shuffle")) {
			Random rnd = new Random();
			int ran = rnd.nextInt(Integer.parseInt(commandArgs.get(0)));
			
			for(int i=0;i<recipients.size();i++) {
				Message sMsg = new Message();
				sMsg.setRecipient(recipients.get(i));
				sMsg.setSender(oMsg.getSender());
				sMsg.setSubject("random number");
				sMsg.addBody(Integer.toString(ran));
				sMsg.addBody(".");
				for(int j=0;j<oMsg.getHeader().size();j++) {
					sMsg.addHeader(oMsg.getHeader().get(j));
				}
				
				sMsgs.add(sMsg);
			}
		}else {
			Message sMsg = new Message();
			sMsg.setRecipient(oMsg.getSender());
			sMsg.setSender(oMsg.getSender());
			sMsg.setSubject("error");
			sMsg.addBody("none command");
			sMsg.addBody(".");
			
			sMsgs.add(sMsg);
		}
		
		return sMsgs;
	}

	@Override
	public ArrayList<String> getAvailableFunctionNames() {
		String function = "random";
		ArrayList<String> functions = new ArrayList<String>();
		functions.add(function);
		return functions;
	}

}
