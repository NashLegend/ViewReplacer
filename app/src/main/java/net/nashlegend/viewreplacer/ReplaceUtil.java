package net.nashlegend.viewreplacer;

import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;

/**
 * @author 潘志会 @ Zhihu Inc.
 * @since 01-20-2017
 */

public class ReplaceUtil {
	/**
	 * 通过抢先设置Factory的方式，要在super.onCreate(savedInstanceState)之前调用，应该用这个更安全
	 */
	public static void installViewFactoryBefore(AppCompatActivity activity) {
		AppCompatDelegate delegate = activity.getDelegate();
		if (delegate instanceof LayoutInflater.Factory2) {
			LayoutInflater.Factory2 factory = (LayoutInflater.Factory2) delegate;
			LayoutInflaterFactoryWrapper wrapper =
				new LayoutInflaterFactoryWrapper(factory, activity.getWindow());
			LayoutInflater layoutInflater = LayoutInflater.from(activity);
			if (layoutInflater.getFactory() == null) {
				LayoutInflaterCompat.setFactory2(layoutInflater, wrapper);
			}
		}
	}
}
