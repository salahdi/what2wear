package iAndroid.what2wear;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class myFriends extends Activity {

	private ArrayList<UserStruct> users = null;
	private UserAdapter usersAdapter = null;

	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.top5_layout);

		ImageView imageView = (ImageView) findViewById(R.id.top5_title);
		Context myContext = getBaseContext();
		Bitmap bm = BitmapFactory.decodeResource(myContext.getResources(),
				R.drawable.my_friends_title);
		imageView.setImageBitmap(bm);

		SharedPreferences mySharedPreferences = getSharedPreferences(
				Common.PREF_NAME, Activity.MODE_PRIVATE);
		if ("google".equals(mySharedPreferences.getString("account_id", ""))) {
			this.users = AsyncFriendsGet.users;
		} else if ("facebook".equals(mySharedPreferences.getString(
				"account_id", ""))) {
			this.users = AndroidList.facebook.friends;
		}
		if (users != null) {
			ListView MFusers = (ListView) findViewById(R.id.top5users);
			usersAdapter = new UserAdapter(this, R.layout.user_row_layout,users, 1);
			users = (ArrayList<UserStruct>) getIntent().getSerializableExtra("users");

			MFusers.setAdapter(usersAdapter);

			if (users != null && !users.isEmpty()) {

				usersAdapter.notifyDataSetChanged();
				usersAdapter.clear();
				for (int i = 0; i < users.size(); i++) {
					usersAdapter.add(users.get(i));
				}
			}

			usersAdapter.notifyDataSetChanged();
			MFusers.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					UserStruct user = usersAdapter.getItem(position);
					/* perform async search */
					new AsyncUserImages(view.getContext())
							.execute(user.email_or_id);
				}
			});
		}
	}
}
