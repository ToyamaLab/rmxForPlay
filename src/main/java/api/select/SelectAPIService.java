package api.select;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import logic.api.JsonOutputModel;
import logic.parse.User;
import dao.DBDao;
import dao.PropfileDao;
import data.RmxAPIStatus;
import data.RmxConstant;

public class SelectAPIService {
	// メンバ変数
	private static final Logger log = LoggerFactory.getLogger(SelectAPIService.class);
	private String jsonIn;
	private String domain;
	private EmailModel model;
	private PropfileDao envDao;
	
	public SelectAPIService(String jsonIn, String domain) {
		this.jsonIn = jsonIn;
		this.domain = domain;
		envDao = new PropfileDao(RmxConstant.ENV_PROPERTY_NAME.getValue());
	}
	
	public JsonOutputModel getSelectAPIJson() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			model = mapper.readValue(jsonIn, EmailModel.class);
		} catch (JsonParseException e) {
			return this.getErrorAPIJson(RmxAPIStatus.CLIENT_ERROR);
		} catch (JsonMappingException e) {
			return this.getErrorAPIJson(RmxAPIStatus.CLIENT_ERROR);
		} catch (IOException e) {
			return this.getErrorAPIJson(RmxAPIStatus.CLIENT_ERROR);
		}
		
		String recipient = model.email;

		User user = new User(envDao);
		user.UserStart(recipient);
		
		if(!user.getSundomain().equalsIgnoreCase(domain))
			return this.getErrorAPIJson(RmxAPIStatus.CLIENT_ERROR);
		
		if(user.getNormalFlg())
			return this.getCorrectAPIJson(user);
		else if(user.getErrorObj().toString().equalsIgnoreCase("java.util.MissingResourceException"))
			return this.getErrorAPIJson(RmxAPIStatus.INTERNAL_SERVER_ERROR);
		else if(user.getErrorObj().toString().equalsIgnoreCase("java.lang.ClassNotFoundException"))
			return this.getErrorAPIJson(RmxAPIStatus.INTERNAL_SERVER_ERROR);
		else if(user.getErrorObj().toString().equalsIgnoreCase("java.sql.SQLException"))
			return this.getErrorAPIJson(RmxAPIStatus.DB_ACCESS_ERROR);
		else if(user.getErrorObj().toString().equalsIgnoreCase("logic.parse.SOP.ParseException"))
			return this.getErrorAPIJson(RmxAPIStatus.SYNTAX_ERROR);
		else if(user.getFunctionFlg())
			return this.getErrorAPIJson(RmxAPIStatus.SYNTAX_ERROR);
		else
			return this.getErrorAPIJson(RmxAPIStatus.SYNTAX_ERROR);
	}
	
	/**
	 * 
	 * */
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
		try {
			JsonOutputModel jsonOutObj = new JsonOutputModel();
			RmxAPIStatus status = RmxAPIStatus.OK;
			jsonOutObj.setCode(status.getCode());
			jsonOutObj.setMessage(status.getMessage());
			
			String query = user.getQuery();
			ArrayList<Object> params = user.getPara();
			
			DBDao db = new DBDao(user.getDomainMap());
			ResultSet rs = db.read(query, params);
			
			List<EmailModel> emails = new ArrayList<EmailModel>();
			
			while(rs.next()) {
				EmailModel emailModel = new EmailModel();
				emailModel.email = rs.getString(1);
				emails.add(emailModel);
			}
			
			jsonOutObj.setRetval(emails);
			
			return jsonOutObj;
		} catch (ClassNotFoundException e) {
			log.error("# Error: " + e.toString());
			return getErrorAPIJson(RmxAPIStatus.DB_CLASS_NOT_FOUND_ERROR);
		} catch (SQLException e) {
			log.error("# Error: " + e.toString());
			return getErrorAPIJson(RmxAPIStatus.DB_ACCESS_ERROR);
		}
	}
}
