package net.nashlegend.viewreplacer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory2;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Factory2 factory2 = getLayoutInflater().getFactory2();
			if (factory2 != null) {
				Factory2Wrapper wrapper = new Factory2Wrapper(getWindow());
				Field fieldFactory2 = LayoutInflater.class.getDeclaredField("mFactory2");
				fieldFactory2.setAccessible(true);
				fieldFactory2.set(getLayoutInflater(), wrapper);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		setContentView(R.layout.activity_main);
	}
}
