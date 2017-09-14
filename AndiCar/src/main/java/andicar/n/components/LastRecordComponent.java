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

@SuppressWarnings("unused")
public class LastRecordComponent extends LinearLayout {
    Context mCtx;
    TextView lineHeader;
    TextView firstLine;
    TextView secondLine;
    TextView thirdLine;
    View buttonsLine;
    ImageButton btnMap;
    ImageButton btnEdit;
    ImageButton btnShowList;
    ImageButton btnAddNew;
    String headerText;
    String firstLineText;
    String secondLineText;
    String thirdLineText;

    private long recordId;
    private int whatEditAdd;
    private int whatList;


    public LastRecordComponent(Context context) {
        super(context);
        mCtx = context;
    }

    public LastRecordComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LastRecordComponent, 0, 0);

        try {
            headerText = a.getString(R.styleable.LastRecordComponent_headerText);
            firstLineText = a.getString(R.styleable.LastRecordComponent_firstLineText);
            secondLineText = a.getString(R.styleable.LastRecordComponent_secondLineText);
            thirdLineText = a.getString(R.styleable.LastRecordComponent_thirdLineText);
        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        View rootView = inflate(mCtx, R.layout.component_last_record, this);

        lineHeader = rootView.findViewById(R.id.lineHeader);
        firstLine = rootView.findViewById(R.id.firstLine);
        secondLine = rootView.findViewById(R.id.secondLine);
        thirdLine = rootView.findViewById(R.id.thirdLine);
        buttonsLine = rootView.findViewById(R.id.lineButtons);
        btnMap = rootView.findViewById(R.id.btnMap);
        btnEdit = rootView.findViewById(R.id.btnEdit);
        btnShowList = rootView.findViewById(R.id.btnShowList);
        btnAddNew = rootView.findViewById(R.id.btnAddNew);

        lineHeader.setText(headerText);
        firstLine.setText(firstLineText);
        if (secondLine != null)
            secondLine.setText(secondLineText);
        if (thirdLine != null)
            thirdLine.setText(thirdLineText);
    }

    public void setMapButtonOnClickListener(View.OnClickListener listener) {
        btnMap.setOnClickListener(listener);
    }

    public void setEditButtonOnClickListener(View.OnClickListener listener) {
        btnEdit.setOnClickListener(listener);
    }

    public void setShowListButtonOnClickListener(View.OnClickListener listener) {
        btnShowList.setOnClickListener(listener);
    }

    public void setAddNewButtonOnClickListener(View.OnClickListener listener) {
        btnAddNew.setOnClickListener(listener);
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
        lineHeader.setText(headerText);
    }

    public void setFirstLineText(String firstLineText) {
        this.firstLineText = firstLineText;
        firstLine.setText(firstLineText);
        if (firstLineText.contains("Draft")) {
            firstLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.holo_red_dark));
        } else {
            firstLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.primary_text_light));
        }
    }

    public void setSecondLineText(String secondLineText) {
        if (this.secondLine != null) {
            this.secondLineText = secondLineText;
            secondLine.setText(secondLineText);
            if (secondLineText.contains("Draft")) {
                secondLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.holo_red_dark));
            } else {
                secondLine.setTextColor(ContextCompat.getColor(mCtx, android.R.color.primary_text_light));
            }
        }
    }

    public void setThirdLineText(String thirdLineText) {
        if (this.secondLine != null) {
            this.thirdLineText = thirdLineText;
            thirdLine.setText(thirdLineText);
        }
    }

    public void setHeaderText(int resId) {
        this.headerText = mCtx.getString(resId);
        lineHeader.setText(this.headerText);
    }

    public void setFirstLineText(int resId) {
        this.firstLineText = mCtx.getString(resId);
        firstLine.setText(this.firstLineText);
    }

    public void setSecondLineText(int resId) {
        if (this.secondLine != null) {
            this.secondLineText = mCtx.getString(resId);
            secondLine.setText(this.secondLineText);
        }
    }

    public void setThirdLineText(int resId) {
        if (this.secondLine != null) {
            this.thirdLineText = mCtx.getString(resId);
            thirdLine.setText(this.thirdLineText);
        }
    }

    public void setMapButtonVisibility(int visibility) {
        btnMap.setVisibility(visibility);
    }

    public void setEditButtonVisibility(int visibility) {
        btnEdit.setVisibility(visibility);
    }

    public void setShowListButtonVisibility(int visibility) {
        btnShowList.setVisibility(visibility);
    }

    public void setAddNewButtonVisibility(int visibility) {
        btnAddNew.setVisibility(visibility);
    }

    public void setFirstLineVisibility(int visibility) {
        firstLine.setVisibility(visibility);
    }

    public void setSecondLineVisibility(int visibility) {
        if (secondLine != null)
            secondLine.setVisibility(visibility);
    }

    public void setThirdLineVisibility(int visibility) {
        if (thirdLine != null)
            thirdLine.setVisibility(visibility);
    }

    public void setButtonsLineVisibility(int visibility) {
        if (buttonsLine != null)
            buttonsLine.setVisibility(visibility);
    }

    public boolean isFirstLineExists() {
        return firstLine != null;
    }

    public boolean isSecondLineExists() {
        return secondLine != null;
    }

    public boolean isThirdLineExists() {
        return thirdLine != null;
    }

    public int getWhatEditAdd() {
        return whatEditAdd;
    }

    public void setWhatEditAdd(int whatEditAdd) {
        this.whatEditAdd = whatEditAdd;
    }

    public int getWhatList() {
        return whatList;
    }

    public void setWhatList(int whatList) {
        this.whatList = whatList;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

}
