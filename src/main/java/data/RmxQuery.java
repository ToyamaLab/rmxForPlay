package data;

public enum RmxQuery {
//	DOMAIN_RULE_QUERY("SELECT domain.db_manager, domain.db_url, domain.username, domain.password, "
//			+ "rule.rule_name, rule.rule_name_value, rule.rule_type, rule.rule_type_value, rule.rule_query, rule.rule_query_value "
//			+ "FROM domain, rule WHERE domain.domain_name=? AND rule.domain_id=domain.id"),
	DOMAIN_RULE_QUERY("SELECT domain.db_manager, domain.db_url, domain.username, domain.password, "
			+ "rule.rule_name, rule.rule_name_value, rule.rule_type, rule.rule_type_value, rule.rule_query, rule.rule_query_value "
			+ "FROM domain, rule , rule_domain rd WHERE domain.domain_name=? AND rd.rule_id = rule.id AND rd.domain_id = domain.id"),
	
	LIST_API_QUERY("SELECT id, recipient, sender, subdomain, subject, transmitted FROM timeline WHERE subdomain = ?"),
	GET_API_QUERY("SELECT id, recipient, sender, subdomain, subject, transmitted, body, header FROM timeline WHERE id = ? AND subdomain = ?"),
	DELETE_API_QUERY("DELETE FROM timeline WHERE id = ? AND subdomain = ?");
	
	
	
	private String query;
	
	private RmxQuery(String query) {
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
}
