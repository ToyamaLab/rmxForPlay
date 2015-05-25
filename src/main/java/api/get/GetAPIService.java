package api.get;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import logic.api.JsonOutputModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dao.DBDao;
import dao.PropfileDao;
import data.RmxAPIStatus;
import data.RmxConstant;
import data.RmxQuery;

public class GetAPIService {
	// メンバ変数
	private static final Logger log = LoggerFactory.getLogger(GetAPIService.class);
	private String jsonIn;
	private String domain;
	private EmailModel model;
	private PropfileDao envDao;
	
	public GetAPIService(String jsonIn, String domain) {
		this.jsonIn = jsonIn;
		this.domain = domain;
		envDao = new PropfileDao(RmxConstant.ENV_PROPERTY_NAME.getValue());
	}
	
	public JsonOutputModel getGetAPIJson() {
		try {
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
			
			int id = model.id;
			
			DBDao db = new DBDao(
					envDao.getString("DB_DRIVER"), 
					envDao.getString("DB_URL"), 
					envDao.getString("DB_ID"), 
					envDao.getString("DB_PASSWORD"));
			
			String query = RmxQuery.GET_API_QUERY.getQuery();
			ArrayList<Object> params = new ArrayList<>();
			params.add(id);
			params.add(domain);
			
			ResultSet rs = db.read(query, params);
			
			return this.getCorrectAPIJson(rs);
		} catch (ClassNotFoundException e) {
			return this.getErrorAPIJson(RmxAPIStatus.INTERNAL_SERVER_ERROR);
		} catch (MissingResourceException e) {
			return this.getErrorAPIJson(RmxAPIStatus.INTERNAL_SERVER_ERROR);
		} catch (SQLException e) {
			return this.getErrorAPIJson(RmxAPIStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private JsonOutputModel getCorrectAPIJson(ResultSet rs) {
		try {
			JsonOutputModel jsonOutObj = new JsonOutputModel();
			RmxAPIStatus status = RmxAPIStatus.OK;
			jsonOutObj.setCode(status.getCode());
			jsonOutObj.setMessage(status.getMessage());
			
			List<RetModel> emails = new ArrayList<RetModel>();
			
			while(rs.next()) {
				RetModel email = new RetModel();
				email.id = rs.getInt(1);
				email.recipient = rs.getString(2);
				email.sender = rs.getString(3);
				email.subdomain = rs.getString(4);
				email.subject = rs.getString(5);
				email.transmitted = rs.getDate(6);
				email.body = rs.getString(7);
				email.header = rs.getString(8);
				emails.add(email);
			}
			
			jsonOutObj.setRetval(emails);
			
			return jsonOutObj;
		} catch (SQLException e) {
			log.error("# Error: " + e.toString());
			return getErrorAPIJson(RmxAPIStatus.DB_ACCESS_ERROR);
		}
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
}
