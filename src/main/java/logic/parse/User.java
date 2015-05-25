package logic.parse;

import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.DBDao;
import dao.PropfileDao;
import data.RmxQuery;
import logic.parse.SOP.*;
import logic.service.AESService;

public class User implements parserVisitor {
	/** Log */
	private static final Logger log = LoggerFactory.getLogger(User.class);
	
	/** DB conn */
	public String driver;
	public String url;
	public String user;
	public String pass;
	public DBDao dbdao;
	
	/** rule & domain info in DB */
	public HashMap<String, String> ruleMaps;
	public HashMap<String, String> domainMap;
	
	/** ex) mail's domain ex)testk.db.ics.keio.ac.jp */
	public  String domain;
	public  String subdomain;
	public String fulldomain;

	/** recipient */
	public  String recipient;

	/** # function */
	public  String function;
	public String command;
	public ArrayList<String> commandArgs;
	public String functionTarget;
	
	/** flg */
	public boolean functionFlg;
	public boolean normalFlg;
	private boolean polymorFlg;
	
	/** ex) obunai, speed+wix+ssql */
	public  ArrayList<String> values;

	/** ex) name, grp */
	public  ArrayList<String> keys;

	/** ex) obunai, speed, wix, ssql */
	public  ArrayList<Object> params;

	/** count parameter for each query */
	private ArrayList<Integer> paranum;
	

	/** query before union, intersect or except among rules
	 *  i.g. queries.size = the number of rules */
	private ArrayList<String> queries;

	/** query before union, intersect or except
	 * 	i.g. minimamqueries.size = the number of queries called from conf file */
	private ArrayList<String> minimamQueries;

	/** operateors order by appeared */
	private ArrayList<String> operator;

	/** ex) (select ~) union (~) intersect (~) */
	public  String finalQuery;

	/** ex) testo.properties */
	public PropfileDao envDao;
	
	/** ex) name, grp */
	public  String rule;

	/** true if address has paralist until intersection, union, except's leftnode*/
	private boolean paralistFlg;

	/** key{paralist} */
	private ArrayList<String> paralist;

	/** values for paralist*/
	private ArrayList<String> tmppara;

	/** true if address contains Atmark rule 
	 * ex)course2valuation{DM}@ */
	private boolean containsATmark;

	/***/
	public  int polymorChildNum;

	/** ex) gradeType = integer,String */
	public  ArrayList<String> polyTypes;
	public  int polyTypesNum;
	public  int polyTypesPointer;
	public  String polyLastType;
	
	/**Error*/
	public Exception errorObj = null;

	//	public static User visitor;
	//	public static parser parser;

	public User(PropfileDao envDao) {
		this.envDao = envDao;
		this.driver = new String();
		this.url = new String();
		this.user = new String();
		this.pass = new String();
		this.ruleMaps = new HashMap<String,String>();
		this.domainMap = new HashMap<String, String>();
		this.domain = new String();
		this.subdomain = new String();
		this.fulldomain = new String();
		this.function = new String();
		this.command = new String();
		this.commandArgs = new ArrayList<String>();
		this.functionTarget = new String();
		this.values = new ArrayList<String>();
		this.keys = new ArrayList<String>();
		this.params = new ArrayList<Object>();
		this.paranum = new ArrayList<Integer>();
		this.queries = new ArrayList<String>();
		this.operator = new ArrayList<String>();
		this.paralist = new ArrayList<String>();
		this.tmppara = new ArrayList<String>();
		this.finalQuery = new String();
		this.polymorChildNum = 0;
		this.rule = new String();
		this.minimamQueries = new ArrayList<String>();
	}

