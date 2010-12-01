package iAndroid.what2wear;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageStruct {
	public String gender;
	public String items_num;
	public String style;
	public String season;
	public ArrayList<ItemStruct> itemsArray = new ArrayList<ItemStruct>();
	public String rating_id = "";
	public String url_id = "";


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

	public void deserializeFromObj(JSONObject obj) throws JSONException {
		this.gender = obj.getString("gender_id");
		this.items_num = obj.getString("items_num_id");
		this.style = obj.getString("style_id");
		this.season = obj.getString("season_id");
		this.rating_id = obj.getString("rating_id");
		this.url_id = "http://what-2-wear.appspot.com/img?key_id=" + obj.getString("key_id");
		int size = Integer.parseInt(this.items_num.trim());
		for (int i = 1; i<=size; i++){
			ItemStruct item = new ItemStruct();
			item.item_type = obj.getString("item" + i+ "_type_id");
			item.item_color = obj.getString("item" + i+ "_color_id");
			this.itemsArray.add(item);
		}
	}

}