package andicar.n.activity.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.andicar2.activity.R;

import andicar.n.activity.CommonDetailActivity;
import andicar.n.activity.CommonListActivity;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.Utils;

/**
 * Created by miki on 26.02.2017.
 */

public class BPartnerEditFragment extends BaseEditFragment {
    private Cursor cAddressCursor;
    private SimpleCursorAdapter listCursorAdapter;

    private ListView lvAddressList;
    private CheckBox ckIsGasStation;

    private boolean mIsGasStation = false;

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
            if (mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_EDIT)) {
                loadDataFromDB();
            }
        }
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();
        mIsGasStation = false;
    }

    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_BPARTNER, DBAdapter.COL_LIST_BPARTNER_TABLE, mRowId);

        assert c != null;

        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        mIsGasStation = c.getString(DBAdapter.COL_POS_BPARTNER__ISGASSTATION).equals("Y");
        c.close();

        fillAddressList();
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);
        ckIsGasStation = mRootView.findViewById(R.id.ckIsGasStation);
        lvAddressList = mRootView.findViewById(R.id.lvAddressList);
        lvAddressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(BPartnerEditFragment.this.getActivity(), CommonDetailActivity.class);
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_BPARTNER_LOCATION);
                intent.putExtra(BaseEditFragment.RECORD_ID_KEY, l);
                BPartnerEditFragment.this.startActivityForResult(intent, 1);
            }
        });

        ImageButton btnNewAddress = mRootView.findViewById(R.id.btnNewAddress);
        btnNewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BPartnerEditFragment.this.getActivity(), CommonDetailActivity.class);
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_BPARTNER_LOCATION);
                intent.putExtra(BaseEditFragment.RECORD_ID_KEY, -1L);
                intent.putExtra(BaseEditFragment.BPARTNER_ID_KEY, mRowId);
                BPartnerEditFragment.this.startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void initSpecificControls() {

    }

    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        acUserComment.setText(mUserComment);
        ckIsActive.setChecked(mIsActive);
        ckIsGasStation.setChecked(mIsGasStation);
        showAddresses();
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
        cvData.put(DBAdapter.COL_NAME_BPARTNER__ISGASSTATION, (ckIsGasStation.isChecked() ? "Y" : "N"));

        int dbRetVal;
        String errMsg;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_BPARTNER, cvData)).intValue();
            if (dbRetVal > 0) {
                //update the locations created before the partner was saved
                String[] selectionArgs = {"-1"};
                ContentValues newContent = new ContentValues();
                newContent.put(DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID, Long.toString(dbRetVal));
                mDbAdapter.updateRecords(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID + " = ? ", selectionArgs, newContent);
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
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_BPARTNER, mRowId, cvData);
            if (dbRetVal != -1) {
                errMsg = mResource.getString(dbRetVal);
                if (dbRetVal == R.string.error_000) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            fillAddressList();
            showAddresses();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cAddressCursor != null) {
            cAddressCursor.close();
        }
        listCursorAdapter = null;
    }

    private void showAddresses() {
        lvAddressList.setAdapter(null);
        lvAddressList.setAdapter(listCursorAdapter);
    }

    private void fillAddressList() {
        String selection = DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID + "= ? ";
        String[] selectionArgs = {Long.toString(mRowId)};

        String columns[] = {DBAdapter.COL_NAME_GEN_ROWID, DBAdapter.COL_NAME_GEN_NAME, DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS};
        cAddressCursor = mDbAdapter.query(DBAdapter.TABLE_NAME_BPARTNERLOCATION, columns, selection, selectionArgs,
                DBAdapter.COL_NAME_GEN_NAME + ", " + DBAdapter.COL_NAME_GEN_ISACTIVE + " DESC");

        int listLayout = android.R.layout.simple_list_item_2;
        //noinspection deprecation
        listCursorAdapter = new SimpleCursorAdapter(getActivity(), listLayout, cAddressCursor, new String[]{DBAdapter.COL_NAME_GEN_NAME,
                DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS}, new int[]{android.R.id.text1, android.R.id.text2});
    }
}
