package iAndroid.what2wear;


import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class mainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.our_main_layout);
        
        Button go_to_search_by_item = (Button) findViewById(R.id.go_to_search_by_item);
        go_to_search_by_item.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), searchItemActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }

}