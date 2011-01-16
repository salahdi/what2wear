package iAndroid.what2wear;

import org.json.JSONException;
import org.json.JSONObject;

public class UserStruct {
	public Integer images_num;
	public Float score;
	public String name;
	public String email_or_id;
	public String image_url=null;
	
	public UserStruct() {
	}

	public UserStruct(JSONObject obj) throws JSONException {
		deserializeFromObj(obj);
	}

	public UserStruct(String serializedObj) throws JSONException {
		deserialize(serializedObj);
		
	}

	public void deserialize(String serializedObj) throws JSONException {
		JSONObject obj = new JSONObject(serializedObj);
		deserializeFromObj(obj);
	}

	public void deserializeFromObj(JSONObject obj) throws JSONException {
		this.images_num = obj.getInt("images_num_id");
		this.score = Float.valueOf(obj.getString("score_id"));
		this.name = obj.getString("name_id");
		this.email_or_id = obj.getString("email_or_id_id");
	}	
}
