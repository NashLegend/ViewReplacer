package net.nashlegend.viewreplacer;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater.Factory2;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;

/**
 * @author 潘志会
 * @since 01-17-2017
 */

public class Factory2Wrapper implements Factory2 {

	private ReplaceInflater mInflater = new ReplaceInflater();

	private Factory2 originalFactory2;
	private Window mWindow;

	public Factory2Wrapper(Window window) {
		mWindow = window;
		originalFactory2 = window.getLayoutInflater().getFactory2();
	}

	@Override
	public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
		View view = replaceView(parent, name, context, attrs);
		if (view == null) {
			view = originalFactory2.onCreateView(parent, name, context, attrs);
		}
		return view;
	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		return originalFactory2.onCreateView(name, context, attrs);
	}

	private View replaceView(View parent, String name, Context context, AttributeSet attrs) {
		final boolean isPre21 = Build.VERSION.SDK_INT < 21;
		// 5.0之前的且父控件不为空的且所在View树没有添加到Window上的会继承父控件的Context
		final boolean inheritContext = isPre21 && shouldInheritContext((ViewParent) parent);
		return mInflater.createView(parent, name, context, attrs, inheritContext, isPre21);
	}

	/**
	 * 如果View所在的树并没有加到Window上，则返回true
	 */
	private boolean shouldInheritContext(ViewParent parent) {
		if (parent == null) {
			return false;
		}
		final View windowDecor = mWindow.getDecorView();
		while (true) {
			if (parent == null) {
				return true;
			} else if (parent == windowDecor
				|| !(parent instanceof View)
				|| ViewCompat.isAttachedToWindow((View) parent)) {
				return false;
			}
			parent = parent.getParent();
		}
	}
}