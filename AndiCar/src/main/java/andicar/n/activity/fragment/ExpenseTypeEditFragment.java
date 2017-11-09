package andicar.n.activity.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.Utils;

/**
 * Created by miki on 01.02.2017.
 */

public class ExpenseTypeEditFragment extends BaseEditFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            switch (mOperationType) {
                case BaseEditFragment.DETAIL_OPERATION_EDIT: {
                    loadDataFromDB();
                    break;
                }
                default: //new record
                    initDefaultValues();
                    break;
            }
        }
    }

    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_EXPENSETYPE, DBAdapter.COL_LIST_EXPENSETYPE_TABLE, mRowId);

        assert c != null;

        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        c.close();
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);
    }

    @Override
    protected void initSpecificControls() {

    }

    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        acUserComment.setText(mUserComment);
        ckIsActive.setChecked(mIsActive);
    }

    @Override
    public void setSpecificLayout() {

    }

    @Override
    protected boolean saveData() {
        ContentValues data = new ContentValues();
        data.put(DBAdapter.COL_NAME_GEN_NAME, etName.getText().toString());
        data.put(DBAdapter.COL_NAME_GEN_ISACTIVE, (ckIsActive.isChecked() ? "Y" : "N"));
        data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());

        int dbRetVal;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_EXPENSETYPE, data)).intValue();
            if (dbRetVal > 0) {
                return true;
            }
            else {
                if (dbRetVal == -1) //DB Error
                {
                    Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException);
                }
                else //precondition error
                {
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(-1 * dbRetVal));
                }

                return false;
            }
        }
        else {
            int updResult = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_EXPENSETYPE, mRowId, data);
            if (updResult != -1) {
                String errMsg;
                errMsg = mResource.getString(updResult);
                if (updResult == R.string.error_000) {
                    errMsg = errMsg + "\n" + mDbAdapter.mErrorMessage;
                }
                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), errMsg, mDbAdapter.mException);
                return false;
            }
            else {
                return true;
            }
        }
    }
}
