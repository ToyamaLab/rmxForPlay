package data;

public enum RmxAPIStatus {
	OK							(200, "ok"),
	CLIENT_ERROR				(404, "incorrect args"),
	INTERNAL_SERVER_ERROR		(500, "internal server error"),
	DB_CLASS_NOT_FOUND_ERROR	(501, "db class not found"),
	DB_ACCESS_ERROR				(502, "cannot access db"),
	SYNTAX_ERROR				(503, "syntax error");
	
	private Integer code;
	private String message;
	
	private RmxAPIStatus(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public Integer getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
}
