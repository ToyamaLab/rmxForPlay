package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import logic.SmtpListener;
import logic.parse.*;

public class DBDao {
	private static final Logger log = LoggerFactory.getLogger(DBDao.class);
	private Connection conn = null;
	private ResultSet rs = null;
	private PreparedStatement pstmt = null;
	private Statement stmt = null;
	private String driver;
	private String url;
	private String user;
	private String pass;
	
	/** コンストラクタ 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * */
	public DBDao(String driver, String url, String user, String pass) throws ClassNotFoundException, SQLException {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.pass = pass;
		getConnection();
	}
	
	public DBDao(HashMap<String, String> domainMap) throws ClassNotFoundException, SQLException {
		this.driver = domainMap.get("DB_MANAGER");
		this.url = domainMap.get("DB_URL");
		this.user = domainMap.get("USERNAME");
		this.pass = domainMap.get("PASSWORD");
		getConnection();
	}
	
	/**
     * selectSQLを実行して結果を取得する。
     * @param query
     * @return
	 * @throws SQLException 
     */
	public ResultSet read(String query) throws SQLException {
		return read(query,null);
	}
	
	/**
     * パラメータ化されたselectSQLを実行して結果を取得する。
     * パラメータはrmxでは{String,team,integer,3}など。
     * パラメータは通常でもrmxでも使える。
     * @param query
     * @param params
     * @return
	 * @throws SQLException 
     */
	public ResultSet read(String query, ArrayList<Object> params) throws SQLException{
		try {
			int scroll=ResultSet.TYPE_SCROLL_INSENSITIVE;
        	int update=ResultSet.CONCUR_UPDATABLE;
        	
        	if(params!=null) {
        		pstmt = conn.prepareStatement(query, scroll, update);
        		this.setParams(params);
        		return pstmt.executeQuery();
        	}else {
        		stmt = conn.createStatement(scroll, update);
        		return stmt.executeQuery(query);
        	}
		}catch (SQLException e) {
			log.error("# Error: " + e.toString());
            this.roleBack();
            throw new SQLException();
        }
	}
	
	 /**
     * update,insert,deleteSQLを実行して結果を取得する。
     * @param query
     * @return
	 * @throws SQLException 
     */
	public int write(String query) throws SQLException {
		return write(query, null);
	}
	
	 /**
     * パラメータ化されたupdate,insert,deleteSqlを実行して結果を取得する。
     * @param query
     * @param params
     * @return
	 * @throws SQLException 
     */
	public int write(String query, ArrayList<Object> params) throws SQLException{
		try {
			int scroll=ResultSet.TYPE_SCROLL_INSENSITIVE;
        	int update=ResultSet.CONCUR_UPDATABLE;
        	
        	if(params!=null) {
        		pstmt = conn.prepareStatement(query, scroll, update);
	            this.setParams(params);
	            int i = pstmt.executeUpdate();
	            if (i >= 0) {conn.commit();}
	            return i;
        	}else {
        		stmt = conn.createStatement(scroll, update);
        		int i = stmt.executeUpdate(query);
        		if(i>=0) {conn.commit();}
        		return i;
        	}
		}catch (SQLException e) {
			log.error("# Error: " + e.toString());
            this.roleBack();
            throw new SQLException();
        }
	}
	
	/**
     * 変数をセットする
	 * @throws SQLException 
     */
	private void setParams(ArrayList<Object> params) throws SQLException {
		try {
			ListIterator<Object> bind = params.listIterator();
			int num = 1;
			while(bind.hasNext()) {
				Object param = bind.next();
				if(param.toString().equalsIgnoreCase("integer")) {
					int tmpIntParam = Integer.parseInt(bind.next().toString());
					pstmt.setInt(num, tmpIntParam);
					num++;
				}else if(param.toString().equalsIgnoreCase("string")) {
					String tmpStrParam = bind.next().toString();
					pstmt.setString(num, tmpStrParam);
					num++;
				}else {
					pstmt.setObject(num, param);
					num++;
				}
			}
		}catch (SQLException e) {
			log.error("# Error: " + e.toString());
			this.roleBack();
			throw new SQLException();
		}
	}
	
	/**
     * コネクションを取得する。
	 * @throws SQLException 
     */
    private void getConnection() throws SQLException, ClassNotFoundException{
        try{
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, pass);
            conn.setAutoCommit(false);
        }catch(SQLException e){
        	log.error("# Error: " + e.toString());
            this.roleBack();
            throw new SQLException();
        } catch (ClassNotFoundException e) {
        	log.error("# Error: " + e.toString());
            this.roleBack();
            throw new ClassNotFoundException();
        }
    }
    
    /**
     * ロールバックする。
     * @throws SQLException 
     */
    private void roleBack() throws SQLException{
        try {
            conn.rollback();
        } catch (SQLException e) {
        	log.error("# Error: " + e.toString());
        	throw new SQLException();
        }finally{
            close();
        }
    }
    
    /**
     * メモリ開放したり色々閉じる。
     * @throws SQLException 
     */
    public void close() throws SQLException{
        try{
            if(rs != null){
                rs.close();
                rs = null;
            }
            if(stmt != null){
                stmt.close();
                stmt = null;
            }
            if(pstmt != null){
                pstmt.close();
                pstmt = null;
            }
            if(conn != null){
                if(!conn.isClosed()){
                    conn.close();
                }
                conn = null;
            }
        }catch(SQLException e){
        	log.error("# Error: " + e.toString());
        	throw new SQLException();
        }
    }
}
