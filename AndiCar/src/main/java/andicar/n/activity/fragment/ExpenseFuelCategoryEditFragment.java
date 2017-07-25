package andicar.n.activity.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CheckBox;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.Utils;

/**
 * Created by miki on 30.01.2017.
 */

public class ExpenseFuelCategoryEditFragment extends BaseEditFragment {
    private boolean mIsExcludeFromMileageCost;

    private CheckBox ckIsExcludeFromMileageCost;
    private boolean mIsFuel;

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
        else {
            mIsFuel = savedInstanceState.getBoolean(BaseEditFragment.IS_FUEL_KEY);
        }
    }

    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_EXPENSECATEGORY, DBAdapter.COL_LIST_EXPENSECATEGORY_TABLE, mRowId);

        assert c != null;

        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        mIsFuel = c.getString(DBAdapter.COL_POS_EXPENSECATEGORY__ISFUEL).equals("Y");
        mIsExcludeFromMileageCost = c.getString(DBAdapter.COL_POS_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST).equals("Y");
        c.close();
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        mIsFuel = mArgumentsBundle.getBoolean(BaseEditFragment.IS_FUEL_KEY);
        mIsExcludeFromMileageCost = false;
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);
        ckIsExcludeFromMileageCost = mRootView.findViewById(R.id.ckIsExcludeFromMileageCost);
    }

    @Override
    protected void initSpecificControls() {

    }

    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        acUserComment.setText(mUserComment);
        ckIsActive.setChecked(mIsActive);
        ckIsExcludeFromMileageCost.setChecked(mIsExcludeFromMileageCost);
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
        cvData.put(DBAdapter.COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST, (ckIsExcludeFromMileageCost.isChecked() ? "Y" : "N"));
        cvData.put(DBAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL, mIsFuel ? "Y" : "N");

        int dbRetVal;
        String errMsg;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_EXPENSECATEGORY, cvData)).intValue();
            if (dbRetVal > 0) {
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
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_EXPENSECATEGORY, mRowId, cvData);
            if (dbRetVal != -1) {
                errMsg = mResource.getString(dbRetVal);
                if (dbRetVal == R.string.error_000) {
                    errMsg = errMsg + "\n" + mDbAdapter.mErrorMessage;
                }
                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), errMsg, mDbAdapter.mException, false);
                return false;
            }
            else {
                return true;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BaseEditFragment.IS_FUEL_KEY, mIsFuel);
    }
}
