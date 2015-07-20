package com.pgizka.simplecallrecorder.options;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;

/**
 * Created by Paweł on 2015-07-20.
 */
public class OptionsListAdapter extends CursorAdapter {
    static final String TAG = OptionsListAdapter.class.getSimpleName();
    OnDeleteButtonClickListener clickListener;

    public OptionsListAdapter(Context context, Cursor c, OnDeleteButtonClickListener listener) {
        super(context, c);
        clickListener = listener;
    }

    static class ViewHolder{
        ImageView mainImage;
        TextView displayNameText, phoneText;
        Button deleteButton;

        public ViewHolder(View view){
            mainImage = (ImageView) view.findViewById(R.id.options_item_main_image);
            displayNameText = (TextView) view.findViewById(R.id.options_item_display_name_text);
            phoneText = (TextView) view.findViewById(R.id.options_item_phone_text);
            deleteButton = (Button) view.findViewById(R.id.options_item_delete_button);
        }

    }

    @Override
    public View newView(Context context, final Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.options_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = cursor.getInt(cursor.getColumnIndex(RecorderContract.ContactEntry._ID));
                if(clickListener != null){
                    clickListener.onClick(id);
                }
            }
        });

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();


    }

    public static interface OnDeleteButtonClickListener{
        public void onClick(int id);
    }

}
