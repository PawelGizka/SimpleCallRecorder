package com.pgizka.simplecallrecorder.util;

/**
 * Created by Paweł on 2015-07-17.
 */
public class Utils {

    /**Returns formated duration which goes like 0:03:23
     *
     * @param duration number of seconds
     * @return formated String
     */
    public static String formatTime(int duration){
        int hours = 0;
        int minutes = duration/60;
        int seconds = duration%60;
        if(minutes >= 60){
            hours = minutes/60;
            minutes %= 60;
        }
        String formatedMinutes;
        if(minutes < 10){
            formatedMinutes = "0" + minutes;
        } else {
            formatedMinutes = Integer.toString(minutes);
        }
        String formatedSeconds;
        if(seconds < 10){
            formatedSeconds = "0" + seconds;
        } else {
            formatedSeconds = Integer.toString(seconds);
        }

        String durationFormated = hours + ":" + formatedMinutes + ":" + formatedSeconds;

        return durationFormated;
    }


}
