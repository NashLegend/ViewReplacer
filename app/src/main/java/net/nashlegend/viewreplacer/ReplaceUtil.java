package net.nashlegend.viewreplacer;

import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import java.lang.reflect.Field;

/**
 * @author 潘志会 @ Zhihu Inc.
 * @since 01-20-2017
 */

public class ReplaceUtil {
	/**
	 * 通过反射的方式设置Factory2
	 */
	public static void injectWithReflect(AppCompatActivity activity) {
		try {
			LayoutInflater.Factory2 factory2 = activity.getLayoutInflater().getFactory2();
			if (factory2 != null) {
				Factory2Wrapper wrapper = new Factory2Wrapper(activity.getWindow());
				Field fieldFactory2 = LayoutInflater.class.getDeclaredField("mFactory2");
				fieldFactory2.setAccessible(true);
				fieldFactory2.set(activity.getLayoutInflater(), wrapper);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 通过抢先设置Factory的方式，要在super.onCreate(savedInstanceState)之前调用，应该用这个更安全
	 */
	public static void installViewFactoryBefore(AppCompatActivity activity) {
		AppCompatDelegate delegate = activity.getDelegate();
		if (delegate instanceof LayoutInflaterFactory) {
			LayoutInflaterFactory factory = (LayoutInflaterFactory) delegate;
			LayoutInflaterFactoryWrapper wrapper =
				new LayoutInflaterFactoryWrapper(factory, activity.getWindow());
			LayoutInflater layoutInflater = LayoutInflater.from(activity);
			if (layoutInflater.getFactory() == null) {
				LayoutInflaterCompat.setFactory(layoutInflater, wrapper);
			}
		}
	}
}
