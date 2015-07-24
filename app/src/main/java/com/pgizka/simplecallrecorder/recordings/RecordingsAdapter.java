package com.pgizka.simplecallrecorder.recordings;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.InputStream;
import java.text.SimpleDateFormat;

/**
 * Created by Pawe� on 2015-07-11.
 */
public class RecordingsAdapter extends CursorAdapter {
    static final String TAG = RecordingsAdapter.class.getSimpleName();

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
        String displayName = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
        String contactId = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_CONTACT_ID));

        viewHolder.timeText.setText(Utils.formatTime(date));
        viewHolder.durationText.setText(Utils.formatDuration(duration));

        if(type == RecorderContract.RecordEntry.TYPE_INCOMING){
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_incoming);
        } else {
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_outgoing);
        }

        if(displayName != null){
            viewHolder.displayNameText.setText(displayName);
            viewHolder.phoneText.setVisibility(View.VISIBLE);
            viewHolder.phoneText.setText(phoneNumber);
        } else {
            viewHolder.displayNameText.setText(phoneNumber);
            viewHolder.phoneText.setVisibility(View.GONE);
        }

        if(contactId != null && !TextUtils.isEmpty(contactId)){
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
            if(input != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                viewHolder.contactImage.setImageBitmap(bitmap);
            } else {
                viewHolder.contactImage.setImageResource(R.drawable.defult_contact_image);
            }
        } else {
            viewHolder.contactImage.setImageResource(R.drawable.defult_contact_image);
        }

    }




}
