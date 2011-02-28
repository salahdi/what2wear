package iAndroid.what2wear;

/**
 * ItemStruct characterizes an item in an image.
 * It holds the item's type (Shirt, Coat etc.)
 * and the item's color.
 *
 */
public class ItemStruct {
	public String item_type;
	public String item_color;

	/**
	 * A constructor for the class.
	 */
	public ItemStruct(){
		item_type = "";
		item_color = "";
	}

}
