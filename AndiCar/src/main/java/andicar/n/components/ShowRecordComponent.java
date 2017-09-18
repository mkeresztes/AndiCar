package andicar.n.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.andicar2.activity.R;

/**
 * Created by miki on 14.09.2017.
 */

public class ShowRecordComponent extends LinearLayout {
    private final Context mCtx;
    private TextView mLineHeader;
    private TextView mFirstLine;
    private TextView mSecondLine;
    private TextView mThirdLine;
    private View mButtonsLine;
    private ImageButton mMapButton;
    private ImageButton mEditButton;
    private ImageButton mhowListButton;
    private ImageButton mNewButton;
    private String mHeaderText;
    private String mFirstLineText;
    private String mSecondLineText;
    private String mThirdLineText;

    public ShowRecordComponent(Context context) {
        super(context);
        mCtx = context;
        init();
    }

    public ShowRecordComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ShowRecordComponent, 0, 0);

        try {
            mHeaderText = a.getString(R.styleable.ShowRecordComponent_headerText);
            mFirstLineText = a.getString(R.styleable.ShowRecordComponent_firstLineText);
            mSecondLineText = a.getString(R.styleable.ShowRecordComponent_secondLineText);
            mThirdLineText = a.getString(R.styleable.ShowRecordComponent_thirdLineText);
        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        View rootView = inflate(mCtx, R.layout.component_show_record, this);

        mLineHeader = rootView.findViewById(R.id.lineHeader);
        mFirstLine = rootView.findViewById(R.id.firstLine);
        mSecondLine = rootView.findViewById(R.id.secondLine);
        mThirdLine = rootView.findViewById(R.id.thirdLine);
        mButtonsLine = rootView.findViewById(R.id.lineButtons);
        mMapButton = rootView.findViewById(R.id.btnMap);
        mEditButton = rootView.findViewById(R.id.btnEdit);
        mhowListButton = rootView.findViewById(R.id.btnShowList);
        mNewButton = rootView.findViewById(R.id.btnAddNew);

        mLineHeader.setText(mHeaderText);
        mFirstLine.setText(mFirstLineText);
        if (mSecondLine != null) {
            mSecondLine.setText(mSecondLineText);
        }
        if (mThirdLine != null) {
            mThirdLine.setText(mThirdLineText);
        }
    }

    public void setMapButtonOnClickListener(View.OnClickListener listener) {
        mMapButton.setOnClickListener(listener);
    }

    public void setEditButtonOnClickListener(View.OnClickListener listener) {
        mEditButton.setOnClickListener(listener);
    }

    public void setShowListButtonOnClickListener(View.OnClickListener listener) {
        mhowListButton.setOnClickListener(listener);
    }

    public void setAddNewButtonOnClickListener(View.OnClickListener listener) {
        mNewButton.setOnClickListener(listener);
    }

    public void setHeaderText(int resId) {
        this.mHeaderText = mCtx.getString(resId);
        mLineHeader.setText(this.mHeaderText);
    }

    public void setFirstLineText(String firstLineText) {
        this.mFirstLineText = firstLineText;
        if (firstLineText == null || firstLineText.trim().length() == 0) {
            mFirstLine.setVisibility(GONE);
        }
        else {
            mFirstLine.setVisibility(VISIBLE);
            mFirstLine.setText(firstLineText);
            if (firstLineText.contains("Draft")) {
                mFirstLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.holo_red_dark));
            }
            else {
                mFirstLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.primary_text_light));
            }
        }
    }

    public void setFirstLineText(CharSequence firstLineText) {
        this.mFirstLineText = firstLineText.toString();
        if (firstLineText.toString().trim().length() == 0) {
            mFirstLine.setVisibility(GONE);
        }
        else {
            mFirstLine.setVisibility(VISIBLE);
            mFirstLine.setText(firstLineText);
            if (this.mFirstLineText.contains("Draft")) {
                mFirstLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.holo_red_dark));
            }
            else {
                mFirstLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.primary_text_light));
            }
        }
    }

    public void setFirstLineText(@SuppressWarnings("SameParameterValue") int resId) {
        this.mFirstLineText = mCtx.getString(resId);
        mFirstLine.setText(this.mFirstLineText);
    }

    public void setSecondLineText(String secondLineText) {
        if (this.mSecondLine != null) {
            this.mSecondLineText = secondLineText;
            if (secondLineText == null || secondLineText.trim().length() == 0) {
                mSecondLine.setVisibility(GONE);
            }
            else {
                mSecondLine.setVisibility(VISIBLE);
                mSecondLine.setText(secondLineText);
                if (secondLineText.contains("Draft")) {
                    mSecondLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.holo_red_dark));
                }
                else {
                    mSecondLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.primary_text_light));
                }
            }
        }
    }

    public void setThirdLineText(String thirdLineText) {
        if (this.mThirdLine != null) {
            this.mThirdLineText = thirdLineText;
            if (thirdLineText == null || thirdLineText.trim().length() == 0) {
                mThirdLine.setVisibility(GONE);
            }
            else {
                mThirdLine.setVisibility(VISIBLE);
                mThirdLine.setText(thirdLineText);
            }
        }
    }

    public void setMapButtonVisibility(int visibility) {
        mMapButton.setVisibility(visibility);
    }

    public void setButtonsLineVisibility(int visibility) {
        if (mButtonsLine != null) {
            mButtonsLine.setVisibility(visibility);
        }
    }

    public boolean isSecondLineExists() {
        return mSecondLine != null;
    }

    public boolean isThirdLineExists() {
        return mThirdLine != null;
    }

    public void setWhatEditAdd(int whatEditAdd) {
        mNewButton.setTag(R.string.record_component_table_key, whatEditAdd);
        mEditButton.setTag(R.string.record_component_table_key, whatEditAdd);
    }

    public void setWhatList(int whatList) {
        mhowListButton.setTag(R.string.record_component_table_key, whatList);
    }

    public void setRecordId(long recordId) {
        mEditButton.setTag(R.string.record_component_record_id_key, recordId);
        mMapButton.setTag(R.string.record_component_record_id_key, recordId);
    }

    public TextView getFirstLine() {
        return mFirstLine;
    }
}
