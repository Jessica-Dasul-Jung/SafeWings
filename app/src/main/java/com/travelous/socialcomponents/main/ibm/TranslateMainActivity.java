package com.travelous.socialcomponents.main.ibm;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.android.library.camera.CameraHelper;
import com.ibm.watson.developer_cloud.android.library.camera.GalleryHelper;
import com.ibm.watson.developer_cloud.language_translator.v3.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationResult;
import com.ibm.watson.developer_cloud.language_translator.v3.util.Language;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;
import com.travelous.socialcomponents.R;
import java.io.IOException;
import java.io.InputStream;

public class TranslateMainActivity extends AppCompatActivity {
    private final String TAG = "TranslateMainActivity";

    private EditText input;
    private Button mic;
    private Button translate;
    private Button play;
    private TextView translatedText;
    private ImageView loadedImage;

    private SpeechToText speechService;
    private TextToSpeech textService;
    private LanguageTranslator translationService;
    private String selectedTargetLanguage = Language.SPANISH;

    private StreamPlayer player = new StreamPlayer();

    private CameraHelper cameraHelper;
    private GalleryHelper galleryHelper;
    private MicrophoneHelper microphoneHelper;

    private MicrophoneInputStream capture;
    private boolean listening = false;

    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_main);

        cameraHelper = new CameraHelper(this);
        galleryHelper = new GalleryHelper(this);
        microphoneHelper = new MicrophoneHelper(this);

        speechService = initSpeechToTextService();
        textService = initTextToSpeechService();
        translationService = initLanguageTranslatorService();

        RadioGroup targetLanguage = findViewById(R.id.target_language);
        input = findViewById(R.id.input);
        mic = findViewById(R.id.mic);
        translate = findViewById(R.id.translate);
        play = findViewById(R.id.play);
        translatedText = findViewById(R.id.translated_text);
        Button gallery = findViewById(R.id.gallery_button);
        Button camera = findViewById(R.id.camera_button);
        loadedImage = findViewById(R.id.loaded_image);

        targetLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.spanish:
                        selectedTargetLanguage = Language.SPANISH;
                        break;
                    case R.id.french:
                        selectedTargetLanguage = Language.FRENCH;
                        break;
                    case R.id.italian:
                        selectedTargetLanguage = Language.ITALIAN;
                        break;
                }
            }
        });

        input.addTextChangedListener(new EmptyTextWatcher() {
            @Override
            public void onEmpty(boolean empty) {
                if (empty) {
                    translate.setEnabled(false);
                } else {
                    translate.setEnabled(true);
                }
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!listening) {
                    // Update the icon background
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mic.setBackgroundColor(Color.GREEN);
                        }
                    });
                    capture = microphoneHelper.getInputStream(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                speechService.recognizeUsingWebSocket(getRecognizeOptions(capture),
                                        new MicrophoneRecognizeDelegate());
                            } catch (Exception e) {
                                showError(e);
                            }
                        }
                    }).start();

                    listening = true;
                } else {
                    // Update the icon background
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mic.setBackgroundColor(Color.LTGRAY);
                        }
                    });
                    microphoneHelper.closeInputStream();
                    listening = false;
                }
            }
        });

        translate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new TranslationTask().execute(input.getText().toString());
            }
        });

        translatedText.addTextChangedListener(new EmptyTextWatcher() {
            @Override
            public void onEmpty(boolean empty) {
                if (empty) {
                    play.setEnabled(false);
                } else {
                    play.setEnabled(true);
                }
            }
        });

        play.setEnabled(false);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SynthesisTask().execute(translatedText.getText().toString());
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryHelper.dispatchGalleryIntent();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHelper.dispatchTakePictureIntent();
            }
        });
    }


    private void showTranslation(final String translation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                translatedText.setText(translation);
            }
        });
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TranslateMainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                // Update the icon background
                mic.setBackgroundColor(Color.LTGRAY);
            }
        });
    }

    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                input.setText(text);
            }
        });
    }

    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mic.setEnabled(true);
            }
        });
    }

    private SpeechToText initSpeechToTextService() {
        IamOptions options = new IamOptions.Builder()
                .apiKey(getString(R.string.speech_text_apikey))
                .build();
        SpeechToText service = new SpeechToText(options);
        service.setEndPoint(getString(R.string.speech_text_url));
        return service;
    }

    private TextToSpeech initTextToSpeechService() {
        IamOptions options = new IamOptions.Builder()
                .apiKey(getString(R.string.text_speech_apikey))
                .build();
        TextToSpeech service = new TextToSpeech(options);
        service.setEndPoint(getString(R.string.text_speech_url));
        return service;
    }

    private LanguageTranslator initLanguageTranslatorService() {
        IamOptions options = new IamOptions.Builder()
                .apiKey(getString(R.string.language_translator_apikey))
                .build();
        LanguageTranslator service = new LanguageTranslator("2018-05-01", options);
        service.setEndPoint(getString(R.string.language_translator_url));
        return service;
    }

    private RecognizeOptions getRecognizeOptions(InputStream captureStream) {
        return new RecognizeOptions.Builder()
                .audio(captureStream)
                .contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .build();
    }

    private abstract class EmptyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        // assumes text is initially empty
        private boolean isEmpty = true;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {
                isEmpty = true;
                onEmpty(true);
            } else if (isEmpty) {
                isEmpty = false;
                onEmpty(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}

        public abstract void onEmpty(boolean empty);
    }

    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {
        @Override
        public void onTranscription(SpeechRecognitionResults speechResults) {
            System.out.println(speechResults);
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                showMicText(text);
            }
        }

        @Override
        public void onError(Exception e) {
            try {
                // This is critical to avoid hangs
                // (see https://github.com/watson-developer-cloud/android-sdk/issues/59)
                capture.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            showError(e);
            enableMicButton();
        }

        @Override
        public void onDisconnected() {
            enableMicButton();
        }
    }

    private class TranslationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            TranslateOptions translateOptions = new TranslateOptions.Builder()
                    .addText(params[0])
                    .source(Language.ENGLISH)
                    .target(selectedTargetLanguage)
                    .build();
            TranslationResult result = translationService.translate(translateOptions).execute();
            String firstTranslation = result.getTranslations().get(0).getTranslationOutput();
            showTranslation(firstTranslation);
            return "Did translate";
        }
    }

    private class SynthesisTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
                    .text(params[0])
                    .voice(SynthesizeOptions.Voice.EN_US_LISAVOICE)
                    .accept(SynthesizeOptions.Accept.AUDIO_WAV)
                    .build();
            player.playStream(textService.synthesize(synthesizeOptions).execute());
            return "Did synthesize";
        }
    }

    /**
     * On request permissions result.
     *
     * @param requestCode the request code
     * @param permissions the permissions
     * @param grantResults the grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case CameraHelper.REQUEST_PERMISSION: {
                // permission granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraHelper.dispatchTakePictureIntent();
                }
            }
            case MicrophoneHelper.REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * On activity result.
     *
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            loadedImage.setImageBitmap(cameraHelper.getBitmap(resultCode));
        }

        if (requestCode == GalleryHelper.PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            loadedImage.setImageBitmap(galleryHelper.getBitmap(resultCode, data));
        }
    }
}
