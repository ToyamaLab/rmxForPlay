package api.send;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logic.api.JsonOutputModel;
import logic.flow.FunctionFlow;
import logic.flow.TransferFlow;
import logic.parse.User;
import logic.service.CheckTypeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import api.send.EmailModel;
import dao.PropfileDao;
import data.Header;
import data.Message;
import data.RmxAPIStatus;
import data.RmxConstant;

public class SendAPIService {
	// メンバ変数
	private static final Logger log = LoggerFactory.getLogger(SendAPIService.class);
	private String jsonIn;
	private String domain;
	private EmailModel model;
	private PropfileDao envDao;

	/** message type flag */
	private boolean transferFlg = false;
	private boolean answerFlg = false;
	private boolean functionFlg = false;

	public SendAPIService(String jsonIn, String domain) {
		this.jsonIn = jsonIn;
		this.domain = domain;
		envDao = new PropfileDao(RmxConstant.ENV_PROPERTY_NAME.getValue());
	}

	public JsonOutputModel getSendAPIJson() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			model = mapper.readValue(jsonIn, EmailModel.class);
		} catch (JsonParseException e) {
			
		} catch (JsonMappingException e) {
			
		} catch (IOException e) {
			
		}
		
		Message oMsg = new Message();
		Header header = new Header();
		oMsg.setRecipient(model.recipient);
		oMsg.setSender(model.sender);
		oMsg.addHeader(header.getSubject(model.subject));
		oMsg.addHeader(header.getDate());
		oMsg.addHeader(header.getContentType());
		oMsg.addHeader(header.getFrom(model.sender));
		oMsg.addHeader(header.getContentTransferEncode());
		oMsg.addHeader(header.getTo(model.recipient));
		oMsg.addHeader(header.getMessageID());
		oMsg.addHeader(header.getMimeVer());
		oMsg.addBody(model.body);
		oMsg.addBody(".");

		User user = new User(envDao);
		user.UserStart(oMsg.getRecipient());
		
		if(!user.getSundomain().equalsIgnoreCase(domain))
			return this.getErrorAPIJson(RmxAPIStatus.CLIENT_ERROR);

		// エラーが存在するとき
		if(user.getErrorObj()!=null) {
			if(user.getErrorObj().toString().equalsIgnoreCase("java.util.MissingResourceException"))
				return this.getErrorAPIJson(RmxAPIStatus.INTERNAL_SERVER_ERROR);
			else if(user.getErrorObj().toString().equalsIgnoreCase("java.lang.ClassNotFoundException"))
				return this.getErrorAPIJson(RmxAPIStatus.INTERNAL_SERVER_ERROR);
			else if(user.getErrorObj().toString().equalsIgnoreCase("java.sql.SQLException"))
				return this.getErrorAPIJson(RmxAPIStatus.DB_ACCESS_ERROR);
			else if(user.getErrorObj().toString().equalsIgnoreCase("logic.parse.SOP.ParseException"))
				return this.getErrorAPIJson(RmxAPIStatus.SYNTAX_ERROR);
		}

		// エラーが存在しないとき
		else {
			// 関数もしくは自然形式の宛先のとき
			if(user.getNormalFlg()) {
				CheckTypeService check = new CheckTypeService(user.getKeys(), user.getRuleMaps());
				transferFlg = check.checkForTransfer();
				answerFlg = check.checkForAnswer();
			}

			//#形式のとき
			else if(user.getFunctionFlg()) {
				functionFlg = true;
			}

			//それぞれのフラグに応じてflowへ飛ばす
			try {
				if(transferFlg) {
					TransferFlow t_flow = new TransferFlow(oMsg, user);
					t_flow.startTransfer();
				}else if(functionFlg) {
					FunctionFlow f_flow = new FunctionFlow(oMsg, user);
					f_flow.startFunction();
				}
			} catch (ClassNotFoundException e) {
				return this.getErrorAPIJson(RmxAPIStatus.DB_ACCESS_ERROR);
			} catch (SQLException e) {
				return this.getErrorAPIJson(RmxAPIStatus.DB_CLASS_NOT_FOUND_ERROR);
			}
		}
		return this.getCorrectAPIJson(user);
	}

	public JsonOutputModel getErrorAPIJson(RmxAPIStatus status) {
		JsonOutputModel jsonOutObj = new JsonOutputModel();
		jsonOutObj.setCode(status.getCode());
		jsonOutObj.setMessage(status.getMessage());
		jsonOutObj.setRetval(null);
		return jsonOutObj;

	}

	/**
	 * 
	 * */
	private JsonOutputModel getCorrectAPIJson(User user) {
		JsonOutputModel jsonOutObj = new JsonOutputModel();
		RmxAPIStatus status = RmxAPIStatus.OK;
		jsonOutObj.setCode(status.getCode());
		jsonOutObj.setMessage(status.getMessage());
		jsonOutObj.setRetval(null);
		return jsonOutObj;
	}
}
