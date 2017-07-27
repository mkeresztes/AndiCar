/*
 * AndiCar
 *
 *  Copyright (c) 2016 Miklos Keresztes (miklos.keresztes@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package andicar.n.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.andicar2.activity.R;

import java.util.ArrayList;

import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 6/15/16.
 */
public class MainNavigationView extends android.support.design.widget.NavigationView {
    public static final int MENU_TYPE_SECONDARY = 1;
    public static final int MENU_ADD_ID = -9999;
    private static final int MENU_TYPE_PRIMARY = 0;
    private final ArrayList<SecondaryMenuEntry> mSecondaryMenuEntryList = new ArrayList<>();
    public boolean mForceSecondary = false; //used to force showing only the secondary menu if no car exists
    private int mMenuType = MENU_TYPE_PRIMARY;
    private ImageView mArrowIcon;
    private TextView mDrawerTitle;
    private TextView mLabel1;
    private TextView mLabel2;

    public MainNavigationView(Context context) {
        super(context);
    }

    public MainNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * add a new menu entry in the secondary list
     * call changeMenuLayout to force a redraw after changing the items
     */
    public void addSecondaryMenuEntry(int id, String title, @SuppressWarnings("SameParameterValue") int iconResource) {
        mSecondaryMenuEntryList.add(new SecondaryMenuEntry(id, title, iconResource));
    }

    /**
     * clear the items from the secondary list
     */
    public void clearSecondaryMenuEntries() {
        mSecondaryMenuEntryList.clear();
    }

    /**
     * Change the navigation menu items between default view and car list view
     */
    public void changeMenuLayout() {
        if (mForceSecondary) {
            changeMenuLayout(MENU_TYPE_SECONDARY);
        }
        else {
            if (this.getMenuType() == MENU_TYPE_PRIMARY) {
                changeMenuLayout(MENU_TYPE_SECONDARY);
            }
            else {
                changeMenuLayout(MENU_TYPE_PRIMARY);
            }
        }
    }

    public int getMenuType() {
        return mMenuType;
    }

    private void setMenuType(int mMenuType) {
        if (mForceSecondary) {
            this.mMenuType = MENU_TYPE_SECONDARY;
        }
        else {
            this.mMenuType = mMenuType;
        }
    }

    public void changeMenuLayout(int layoutId) {
        if (layoutId == MENU_TYPE_SECONDARY) {
            //change to secondary (car) list
            this.getMenu().clear();
            this.inflateMenu(R.menu.main_navigation_secondary_items);
            mArrowIcon.setImageResource(R.drawable.ic_menu_drop_up_white);

            //add the list of cars (dynamic list)
            for (SecondaryMenuEntry temp : mSecondaryMenuEntryList) {
                this.getMenu().add(R.id.mnu_secondary_list_group, temp.menuItemId, Menu.NONE, temp.menuItemTitle).setIcon(temp.menuItemIconResource);
            }

            //add the new item option
            this.getMenu().add(Menu.NONE, MENU_ADD_ID, Menu.NONE, R.string.gen_add_new).setIcon(R.drawable.ic_menu_add_circle_outline_black);
            if (mSecondaryMenuEntryList.size() == 0) //no car => also show the settings menu
            {
                this.getMenu().add(Menu.NONE, R.id.nav_settings, Menu.NONE, R.string.gen_settings).setIcon(R.drawable.ic_menu_settings_black);
            }
            this.setMenuType(MENU_TYPE_SECONDARY);
        }
        else {
            //change to default layout
            this.getMenu().clear();
            this.inflateMenu(R.menu.main_navigation_primary_items);
            if (this.getMenu().findItem(R.id.nav_rate) != null && Utils.isCanSHowRateApp(getContext())) {
                this.getMenu().findItem(R.id.nav_rate).setVisible(true);
            }
            mArrowIcon.setImageResource(R.drawable.ic_menu_drop_down_white);
            this.setMenuType(MENU_TYPE_PRIMARY);
        }
    }


    //commented out because frequent crashes (see https://issuetracker.google.com/issues/37073849)
//    @Override
//    protected Parcelable onSaveInstanceState() {
//        /*
//         * don't ask me what's this.
//         * See http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
//         */
//        //begin boilerplate code that allows parent classes to save state
//        Parcelable superState = super.onSaveInstanceState();
//
//        SavedState ss = new SavedState(superState);
//        //end
//        Bundle savedState = new Bundle();
//        savedState.putInt("mMenuType", mMenuType);
//        savedState.putString("mHeaderTitle", mDrawerTitle.getText().toString());
//        savedState.putString("mHeaderLabel1", mLabel1.getText().toString());
//        savedState.putString("mHeaderLabel2", mLabel2.getText().toString());
//        ss.savedState = savedState;
//
//        return ss;
//
//    }

//    @Override
//    protected void onRestoreInstanceState(Parcelable savedState) {
//        //begin boilerplate code so parent classes can restore state
//        if (!(savedState instanceof SavedState)) {
//            super.onRestoreInstanceState(savedState);
//            return;
//        }
//
//        SavedState ss = (SavedState) savedState;
//        super.onRestoreInstanceState(ss.getSuperState());
//        //end
//
//        changeMenuLayout(ss.savedState.getInt("mMenuType"));
//        setHeaderLabels(ss.savedState.getString("mHeaderTitle"), ss.savedState.getString("mHeaderLabel1"), ss.savedState.getString("mHeaderLabel2"));
//
//    }

    public void setHeaderLabels(String title, String label1, String label2) {
        mDrawerTitle.setText(title);
        mLabel1.setText(label1);
        mLabel2.setText(label2);
    }

    @Override
    public View inflateHeaderView(@LayoutRes int res) {
        View header = super.inflateHeaderView(res);
        mArrowIcon = header.findViewById(R.id.img_view_arrow_up_down);
        mLabel1 = header.findViewById(R.id.navLabel1);
        mLabel2 = header.findViewById(R.id.navLabel2);
        mDrawerTitle = header.findViewById(R.id.drawer_title);
        return header;
    }

    /**
     * don't ask me what's this.
     * See http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
     */

    //commented out because frequent crashes (see https://issuetracker.google.com/issues/37073849)
//    private static class SavedState extends BaseSavedState {
//        //required field that makes Parcelables from a Parcel
//        public static final Parcelable.Creator<SavedState> CREATOR =
//                new Parcelable.Creator<SavedState>() {
//                    public SavedState createFromParcel(Parcel in) {
//                        try {
//                            return new SavedState(in);
//                        } catch (Exception e) {
//                            Utils.showReportableErrorDialog(AndiCar.getAppContext(), e.getMessage(), null, e, false);
//                            return null;
//                        }
//                    }
//
//                    public SavedState[] newArray(int size) {
//                        return new SavedState[size];
//                    }
//                };
//        Bundle savedState = new Bundle();
//
//        SavedState(Parcelable superState) {
//            super(superState);
//        }
//
//        private SavedState(Parcel in) {
//            super(in);
//            this.savedState = in.readBundle(getClass().getClassLoader());
//        }
//
//        @Override
//        public void writeToParcel(Parcel out, int flags) {
//            super.writeToParcel(out, flags);
//            out.writeBundle(this.savedState);
//        }
//    }
//
    @SuppressWarnings("WeakerAccess")
    private class SecondaryMenuEntry {
        final int menuItemId;
        final String menuItemTitle;
        final int menuItemIconResource;

        public SecondaryMenuEntry(int id, String menuItemTitle, int menuItemIconResource) {
            this.menuItemId = id;
            this.menuItemTitle = menuItemTitle;
            this.menuItemIconResource = menuItemIconResource;
        }
    }
}
