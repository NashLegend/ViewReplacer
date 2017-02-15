package net.nashlegend.viewreplacer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static android.support.v7.appcompat.R.styleable;

/**
 * @author 潘志会
 * @since 01-17-2017
 */
@SuppressWarnings("RestrictedApi")
public class ReplaceInflater {

	private static final Class<?>[] sConstructorSignature = new Class[] {
		Context.class, AttributeSet.class
	};
	private static final int[] sOnClickAttrs = new int[] { android.R.attr.onClick };

	private static final String[] sClassPrefixList = {
		"android.widget.", "android.view.", "android.webkit."
	};

	private static final Map<String, Constructor<? extends View>> sConstructorMap =
		new ArrayMap<>();

	private final Object[] mConstructorArgs = new Object[2];

	public final View createView(View parent, final String name, @NonNull Context context,
		@NonNull AttributeSet attrs, boolean inheritContext, boolean readAndroidTheme) {

		//作用是在5.0以下，把带android:theme传给子控件
		if (inheritContext && parent != null) {
			context = parent.getContext();
		}
		context = themifyContext(context, attrs, readAndroidTheme);

		View view = null;

		// 在这里替换View
		switch (name) {
			case "TextView":
				view = new TTextView(context, attrs);
				break;
		}

		if (view != null) {
			//处理android:onClick属性
			checkOnClickListener(view, attrs);
		}

		return view;
	}

	private View createViewFromTag(Context context, String name, AttributeSet attrs) {
		if (name.equals("view")) {
			name = attrs.getAttributeValue(null, "class");
		}
		try {
			mConstructorArgs[0] = context;
			mConstructorArgs[1] = attrs;
			if (-1 == name.indexOf('.')) {
				for (String classPrefix : sClassPrefixList) {
					final View view = createView(context, name, classPrefix);
					if (view != null) {
						return view;
					}
				}
				return null;
			} else {
				return createView(context, name, null);
			}
		} catch (Exception e) {
			//创建View失败则直接交给下一下处理者
			return null;
		} finally {
			mConstructorArgs[0] = null;
			mConstructorArgs[1] = null;
		}
	}

	/**
	 * 如果有android:onClick，就给处理它的点击事件
	 */
	private void checkOnClickListener(View view, AttributeSet attrs) {
		final Context context = view.getContext();

		if (!(context instanceof ContextWrapper) || (Build.VERSION.SDK_INT >= 15
			&& !ViewCompat.hasOnClickListeners(view))) {
			// 如果Context不是ContextWrapper, 或者没有点击事件并且API>=15
			// （因为hasOnClickListeners在API15才有的）则不管这个android:onClick属性了
			return;
		}

		final TypedArray a = context.obtainStyledAttributes(attrs, sOnClickAttrs);
		final String handlerName = a.getString(0);
		if (handlerName != null) {
			view.setOnClickListener(new DeclaredOnClickListener(view, handlerName));
		}
		a.recycle();
	}

	/**
	 * 根据类名通过反射实例化View
	 */
	private View createView(Context context, String name, String prefix)
		throws ClassNotFoundException, InflateException {
		Constructor<? extends View> constructor = sConstructorMap.get(name);
		try {
			if (constructor == null) {
				Class<? extends View> clazz = context.getClassLoader()
					.loadClass(prefix != null ? (prefix + name) : name)
					.asSubclass(View.class);
				constructor = clazz.getConstructor(sConstructorSignature);
				sConstructorMap.put(name, constructor);
			}
			constructor.setAccessible(true);
			return constructor.newInstance(mConstructorArgs);
		} catch (Exception e) {
			//返回null，并由系统的Inflater处理它
			return null;
		}
	}

	/**
	 * 只有5.0以前才会读取theme属性，先读android:theme没有再读app:theme
	 * <p>
	 * 如果5.0以后强行写成app:theme也会读，但是跟不写效果是一样的
	 */
	@SuppressLint("PrivateResource")
	private static Context themifyContext(Context context,
		AttributeSet attrs, boolean useAndroidTheme) {
		final TypedArray a = context.obtainStyledAttributes(attrs, styleable.View, 0, 0);
		int themeId = 0;
		if (useAndroidTheme) {
			//读取app:theme
			themeId = a.getResourceId(styleable.View_android_theme, 0);
		}
		if (themeId == 0) {
			//读取app:theme，但是读取android:theme就可以了，app:theme过时了不建议使用，虽然效果是一样的
			themeId = a.getResourceId(styleable.View_theme, 0);
		}
		a.recycle();
		//如果发现用户设置了theme属性，
		if (themeId != 0 && (!(context instanceof ContextThemeWrapper)
			|| ((ContextThemeWrapper) context).getThemeResId() != themeId)) {
			context = new ContextThemeWrapper(context, themeId);
		}
		return context;
	}

	/**
	 * 解决android:onClick属性
	 */
	private static class DeclaredOnClickListener implements View.OnClickListener {
		private final View mHostView;
		private final String mMethodName;

		private Method mResolvedMethod;
		private Context mResolvedContext;

		public DeclaredOnClickListener(@NonNull View hostView, @NonNull String methodName) {
			mHostView = hostView;
			mMethodName = methodName;
		}

		@Override
		public void onClick(@NonNull View v) {
			if (mResolvedMethod == null) {
				resolveMethod(mHostView.getContext(), mMethodName);
			}
			try {
				mResolvedMethod.invoke(mResolvedContext, v);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(
					"Could not execute non-public method for android:onClick", e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException("Could not execute method for android:onClick", e);
			}
		}

		private void resolveMethod(@Nullable Context context, @NonNull String name) {
			while (context != null) {
				try {
					if (!context.isRestricted()) {
						final Method method = context.getClass().getMethod(mMethodName, View.class);
						if (method != null) {
							mResolvedMethod = method;
							mResolvedContext = context;
							return;
						}
					}
				} catch (NoSuchMethodException e) {
					// 找不到的话，就找baseContext
				}

				if (context instanceof ContextWrapper) {
					context = ((ContextWrapper) context).getBaseContext();
				} else {
					// 仍然找不到，要抛出了
					context = null;
				}
			}

			//仍然没找到，直接抛错
			final int id = mHostView.getId();
			final String idText = id == View.NO_ID ? "" : " with id '"
				+ mHostView.getContext().getResources().getResourceEntryName(id)
				+ "'";
			throw new IllegalStateException("Could not find method "
				+ mMethodName
				+ "(View) in a parent or ancestor Context for android:onClick "
				+ "attribute defined on view "
				+ mHostView.getClass()
				+ idText);
		}
	}
}
