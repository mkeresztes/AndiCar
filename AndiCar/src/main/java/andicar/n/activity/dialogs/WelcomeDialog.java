package andicar.n.activity.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;

import org.andicar2.activity.R;

import andicar.n.activity.CommonDetailActivity;
import andicar.n.activity.CommonListActivity;
import andicar.n.activity.fragment.BaseEditFragment;
import andicar.n.activity.miscellaneous.FileListActivity;

/**
 * Created by Miklos Keresztes on 01.11.2017.
 */

public class WelcomeDialog extends Dialog {
    public WelcomeDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_welcome);

        findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

        findViewById(R.id.btnAddCar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), CommonDetailActivity.class);
                i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_CAR);
                i.putExtra(BaseEditFragment.RECORD_ID_KEY, -1L);
                i.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_NEW);
                getContext().startActivity(i);
                hide();
            }
        });

        findViewById(R.id.btnRestore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), FileListActivity.class);
                i.putExtra(FileListActivity.file_type_extras_key, FileListActivity.LIST_TYPE_BACKUP);
                getContext().startActivity(i);
                hide();
            }
        });
    }
}
