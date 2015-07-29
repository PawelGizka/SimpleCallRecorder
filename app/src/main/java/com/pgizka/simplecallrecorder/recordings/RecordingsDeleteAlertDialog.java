package com.pgizka.simplecallrecorder.recordings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Paweł on 2015-07-25.
 */
public class RecordingsDeleteAlertDialog extends DialogFragment {
    OnDeleteListener onDeleteListener;

    public interface OnDeleteListener{
        public void onUserClickedDelete();
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        builder.setTitle("Are you sure you want to delete?")
                .setMessage("This wil also delte recoding file")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(onDeleteListener != null){
                            onDeleteListener.onUserClickedDelete();
                        }
                    }
                });

        return builder.create();
    }
}
