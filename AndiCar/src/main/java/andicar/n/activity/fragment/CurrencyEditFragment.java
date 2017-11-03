package andicar.n.activity.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.Utils;

import static android.app.Activity.RESULT_OK;

/**
 * Created by miki on 20.02.2017.
 */

public class CurrencyEditFragment extends BaseEditFragment {
    private String mCode;

    private EditText etCode;

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
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_CURRENCY, DBAdapter.COL_LIST_CURRENCY_TABLE, mRowId);

        assert c != null;

        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mCode = c.getString(DBAdapter.COL_POS_CURRENCY__CODE);
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        c.close();
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        mCode = null;
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);
        etCode = mRootView.findViewById(R.id.etCode);
    }

    @Override
    protected void initSpecificControls() {

    }

    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        etCode.setText(mCode);
        acUserComment.setText(mUserComment);
        ckIsActive.setChecked(mIsActive);
    }

    @Override
    public void setSpecificLayout() {

    }

    @Override
    protected boolean saveData() {
        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_GEN_NAME, etName.getText().toString());
        cvData.put(DBAdapter.COL_NAME_GEN_ISACTIVE, (ckIsActive.isChecked() ? "Y" : "N"));
        cvData.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        cvData.put(DBAdapter.COL_NAME_CURRENCY__CODE, etCode.getText().toString());

        int dbRetVal;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_CURRENCY, cvData)).intValue();
            if (dbRetVal > 0) {
                if (getActivity() != null)
                    getActivity().setResult(RESULT_OK, (new Intent()).putExtra("mRowId", dbRetVal));
                return true;
            }
            else {
                if (dbRetVal == -1) //DB Error
                {
                    Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException, false);
                }
                else //precondition error
                {
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(-1 * dbRetVal), false);
                }
                return false;
            }
        }
        else {
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_CURRENCY, mRowId, cvData);
            if (dbRetVal != -1) {
                Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(dbRetVal), false);
                return false;
            }
            else {
                return true;
            }
        }
    }
}
