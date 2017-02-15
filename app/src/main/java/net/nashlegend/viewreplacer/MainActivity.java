package net.nashlegend.viewreplacer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	@Override protected void onCreate(Bundle savedInstanceState) {
		ReplaceUtil.installViewFactoryBefore(this);
		super.onCreate(savedInstanceState);
		//ReplaceUtil.injectWithReflect(this);
		setContentView(R.layout.activity_main);
		findViewById(R.id.button1).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.mai);
		for (int i = 0; i < layout.getChildCount(); i++) {
			System.out.println(layout.getChildAt(i));
		}
	}
}
