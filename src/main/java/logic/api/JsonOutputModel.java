package logic.api;

public class JsonOutputModel {
	private Integer code;
	private String message;
	private Object retval;
	
	/** getter & setter */
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getRetval() {
		return retval;
	}
	public void setRetval(Object retval) {
		this.retval = retval;
	}
}
