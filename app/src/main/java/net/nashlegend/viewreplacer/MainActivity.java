package net.nashlegend.viewreplacer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		ReplaceUtil.installViewFactoryBefore(this);
		super.onCreate(savedInstanceState);
		//ReplaceUtil.injectWithReflect(this);
		setContentView(R.layout.activity_main);
	}
}
