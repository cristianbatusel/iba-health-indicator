package dk.ibapps.healthcheck;

/**
 * @author Cristian Batusel
 */

public enum HealthEnum {
	DB("1", "Database", "Can not connect to DB"),
	URL("2", "3rd Party", "Can not connect to 3rd party"),
	RW("3", "Read/write disk", "Can not read/write from disk"),
	S3("4", "S3", "Can not connect to S3"),
	;

	public static final String CODE = "code";
	public static final String MESSAGE = "message";
	public static final String ADDITIONAL_DETAILS = "additionalDetails";
	private String code;
	private String type;
	private String message;

	HealthEnum(String code, String type, String message) {
		this.code = code;
		this.type = type;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Error " +
				"code: " + code +
				", message: '" + message + '\'';
	}
}
