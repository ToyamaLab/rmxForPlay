package api.list;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import logic.api.JsonOutputModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.DBDao;
import dao.PropfileDao;
import data.RmxAPIStatus;
import data.RmxConstant;
import data.RmxQuery;

public class ListAPIService {
	// メンバ変数
	private static final Logger log = LoggerFactory.getLogger(ListAPIService.class);
	private String jsonIn;
	private String domain;
	private EmailModel model;
	private PropfileDao envDao;
	
	public ListAPIService(String jsonIn, String domain) {
		this.jsonIn = jsonIn;
		this.domain = domain;
		envDao = new PropfileDao(RmxConstant.ENV_PROPERTY_NAME.getValue());
	}
	
	public JsonOutputModel getListAPIJson() {
		try {
			DBDao db = new DBDao(
					envDao.getString("DB_DRIVER"), 
					envDao.getString("DB_URL"), 
					envDao.getString("DB_ID"), 
					envDao.getString("DB_PASSWORD"));
			
			String query = RmxQuery.LIST_API_QUERY.getQuery();
			ArrayList<Object> params = new ArrayList<>();
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
			
			List<EmailModel> emails = new ArrayList<EmailModel>();
			
			while(rs.next()) {
				EmailModel email = new EmailModel();
				email.id = rs.getInt(1);
				email.recipient = rs.getString(2);
				email.sender = rs.getString(3);
				email.subdomain = rs.getString(4);
				email.subject = rs.getString(5);
				email.transmitted = rs.getDate(6);
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
