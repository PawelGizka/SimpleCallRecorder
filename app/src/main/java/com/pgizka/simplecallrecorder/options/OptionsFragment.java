package com.pgizka.simplecallrecorder.options;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.util.PreferanceStrings;


public class OptionsFragment extends Fragment {

    ToggleButton toggleButton;

    SharedPreferences systemPref;

    boolean recordingEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        toggleButton = (ToggleButton) view.findViewById(R.id.options_toggle_button);

        systemPref = getActivity().getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE,
                Context.MODE_PRIVATE);
        recordingEnabled = systemPref.getBoolean(PreferanceStrings.RECORDING_ENABLED, false);

        if(recordingEnabled){
            toggleButton.setChecked(true);
        } else {
            toggleButton.setChecked(false);
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleButtonClick();
            }
        });

        return view;
    }


    public void onToggleButtonClick(){
        SharedPreferences.Editor editor = systemPref.edit();
        if(toggleButton.isChecked()){
            editor.putBoolean(PreferanceStrings.RECORDING_ENABLED, true);
            recordingEnabled = true;
        } else {
            editor.putBoolean(PreferanceStrings.RECORDING_ENABLED, false);
            recordingEnabled = false;
        }
        editor.commit();
    }

}
