package com.pgizka.simplecallrecorder.recordings;


import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;


public class RecordingDetailFragment extends Fragment {
    static final String TAG = RecordingDetailFragment.class.getSimpleName();
    static final String URI_ARG = "uriArg";

    ImageView mainImage, callImage, playPauseImage;
    TextView dateText, durationText, sizeText, playDurationText, playPositionText;
    Button deleteButton, shareButton, callButton, messageButton;
    SeekBar seekBar;

    Uri uri;
    Cursor cursor;
    String audioFilePath;

    Handler handler = new Handler();
    MediaPlayer mediaPlayer;
    boolean isPlaybackOn = false;


    public RecordingDetailFragment() {
        // Required empty public constructor
    }

    public static RecordingDetailFragment newInstance(Uri uri){
        RecordingDetailFragment fragment = new RecordingDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(URI_ARG, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recording_detail, container, false);

        mainImage = (ImageView) view.findViewById(R.id.recording_detail_main_image);
        callImage = (ImageView) view.findViewById(R.id.recording_detail_call_image);
        playPauseImage = (ImageView) view.findViewById(R.id.recording_detail_play_image);
        dateText = (TextView) view.findViewById(R.id.recording_detail_date_text);
        durationText = (TextView) view.findViewById(R.id.recording_detail_duration_text);
        sizeText = (TextView) view.findViewById(R.id.recording_detail_size_text);
        playDurationText = (TextView) view.findViewById(R.id.recording_detail_play_duration_text);
        playPositionText = (TextView) view.findViewById(R.id.recording_detail_play_position_text);
        deleteButton= (Button) view.findViewById(R.id.recording_detail_delete_button);
        shareButton = (Button) view.findViewById(R.id.recording_detail_share_button);
        callButton = (Button) view.findViewById(R.id.recording_detail_call_button);
        messageButton = (Button) view.findViewById(R.id.recording_detail_message_button);
        seekBar = (SeekBar) view.findViewById(R.id.recording_detail_seek_bar);

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteButton();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShareButton();
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallButton();
            }
        });

        playPauseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayPauseImage();
            }
        });

        Bundle bundle = getArguments();
        if(bundle != null){
            uri = bundle.getParcelable(URI_ARG);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        setUpScreen();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpScreen(){
        cursor.moveToFirst();

        String phoneNumber = cursor.getString(cursor.getColumnIndex(
                RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));
        audioFilePath = cursor.getString(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_PATH));
        int duration = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_LENGTH));
        int type = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_TYPE));
        long date = cursor.getLong(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_DATE));
        String displayName = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
        String contactId = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_CONTACT_ID));

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if(displayName != null && !TextUtils.isEmpty(displayName)) {
            actionBar.setTitle(displayName);
        } else {
            actionBar.setTitle(phoneNumber);
        }


        if(contactId != null && !TextUtils.isEmpty(contactId)) {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getActivity().getContentResolver(), uri);
            if(input != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                mainImage.setImageBitmap(bitmap);
            }
        }

        if(type == RecorderContract.RecordEntry.TYPE_INCOMING){
            callImage.setImageResource(R.drawable.call_arrow_incoming);
        } else {
            callImage.setImageResource(R.drawable.call_arrow_outgoing);
        }

        String dateFormated = new SimpleDateFormat("HH:mm dd MMM").format(date);
        dateText.setText(dateFormated);

        String durationFormated = Utils.formatTime(duration);
        durationText.setText(durationFormated);
        playDurationText.setText(durationFormated);

        File file = new File(audioFilePath);
        int size = (int) (file.length() / 1024);
        sizeText.setText(size + "KB");

    }

    private void onPlayPauseImage(){
        if(isPlaybackOn){
            mediaPlayer.pause();
            playPauseImage.setImageResource(R.drawable.ic_action_play);
            isPlaybackOn = false;
        } else {
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            playPauseImage.setImageResource(R.drawable.ic_action_pause);
            playerUpdate();
            isPlaybackOn = true;
        }


    }

    Runnable playerRun = new Runnable() {
        @Override
        public void run() {
            playerUpdate();
        }
    };

    private void playerUpdate(){
        int progress = mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration();
        Log.d(TAG, "progress is " + progress);
        seekBar.setProgress(progress);
        playPositionText.setText(Utils.formatTime(mediaPlayer.getCurrentPosition()/1000));
        if(progress < 99) {
            handler.postDelayed(playerRun, 1000);
        }
    }

    private void onDeleteButton(){

    }

    private void onShareButton(){

    }

    private void onCallButton(){

    }

    private void onMessageButton(){

    }



}
