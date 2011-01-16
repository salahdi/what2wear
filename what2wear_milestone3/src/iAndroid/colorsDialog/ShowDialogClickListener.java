package iAndroid.colorsDialog;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class ShowDialogClickListener implements OnClickListener {
  private Activity activity;
  private int dialogID;

  /**
   * Creates a show dialog click listener
   * @param activity The activity from which the dialog will be created
   * @param dialogID The ID of the dialog in the activity
   */
  public ShowDialogClickListener(Activity activity, int dialogID) {
    this.activity = activity;
    this.dialogID = dialogID;
  }

  public void onClick(View v) {
    this.activity.showDialog(this.dialogID);
  }

}
