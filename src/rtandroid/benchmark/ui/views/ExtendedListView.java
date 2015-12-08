package rtandroid.benchmark.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import rtandroid.benchmark.R;

/**
 * Extends default list by allowing header and footer declaration in xml layout.
 */
public class ExtendedListView extends ListView {

    public ExtendedListView(Context context) {
        this(context, null);
    }

    public ExtendedListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public ExtendedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExtendedListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExtendedListView, defStyleAttr, defStyleRes);

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            int header = a.getResourceId(R.styleable.ExtendedListView_header, View.NO_ID);
            if(header != View.NO_ID) {
                View headerView = inflater.inflate(header, this, false);
                addHeaderView(headerView);
            }

            int header2 = a.getResourceId(R.styleable.ExtendedListView_header2, View.NO_ID);
            if(header2 != View.NO_ID) {
                View headerView = inflater.inflate(header2, this, false);
                addHeaderView(headerView);
            }

            int footer = a.getResourceId(R.styleable.ExtendedListView_footer, View.NO_ID);
            if(footer != View.NO_ID) {
                View footerView = inflater.inflate(footer, this, false);
                addFooterView(footerView);
            }

            int footer2 = a.getResourceId(R.styleable.ExtendedListView_footer2, View.NO_ID);
            if(footer2 != View.NO_ID) {
                View footerView = inflater.inflate(footer2, this, false);
                addFooterView(footerView);
            }
        }
        finally {
            a.recycle();
        }
    }
}