	public void UserStart(String recipient) {
		try {
			// parserスタート
			log.debug("==== parser start ====");
			
			this.recipient = recipient;
			this.driver = envDao.getString("DB_DRIVER");
			this.url = envDao.getString("DB_URL");
			this.user = envDao.getString("DB_ID");
			this.pass = envDao.getString("DB_PASSWORD");
			this.dbdao = new DBDao(driver, url, user, pass);
			
			User visitor = new User(envDao);
			visitor.dbdao = dbdao;
			parser parser = new parser(new StringReader(recipient));
			ASTRecipient start;
			
			start = parser.Recipient();
			// Recipientへ
			start.jjtAccept(visitor, null);
			
			visitor.paranum.add(-1);

			operator = visitor.operator;
			keys = visitor.keys;
			domain = visitor.domain;
			subdomain = visitor.subdomain;
			recipient = visitor.recipient;
			minimamQueries = visitor.minimamQueries;
			params = visitor.params;
			queries = visitor.queries;
			this.remakepara();
			paranum = visitor.paranum;
			function = visitor.function;
			command = visitor.command;
			commandArgs = visitor.commandArgs;
			functionFlg = visitor.functionFlg;
			normalFlg = visitor.normalFlg;
			paralist = visitor.paralist;
			queries = visitor.queries;
			domainMap = visitor.domainMap;
			ruleMaps = visitor.ruleMaps;

			for (int i = 0; i < params.size(); i++) {
				log.debug("params : " + params.get(i).toString());
			}

			visitor.finalQuery = this.simplereplace(visitor.finalQuery);
			log.debug("final query : " + visitor.finalQuery);
			this.checkATmark();

			finalQuery = visitor.finalQuery;

			//initPolyTypes();
			 //ここでルール一覧が手に入るよ！
            System.out.println(ruleMaps);
            
            
			log.debug("==== parser end ====");
		} catch (MissingResourceException e) {
			log.error("# Error: " + e.toString());
			errorObj = e;
		} catch (ClassNotFoundException e) {
			log.error("# Error: " + e.toString());
			errorObj = e;
		} catch (SQLException e) {
			log.error("# Error: " + e.toString());
			errorObj = e;
		} catch (ParseException e) {
			log.error("# Error: " + e.toString());
			errorObj = e;
		}
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		return null;
	}

	@Override
	public Object visit(ASTRecipient node, Object data) {
		showChilds(node);
		
		// 通常の場合はvisit(ASTAddress)、#の場合はvisit(ASTDebugEx)へ
		return node.jjtGetChild(0).jjtAccept(this, null);
	}
	
	@Override
	public Object visit(ASTAddress node, Object data) {
		showChilds(node);
		int childnum = node.jjtGetNumChildren();
		
		normalFlg = true;
		
		if(childnum > 1){
			// visit(ASTdomain)へ
			fulldomain = node.jjtGetChild(1).jjtAccept(this, null).toString();
		}
		
		// 和集合の場合はvisit(ASTUnion)、積集合の場合はvisit(ASTIntersection)、
		// 差集合の場合はvisit(ASTException)、ルールが1つの場合はvisit(ASTExp)へ
		finalQuery = node.jjtGetChild(0).jjtAccept(this, null).toString();
		
		log.debug("fulldomain :" + fulldomain);
		
		return ">rule{para}<" + "@" + fulldomain;
	}
	
	@Override
	public Object visit(ASTdomain node, Object data) {
		showChilds(node);
		int childnum = node.jjtGetNumChildren();
		
		// visit(ASTSubdomain)へ
		subdomain = node.jjtGetChild(0).jjtAccept(this, null).toString();
		
		log.debug("subdomain :" + subdomain);
		
		// DBからsubdomainとsubdomainに対応したルール群をHashMap(domainMapとruleMaps)へ格納
		this.putMaps(subdomain);

		for (int j = 1; j < childnum; j++) {
			// visit(ASTDomainArgs)へ
			String tmp = node.jjtGetChild(j).jjtAccept(this, null).toString();
			if (j == 1)
				domain = tmp;
			else
				domain = domain + "." + tmp;
		}
		
		log.debug("domain :" + domain);
		
		return subdomain + "." + domain;
	}
	
	@Override
	public Object visit(ASTExp node, Object data) {
		showChilds(node);
		int childnum = node.jjtGetNumChildren();
		
		// visit(ASTRule)へ
		rule = node.jjtGetChild(0).jjtAccept(this, null).toString();
		
		// パラメータが複数の場合はvisit(ASTParalis)、1つの場合はvisit(ASTValue)、ポリモルフィックの場合はvisit(ASTPolimolPara)へ
		String query = node.jjtGetChild(1).jjtAccept(this, null).toString();
		
		// パラメータが1つか、ポリモルフィックのとき
		if(node.jjtGetChild(1).toString().equalsIgnoreCase("Value") || node.jjtGetChild(1).toString().equalsIgnoreCase("PolimolPara") ){
			if(tmppara.size()>0){
				paralist.add(tmppara.get(0));
				tmppara.clear();
			}
		}
		
		log.debug("rule : " + rule);
		log.debug("Expquery : " + query);
		
		queries.add(query);
		keys.add(rule);

		//checkNest();
		
		return query;
	}
	
