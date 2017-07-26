package net.nashlegend.viewreplacer;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * @author NashLegend
 * @since 01-17-2017
 */

public class TTextView extends android.support.v7.widget.AppCompatEditText {
	public TTextView(Context context) {
		super(context);
	}

	public TTextView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		setTextColor(Color.parseColor("#fff000"));
	}

	public TTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
}
