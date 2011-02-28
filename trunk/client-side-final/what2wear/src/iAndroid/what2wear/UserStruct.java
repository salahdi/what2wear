package iAndroid.what2wear;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

/**
 * A structure the define a user.
 * Holds the number of images the user uploaded,
 * the user score, the user name, his email or id (depend 
 * if he logged in from facebook or gmail), his profile picture
 * and the latest picture he uploaded (with it's loading status).
 *
 */
public class UserStruct {
	public Integer images_num;
	public Float score;
	public String name;
	public String email_or_id;
	public String image_url = null;
	public Bitmap image = null;
	public ImageStruct latestImage = null;
	public int latestImageStatus = 1; //0 - no image, 1 - loading image, 2 - image loaded
	
	/**
	 * A constructor for the class.
	 */	
	public UserStruct() {
	}

	/**
	 * A constructor for the class.
	 * Creates the UserStruct according to a JSON object.
	 * @param obj- the JSON object.
	 * @throws JSONException
	 */
	public UserStruct(JSONObject obj) throws JSONException {
		deserializeFromObj(obj);
	}

	/**
	 * A constructor for the class.
	 * Creates the UserStruct according to a string that contains a JSON object.
	 * @param serializedObj- the JSON object as string.
	 * @throws JSONException
	 */	
	public UserStruct(String serializedObj) throws JSONException {
		deserialize(serializedObj);
	}

	/**
	 * Creates a JSON object from the given string and fetches the user information from it.
	 * @param serializedObj- the JSON object as string.
	 * @throws JSONException
	 */
	public void deserialize(String serializedObj) throws JSONException {
		JSONObject obj = new JSONObject(serializedObj);
		deserializeFromObj(obj);
	}

	/**
	 * This function receives a JSON object and fetches the user information from it.
	 * @param obj- the JSON object
	 * @throws JSONException
	 */
	public void deserializeFromObj(JSONObject obj) throws JSONException {
		this.images_num = obj.getInt("images_num_id");
		this.score = Float.valueOf(obj.getString("score_id"));
		this.name = obj.getString("name_id");
		this.email_or_id = obj.getString("email_or_id_id");
	}	
}