	@Override
	public Object visit(ASTParalis node, Object data) {
		showChilds(node);
		int childNum = node.jjtGetNumChildren();
		
		String query = "";
		paralistFlg = true;

		String[] tmpparalis = new String[childNum];
		tmppara.clear();		
		for (int i = 0; i < childNum; i++) {
			
			// visit(ASTValue)へ
			String tmpQuery = node.jjtGetChild(i).jjtAccept(this, null).toString();

			if (i == 0) {
				query = tmpQuery;
			} else {
				query = query + " union " + tmpQuery;
			}

			log.debug("tmppara::"+tmppara.get(0));
			tmpparalis[i] = tmppara.get(0).toString();
			tmppara.clear();
		}

		String tmp = new String();
		for(int j = 0; j < childNum; j++){
			if(j==0){
				tmp = tmpparalis[j];
			}else{
				tmp = tmp + "+" + tmpparalis[j];
			}
		}
		paralist.add(tmp);

		log.debug("Paralisquery : " + query);
		
		return query;
	}
	
	@Override
	public Object visit(ASTPolimolPara node, Object data) {
		showChilds(node);
		
		String query = "";

		polymorChildNum = node.jjtGetNumChildren();

		log.debug("polymorchildnum : " + polymorChildNum);

		// zonop add. if Type includes integer and String same line.
		if (ruleMaps.get(rule + "Type").indexOf(",") > 0) {
			//makePolyTypes();
		}
		polymorFlg = true;

		for (int i = 0; i < polymorChildNum; i++) {
			log.debug("child [ " + i + " ] type : " + node.jjtGetChild(i).toString());
			node.jjtGetChild(i).jjtAccept(this, null).toString();
		}

		polymorFlg = false;

		// zonop add. if answer api, get query[0]
		String keyquery = new String();
		
		keyquery = rule + "[" + polymorChildNum + "]";

		query = "(" + ruleMaps.get(keyquery) + ")";
		minimamQueries.add(query);
		log.debug("Polymorquery" + query);

		query = "(" + ruleMaps.get(keyquery) + ")";
		int count = 0;
		String q = query;
		String regex = "$";
		for(;q.indexOf(regex) > 0;){
			q = q.replaceFirst("\\$", "?");
			count++;
		}
		
		log.debug("count : " + count);
		paranum.add(count);

		String tmp = new String();
		for(int i = 0; i < polymorChildNum; i++){
			if(i==0){
				tmp = tmppara.get(i).toString();
			}else{
				tmp = tmp + "-"+tmppara.get(i).toString();
			}
		}
		tmppara.clear();
		tmppara.add(tmp);

		return query;
	}
	
	@Override
	public Object visit(ASTValue node, Object data) {
		String value = node.nodeValue;
		
		log.debug("Value : " + value);
		
		String keyquery = new String();

		if (value.equals("all") | value.equals("*")) 
			keyquery = rule + "[" + 0 + "]";
		else
			keyquery = rule + "[" + 1 + "]";
		
		String query = "";
		
		try{
			if(!polymorFlg){
				query = "(" +ruleMaps.get(keyquery) +")";
				
				int count = 0;
				String q = query;
				String regex = "$";
				for(;q.indexOf(regex) > 0;){
					q = q.replaceFirst("\\$", "?");
					count++;
				}
				
				log.debug("count : " + count);
				
				if(count != 0)
					paranum.add(count);
				minimamQueries.add(query);
			}
		}catch(Exception e){
			query = "miss";

			return query;
		}

		log.debug("Value's query : " + query);
		
		String ruleTypeValue = ruleMaps.get(rule+"Type");
		
		if (polyTypesNum > 0 && polyTypesPointer < polyTypesNum) {
			params.add((String) polyTypes.get(polyTypesPointer));
			polyTypesPointer++;
			if (polyTypesPointer == polyTypesNum) {
				polyLastType = (String) polyTypes.get(polyTypesPointer - 1);
			}
		} else if (polyTypesNum > 0 && polyTypesPointer >= polyTypesNum) {
			params.add(polyLastType);
		} else if (ruleTypeValue.equalsIgnoreCase("integer")) {
			params.add("integer");
		} else if (ruleTypeValue.equalsIgnoreCase("String")) {
			params.add("String");
		}

		params.add(value);
		tmppara.add(value);

		return query;
	}

