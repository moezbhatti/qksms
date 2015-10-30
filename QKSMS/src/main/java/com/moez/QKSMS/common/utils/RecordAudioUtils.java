package com.moez.QKSMS.common.utils;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordAudioUtils {

    private static final String TAG = "RecordAudioUtils";
    private File mCurrentFile;
    private MediaRecorder mRecorder;

    public void onRecord(boolean start) {
        if (start) {
            try{startRecording();}
            catch (Exception e) {Log.e(TAG, "Couldn't start recorder" + e);}
        }
        else {
            try{stopRecording();}
            catch (Exception e) {Log.e(TAG, "Couldn't stop recorder" + e);}
        }
    }

    private String getFilePath() throws IOException {
        String fileName = "QKSMS_AUDIO_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_RINGTONES); //FIXME change directory?
        mCurrentFile = File.createTempFile(
                fileName,  /* prefix */
                ".mp3",     /* suffix */
                storageDir  /* directory */
        );
        return mCurrentFile.getAbsolutePath();
    }

    public File getCurrentFile() {
        return mCurrentFile;
    }

    private void startRecording() throws Exception {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(getFilePath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.prepare();
        mRecorder.start();
    }

    private void stopRecording() throws Exception {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
    }
}
