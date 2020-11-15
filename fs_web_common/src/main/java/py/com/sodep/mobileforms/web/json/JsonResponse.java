package py.com.sodep.mobileforms.web.json;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;

@JsonInclude(Include.NON_NULL)
public class JsonResponse<T> {

	private boolean success;

	private String title;

	private String unescapedTitle;

	private String message;

	private String unescapedMessage;

	private Map<String, Object> content;

	private T obj;

	public boolean getSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Map<String, Object> getContent() {
		return content;
	}

	public void setContent(Map<String, Object> content) {
		this.content = content;
	}

	/**
	 * Message is meant to be an explanation of the action that took place. It
	 * could be a more elaborated description than caption
	 * 
	 * If only one message is needed, this is the preferred property to put it.
	 * 
	 * @param message
	 */
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
	}

	/**
	 * Title is meant to be used as a short message or title for a response to
	 * the user
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * If the title has some html content use this field
	 * 
	 * @return unescaped String. No html content is escaped
	 */
	@JsonSerialize(using = StringSerializer.class, include = Inclusion.NON_NULL)
	public String getUnescapedTitle() {
		return unescapedTitle;
	}

	public void setUnescapedTitle(String unescapedTitle) {
		this.unescapedTitle = unescapedTitle;
	}

	/**
	 * If the message has some html content that needs to be renderer on the
	 * browser use this
	 * 
	 * @return unescaped String. No html content is escaped
	 */
	@JsonSerialize(using = StringSerializer.class, include = Inclusion.NON_NULL)
	public String getUnescapedMessage() {
		return unescapedMessage;
	}

	public void setUnescapedMessage(String unescapedMessage) {
		this.unescapedMessage = unescapedMessage;
	}

	public void addContent(String key, Object val) {
		if (content == null) {
			content = new HashMap<String, Object>();
		}
		content.put(key, val);
	}

	public static JsonResponse<String> buildSimpleSuccessResponse(String message) {
		JsonResponse<String> response = new JsonResponse<String>();
		response.setMessage(message);
		response.setSuccess(true);
		return response;
	}

	public static JsonResponse<String> buildSimpleFailureResponse(String message) {
		JsonResponse<String> response = new JsonResponse<String>();
		response.setMessage(message);
		response.setSuccess(false);
		return response;
	}

}
