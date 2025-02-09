package com.mentra.displaytext;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.augmentos.augmentoslib.AugmentOSLib;
import com.augmentos.augmentoslib.SmartGlassesAndroidService;
import com.augmentos.augmentoslib.TranscriptProcessor;

import java.util.ArrayList;

public class DisplayTextService extends SmartGlassesAndroidService {
    public static final String TAG = "DisplayTextService";

    public AugmentOSLib augmentOSLib;
    ArrayList<String> responsesBuffer;
    ArrayList<String> transcriptsBuffer;
    ArrayList<String> responsesToShare;

    private Handler transcribeLanguageCheckHandler;
    private Handler userInputCheckHandler;
    private final int maxNormalTextCharsPerTranscript = 80;
    private final int maxLines = 5;
    private final TranscriptProcessor normalTextTranscriptProcessor = new TranscriptProcessor(maxNormalTextCharsPerTranscript, maxLines);

    private String lastKnownInputText = "";

    public DisplayTextService() {
        super();
    }

    @Override
    public void setup() {
        augmentOSLib = new AugmentOSLib(this);

        transcribeLanguageCheckHandler = new Handler(Looper.getMainLooper());

        startUserInputCheckTask();

        responsesBuffer = new ArrayList<>();
        responsesToShare = new ArrayList<>();
        responsesBuffer.add("Welcome to AugmentOS.");
        transcriptsBuffer = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Called");
        augmentOSLib.deinit();

        if (userInputCheckHandler != null) {
            userInputCheckHandler.removeCallbacksAndMessages(null);
        }
        if (transcribeLanguageCheckHandler != null) {
            transcribeLanguageCheckHandler.removeCallbacksAndMessages(null);
        }

        Log.d(TAG, "ran onDestroy");
        super.onDestroy();
    }

    public void sendTextWallLiveCaption(final String newLiveCaption) {
        String caption = normalTextTranscriptProcessor.processString(newLiveCaption, true);

        augmentOSLib.sendDoubleTextWall(caption, "");
    }

    private void startUserInputCheckTask() {
        userInputCheckHandler = new Handler(Looper.getMainLooper());
        userInputCheckHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentInputText = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .getString(getString(R.string.SHARED_PREF_TRANSCRIPTION_TEXT), "");

                if (!currentInputText.equals(lastKnownInputText)) {
                    lastKnownInputText = currentInputText;
                    sendTextWallLiveCaption(lastKnownInputText);
                }
                userInputCheckHandler.postDelayed(this, 333);
            }
        }, 0);
    }
}