	@Override
	public Object visit(ASTException node, Object data) {
		showChilds(node);

		String left = node.jjtGetChild(0).jjtAccept(this, null).toString();
		paranum.add(-1);
		operator.add("-");
		String right = node.jjtGetChild(1).jjtAccept(this, null).toString();

		log.debug("left query : " + left);
		log.debug("right query : " + right);

		if(!(left.contains("intersect")|left.contains("union")|left.contains("except")) | paralistFlg){
			//    		queries.add(left);
			if(node.jjtGetChild(0).toString().equalsIgnoreCase("Paralis") && !node.jjtGetChild(1).toString().equalsIgnoreCase("Paralis")){
				paralistFlg = false;		
			}
		}
		if(!(right.contains("intersect")|right.contains("union")|right.contains("except")) | paralistFlg){
			//    		queries.add(right);
			paralistFlg =false;
		}

		String query = " ( " + left + " ) " + " except " + " ( " + right + " ) ";
		
		return query;
	}

	@Override
	public Object visit(ASTUnion node, Object data) {
		showChilds(node);

		String left = node.jjtGetChild(0).jjtAccept(this, null).toString();
		paranum.add(-1);
		operator.add("+");
		String right = node.jjtGetChild(1).jjtAccept(this, null).toString();

		log.debug("left query : " + left);
		log.debug("right query : " + right);

		if(!(left.contains("intersect")|left.contains("union")|left.contains("except")) | paralistFlg){
			//    		queries.add(left);
			if(node.jjtGetChild(0).toString().equalsIgnoreCase("Paralis") && !node.jjtGetChild(1).toString().equalsIgnoreCase("Paralis")){
				paralistFlg = false;		
			}
		}
		if(!(right.contains("intersect")|right.contains("union")|right.contains("except")) | paralistFlg){
			//    		queries.add(right);
			paralistFlg =false;
		} 

		String query = " ( " + left + " ) " + " union " + " ( " + right + " ) ";
		return query;
	}

	@Override
	public Object visit(ASTIntersection node, Object data) {
		showChilds(node);

		String left = node.jjtGetChild(0).jjtAccept(this, null).toString();
		paranum.add(-1);
		operator.add(".");
		String right = node.jjtGetChild(1).jjtAccept(this, null).toString();

		log.debug("left query : " + left);
		log.debug("right query : " + right);

		if(!(left.contains("intersect")|left.contains("union")|left.contains("except")) | paralistFlg){
			//    		queries.add(left);
			if(node.jjtGetChild(0).toString().equalsIgnoreCase("Paralis") && !node.jjtGetChild(1).toString().equalsIgnoreCase("Paralis")){
				paralistFlg = false;		
			}
		}
		if(!(right.contains("intersect")|right.contains("union")|right.contains("except")) | paralistFlg){
			//    		queries.add(right);
			paralistFlg =false;
		}

		return " ( " + left + " ) " + " intersect " + " ( " + right + " ) ";
	}

	@Override
	public Object visit(ASTArg node, Object data) {
		String name = node.nodeValue;

		return String.valueOf(name);
	}

	@Override
	public Object visit(ASTDebugEx node, Object data) {
		showChilds(node);
		int childnum = node.jjtGetNumChildren();
		
		functionFlg = true;
		
		if(childnum==3) {
			String functionExp = node.jjtGetChild(0).jjtAccept(this, null)
					.toString();
			String functionExp2 = node.jjtGetChild(1).jjtAccept(this, null)
					.toString();
			String functionDomain = node.jjtGetChild(2).jjtAccept(this, null)
					.toString();
					
			recipient = node.jjtGetChild(2).jjtAccept(this, null).toString();
					
			log.debug("functionExp :" + functionExp);
			log.debug("recipient :" + recipient);
			
			return "#" + functionExp + "#" + functionExp2 + "@" + functionDomain;
		}else {
			String functionExp = node.jjtGetChild(0).jjtAccept(this, null)
					.toString();
			String functionDomain = node.jjtGetChild(1).jjtAccept(this, null)
					.toString();
					
			recipient = node.jjtGetChild(1).jjtAccept(this, null).toString();
					
			log.debug("functionExp :" + functionExp);
			log.debug("recipient :" + recipient);
			
			
			return "#" + functionExp + "#" + "@" + functionDomain;
		}
	}

