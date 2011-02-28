package iAndroid.what2wear;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserAdapter extends ArrayAdapter<UserStruct> {
    
    private ArrayList<UserStruct> userArray;
    private int activity;
    private Activity context;
    
    /**
     * A constructor for the class.
     */
    public UserAdapter(Activity context, int textViewResourceId, ArrayList<UserStruct> users, int activity) {
        super(context, textViewResourceId, users);
        this.context = context;
        this.userArray = users;
        this.activity = activity;
    }
    
    /**
     * This function returns the item at the given position in the array.
     */
    public UserStruct getItem(int position) {
    	return userArray.get(position);
    }
    
    /**
     * This function receives a position in the array and a bitmap.
     * The function saves the given bitmap in the UserStruct in the given position in the array.
     * Used for setting the user's profile picture. 
     */
    public void setItemBitmap(int position, Bitmap bitmap) {
    	UserStruct userStruct = userArray.get(position);
    	userStruct.image = bitmap;
    	userArray.set(position, userStruct);
    }
    
    /**
     * This function receives a position in the array and an ImageStruct.
     * If the given ImageStruct isn't null, the function saves it in the UserStruct in the given position in the array.
     * It also sets the image status- 0 if it's null (meaning no image) and 2 if there is an image.
     * It is used for setting the latest image the user has uploaded (or no image if the user didn't upload any images).
     */
    public void setItemImageStruct(int position, ImageStruct latestImage) {
    	UserStruct userStruct = userArray.get(position);
    	if (latestImage!= null) {
    		userStruct.latestImage = latestImage;
    		userStruct.latestImageStatus = 2;
    	}
    	else {
    		userStruct.latestImageStatus = 0;
    	}
    	userArray.set(position, userStruct);    	
    }
    
    /**
     * This function is called for every user in the array.
     * It displays the row with the user's details in the list the adapter is connected to.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
  
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (activity==1)
            	view = vi.inflate(R.layout.user_row_layout_mf, null);
            else
            	view = vi.inflate(R.layout.user_row_layout, null);
        }
        
        //Getting the current user
        final UserStruct user = userArray.get(position);
        
        if (user != null) {
            
            //Displaying the name of the user
            TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
            nameTextView.setText(user.name);
            
            //Displaying the user's score (rate)
            TextView ratingTextView = (TextView) view.findViewById(R.id.score_text_view);
            ratingTextView.setText("Score: " + user.score);
            
        
            //If the call came from the "MyFriends" activity, display profile images and latest uploaded image*/
            if (activity == 1) {
            	//Profile picture
	            ImageView imageView = (ImageView) view.findViewById(R.id.user_thumb_icon);
	            Bitmap thumbnail = user.image;
	            if (thumbnail == null) {
	            	thumbnail = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.no_user_photo);
	            }
				imageView.setImageBitmap(resizeAndConvert(thumbnail, 50));
				
				//Latest uploaded image 
	            ImageView image = (ImageView) view.findViewById(R.id.img);
	            Bitmap thumb = null;
	            //The image hasn't finished loading yet
	            if (user.latestImageStatus == 1) {
	            	thumb = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.temp);
	            }
	            //The image is loaded
	            else if (user.latestImageStatus == 2){
	            	thumb = user.latestImage.bitmap;
	            }
	            //No image (the user didn't upload images)
	            else {
	            	thumb = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.no_image);
	            }
	            image.setImageBitmap(resizeAndConvert(thumb, 100));
	            
	            if (user.latestImage!=null)
	            	user.latestImage.bitmap = thumb;
	            
	            //A listener for the image- when pressed, calls the BigPicDialog to show the picture on a bigger window.
	            image.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (user.latestImageStatus==2) {
							Intent myIntent = new Intent(v.getContext(), BigPicDialog.class);
							myIntent.putExtra("calledBy", "myFriends");
							myIntent.putExtra("currUser", position);
							context.startActivityForResult(myIntent, 0);
						}
					}
				});
            }
        }
        return view;
    }
	
    /**
     * This function gets a bitmap and a bound, and it resizes the bitmap to the given bound while keeping the proportions of the original bitmap.
     * If the bitmap was already smaller than the given bound, no change is made. 
     */
	private static Bitmap resizeAndConvert(Bitmap bitPic, int bound){

        float width = bitPic.getWidth();
        float height = bitPic.getHeight();

        if ((width < bound) && (height < bound)){
            return bitPic;
        }

        float proportion = width/height;
        float newWidth = bound * proportion;
        float newHeight = bound;        

        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitPic, 0, 0, (int)width, (int)height, matrix, true); 
        return resizedBitmap;
    }
}
