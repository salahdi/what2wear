package iAndroid.what2wear;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Bitmap;

/**
 * ImageStruct contains information about an image:
 * The gender, number of items and an array
 * of ItemStruct which defines the item in the picture,
 * the styles, the seasons etc.
 * It also holds the url to the picture and the picture as bitmap (after it's downloaded from the url).
 *
 */
public class ImageStruct {
	public String key;
	public String gender;
	public String items_num;
	public String styles;
	public String seasons;
	public ArrayList<ItemStruct> itemsArray = new ArrayList<ItemStruct>();
	public String rating_id = "";
	public String url_id = "";
	public Bitmap bitmap = null;

	public ImageStruct() {
	}

	public ImageStruct(JSONObject obj) throws JSONException {
		deserializeFromObj(obj);
	}

	public ImageStruct(String serializedObj) throws JSONException {
		deserialize(serializedObj);
	}

	public void deserialize(String serializedObj) throws JSONException {
		JSONObject obj = new JSONObject(serializedObj);
		deserializeFromObj(obj);
	}

	/**
	 * The method receives a JSON object and 
	 * fills the fields of the ImageStruct accordingly.
	 * @param obj
	 * @throws JSONException
	 */
	public void deserializeFromObj(JSONObject obj) throws JSONException {
		this.key = obj.getString("key_id");
		this.gender = obj.getString("gender_id");
		this.items_num = obj.getString("items_num_id");
		this.styles = obj.getString("style_id");
		this.seasons = obj.getString("season_id");
		this.rating_id = obj.getString("rating_id");
		this.url_id = "http://what-2-wear.appspot.com/img?image_key_id=" + obj.getString("image_key_id");
		int size = Integer.parseInt(this.items_num.trim());
		for (int i = 1; i<=size; i++){
			ItemStruct item = new ItemStruct();
			item.item_type = obj.getString("item" + i+ "_type_id");
			item.item_color = obj.getString("item" + i+ "_color_id");
			this.itemsArray.add(item);
		}
	}
}