	@Override
	public Object visit(ASTDebug node, Object data) {
		showChilds(node);
		int childnum = node.jjtGetNumChildren();

		for (int j = 0; j < childnum; j++) {
			String tmp = node.jjtGetChild(j).jjtAccept(this, null).toString();
		}
		function = "";
		command = "";

		function = node.jjtGetChild(0).jjtAccept(this, null).toString();
		command = node.jjtGetChild(1).jjtAccept(this, null).toString();
		if(childnum>2) {
			for(int i=2;i<childnum;i++) {
				commandArgs.add(node.jjtGetChild(i).jjtAccept(this, null).toString());
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<commandArgs.size();i++) {sb.append("."+commandArgs.get(i));}
		String tmpStr = new String(sb);
		String tmpDubug = function + "." + command + tmpStr;
		
		if(childnum>2) {
			return tmpDubug;
		}else {
			return function+"."+command;
		}
	}

	@Override
	public Object visit(ASTRule node, Object data) {
		String name = node.nodeValue;
		
		return String.valueOf(name);
	}

	@Override
	public Object visit(ASTSubdomain node, Object data) {
		String name = node.nodeValue;
		return String.valueOf(name);
	}

	@Override
	public Object visit(ASTfunction node, Object data) {
		String name = node.nodeValue;

		return String.valueOf(name);
	}

	@Override
	public Object visit(ASTalias node, Object data) {
		String name = node.nodeValue;

		return String.valueOf(name);
	}
	
	@Override
	public Object visit(ASTcommand node, Object data) {
		String name = node.nodeValue;

		return String.valueOf(name);
	}
	
	@Override
	public Object visit(ASTcommandArg node, Object data) {
		String name = node.nodeValue;

		return String.valueOf(name);
	}
	
	@Override
	public Object visit(ASTDomainArg node, Object data) {
		String name = node.nodeValue;

		return String.valueOf(name);
	}

	public void remakepara(){
		log.debug("this is remakepara1");

		String regex;
		String currentquery;
		int index;
		int k = 0;
		int l = 0;
		ArrayList<Object> tmppara = new ArrayList<Object>();
		regex = "$";

		//		System.out.println(queries);

		boolean need_remake = false;
		//if query contains $recipient or $sender need_remake become true
		//or $ is used several times again in same query
		for(int i = 0; i < queries.size(); i++ ){
			log.debug(queries.get(i).toString());
			if(queries.get(i).indexOf("$s") > 0 || queries.get(i).indexOf("$r") > 0){
				log.debug("contains $sender or $recipient");
				need_remake = true;
				break;
			}

			String q = queries.get(i);
			for(int j = 0; q.indexOf("$") > 0; j++){
				if(q.indexOf("$" + j) > 0){
					q = q.replaceFirst("\\$", "?");
					if(q.indexOf("$" + j) > 0){
						log.debug("contains same $ is used in same query");
						need_remake = true;
						break;
					}
				}

			}
		}

		if(need_remake){

			for (int i = 0; i < minimamQueries.size(); i++ , l = 0){
				currentquery = minimamQueries.get(i).toString();
				for(int j = 1;; j++){
					index = currentquery.indexOf(regex);
					if(index < 0){
						if(currentquery.indexOf("?") < 0 && currentquery.indexOf(".jar") > 0){
							//plugin
							tmppara.add(params.get(k++));
							tmppara.add(params.get(k++));
							break;
						}else{
							//rule[0]
							break;
						}					
					}else if(String.valueOf(currentquery.charAt(index+1)).equals(Integer.toString(j))){
						tmppara.add(params.get(k++));
						tmppara.add(params.get(k++));
						l = l + 2;
						currentquery = currentquery.replaceFirst("\\$", "?");
					}else if(String.valueOf(currentquery.charAt(index+1)).equals("s")){
						tmppara.add("String");
						tmppara.add("sender");
						currentquery = currentquery.replaceFirst("\\$", "?");
					}else if(String.valueOf(currentquery.charAt(index+1)).equals("r")){
						tmppara.add("String");
						tmppara.add("recipient");
						currentquery = currentquery.replaceFirst("\\$", "?");
					}else{
						if (String.valueOf(currentquery.charAt(index+1)).matches("[^0-9]")){
							log.debug("illegal placeholder in query : " + currentquery);
							break;
						}else{
							j = 0;
							k = k - l;
						}
					}
				}
			}
		}
		log.debug("this is remakepara2");
		for(int i = 0; i < tmppara.size(); i++){
			log.debug("tmppara : " +tmppara.get(i).toString());
		}
		if(!tmppara.isEmpty()){
			params = tmppara;	
		}

	}

	public String simplereplace(String q){
		String regex;
		for(int i = 1; ;i++){
			regex = "$"+Integer.toString(i);
			log.debug("regex : " + regex);
			if(q.indexOf(regex) < 0)
				break;
			q = q.replaceAll("\\$" + Integer.toString(i), "?");
		}

		if(q.indexOf("$sender") > 0){
			q = q.replaceAll("\\$sender", "?");
		}

		if(q.indexOf("$recipient") > 0){
			q = q.replaceAll("\\$recipient", "?");
		}
		return q;
	}

	public void checkATmark(){
		for(int i = 0; i < params.size(); i ++){
			if(params.get(i).equals("sender")){
				containsATmark = true;
			}else if (params.get(i).equals("recipient")){
				containsATmark = true;
			}
		}
	}
	
	/**
	 * 
	 * */
	public void showChilds(SimpleNode node) {
		log.debug("this is : " + node.toString());
		int childnum = node.jjtGetNumChildren();
		log.debug("childnum : " + childnum);
		for (int i = 0; i < childnum; i++) {
			log.debug("child [ " + i + " ] type : " + node.jjtGetChild(i).toString());
		}
	}
	
	/**
	 * @throws SQLException 
	 * 
	 * */
	private void putMaps(String subdomain){
		try {
			String query = RmxQuery.DOMAIN_RULE_QUERY.getQuery();
			ArrayList<Object> params = new ArrayList<Object>();
			params.add(subdomain);
			ResultSet rs = dbdao.read(query, params);
			
			while(rs.next()) {
				ruleMaps.put(rs.getString("rule_name"), rs.getString("rule_name_value"));
				ruleMaps.put(rs.getString("rule_type"), rs.getString("rule_type_value"));
				ruleMaps.put(rs.getString("rule_query"), rs.getString("rule_query_value"));
			}
			System.out.println(envDao.getString("SECRET_KEY"));
			if(rs.first()) {
				
				domainMap.put("SUBDOMAIN_NAME", subdomain);
				domainMap.put("DB_MANAGER", rs.getString("db_manager"));
				domainMap.put("DB_URL", AESService.decrypt(rs.getString("db_url"), envDao));
				domainMap.put("USERNAME", AESService.decrypt(rs.getString("username"), envDao));
				domainMap.put("PASSWORD", AESService.decrypt(rs.getString("password"),envDao));
				domainMap.put("ADMIN_EMAIL", rs.getString("admin_email"));
			}
		} catch (SQLException e) {
			log.error("# Error: " + e.toString());
			errorObj = e;
		}
	}
	
	/** getter */
	public ArrayList<String> getminimamqueries(){
		return minimamQueries;
	}

	public ArrayList<String> getoperator(){
		return operator;
	}

	public ArrayList<String> getKeys() {
		return keys;
	}

	public String getDomain() {
		return domain;
	}
	
	public boolean getNormalFlg() {
		return normalFlg;
	}

	public String getFunction() {
		return function;
	}
	
	public boolean getFunctionFlg() {
		return functionFlg;
	}
	public String getCommand(){
		return command;
	}
	
	public ArrayList<String> getCommandArgs(){
		return commandArgs;
	}
	
	public String getTarget() {
		if(getFunctionFlg()) {
			int start = recipient.indexOf("#", 1);
			return recipient.substring(start+1);
		}else
			return null;
	}

	public String getSundomain() {
		return subdomain;
	}

	public ArrayList<Object> getPara() {
		return params;
	}

	public ArrayList<Integer> getParanum(){
		return paranum;
	}

	public String getQuery() {
		return finalQuery;
	}

	public String getfulldomain(){
		return subdomain + "." + domain; 
	}
	
	public HashMap<String, String> getRuleMaps(){
		return ruleMaps;
	}
	
	//user.getRuleMaps
	
	public HashMap<String, String> getDomainMap(){
		return domainMap;
	}
	
	public Exception getErrorObj() {
		return errorObj;
	}
	
	/** used only in 1st Exp */
	@Override
	public Object visit(ASTRecipient1 node, Object data) {

		return null;
	}

	@Override
	public Object visit(ASTDebugEx1 node, Object data) {

		return null;
	}

	@Override
	public Object visit(ASTAddress1 node, Object data) {

		return null;
	}

	@Override
	public Object visit(ASTParas1 node, Object data) {

		return null;
	}

	@Override
	public Object visit(ASTdomain1 node, Object data) {

		return null;
	}
}
