package com.pgizka.simplecallrecorder;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.com.pgizka.simplecallrecorder.data.RecorderProvider;

import java.text.SimpleDateFormat;

/**
 * Created by Pawe³ on 2015-07-11.
 */
public class RecordingsAdapter extends CursorAdapter {


    public RecordingsAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public static class ViewHolder{
        ImageView contactImage;
        ImageView callImage;
        TextView displayNameText;
        TextView phoneText;
        TextView timeText;
        TextView durationText;

        public ViewHolder(View view){
            contactImage = (ImageView) view.findViewById(R.id.recording_item_contact_image);
            callImage = (ImageView) view.findViewById(R.id.recording_item_call_image);
            displayNameText = (TextView) view.findViewById(R.id.recording_item_display_name_text);
            phoneText = (TextView) view.findViewById(R.id.recording_item_phone_text);
            timeText = (TextView) view.findViewById(R.id.recording_item_time_text);
            durationText = (TextView) view.findViewById(R.id.recording_item_duration_text);
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.recordings_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String phoneNumber = cursor.getString(cursor.getColumnIndex(
                RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));
        int duration = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_LENGTH));
        int type = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_TYPE));
        String path = cursor.getString(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_PATH));
        long date = cursor.getLong(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_DATE));

        viewHolder.phoneText.setText(phoneNumber);
        viewHolder.timeText.setText(getTime(date));

        if(type == RecorderContract.RecordEntry.TYPE_INCOMING){
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_incoming);
        } else {
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_outgoing);
        }

        int minute = duration / 60;
        int second = duration % 60;

        viewHolder.durationText.setText(minute + ":" + second);

    }

    private String getDate(long time){
        SimpleDateFormat format = new SimpleDateFormat("EEEEE dd MMMMM");
        return format.format(time);
    }

    private String getTime(long time){
        SimpleDateFormat format = new SimpleDateFormat("kk.mm");
        return format.format(time);
    }


}
