package net.nashlegend.viewreplacer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory2;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		installViewFactoryBefore();
		super.onCreate(savedInstanceState);
		//injectWithReflect();
		setContentView(R.layout.activity_main);
	}

	/**
	 * 通过反射的方式设置Factory2
	 */
	private void injectWithReflect() {
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
	}

	/**
	 * 	通过抢先设置Factory的方式，要在super.onCreate(savedInstanceState)之前调用，应该用这个更安全
	 */
	public void installViewFactoryBefore() {
		AppCompatDelegate delegate = getDelegate();
		if (delegate instanceof LayoutInflaterFactory) {
			LayoutInflaterFactory factory = (LayoutInflaterFactory) delegate;
			LayoutInflaterFactoryWrapper wrapper = new LayoutInflaterFactoryWrapper(factory, getWindow());
			LayoutInflater layoutInflater = LayoutInflater.from(this);
			if (layoutInflater.getFactory() == null) {
				LayoutInflaterCompat.setFactory(layoutInflater, wrapper);
			}
		}
	}
}
