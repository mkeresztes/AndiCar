/*
 * AndiCar
 *
 *  Copyright (c) 2017 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package andicar.n.activity.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import org.andicar2.activity.R;

/**
 * Created by Miklos Keresztes on 09.06.2017.
 */

public class ShareDialogFragment extends DialogFragment {

    public static final String SHARE_FORMAT_KEY = "ShareFormat";

    private Spinner spnReportFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_share, container);

        getDialog().setTitle(R.string.share_dialog_title);

        setCancelable(false);

        View fakeFocus = view.findViewById(R.id.fakeFocus);
        if (fakeFocus != null) {
            fakeFocus.requestFocus();
        }

        spnReportFormat = view.findViewById(R.id.spnReportFormat);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button btnDone = view.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareDialogFragment.ShareDialogListener listener = (ShareDialogFragment.ShareDialogListener) getActivity();
                Bundle params = new Bundle();
                params.putInt(SHARE_FORMAT_KEY, spnReportFormat.getSelectedItemPosition());
                if (listener != null)
                    listener.onFinishShareDialog(params);
                dismiss();
            }
        });

        return view;
    }

    public interface ShareDialogListener {
        void onFinishShareDialog(Bundle searchParams);
    }
}
