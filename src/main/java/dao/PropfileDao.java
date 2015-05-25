package dao;

import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import dao.PropfileDao;

public class PropfileDao{
	// メンバ変数
	private ResourceBundle rb;
	
	// コンストラクタ
	public PropfileDao(String bundleName) throws MissingResourceException{
		rb = ResourceBundle.getBundle(bundleName);
	}
	
	/**
	 * 
	 * */
	public String getString(String key) throws MissingResourceException{
		return rb.getString(key);
	}
	
	/**
	 * 
	 * */
	public int getInteger(String key) throws MissingResourceException{
		return Integer.parseInt(rb.getString(key));
	}
	
	/**
	 * 
	 * */
	public List<String> getValueArray(String key) throws MissingResourceException{
		String[] vals = getString(key).split(",");
		
		for(int i=0;i<vals.length;i++)
			vals[i] = vals[i].trim();
		
		return Arrays.asList(vals);
	}
}
