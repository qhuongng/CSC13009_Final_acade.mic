package com.example.acade_mic;

import android.os.AsyncTask;

import com.example.acade_mic.SpeechCredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import com.google.cloud.speech.v1p1beta1.SpeechSettings;
import com.google.protobuf.ByteString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AsyncAudioTranscriptor extends AsyncTask<String, Void, String> {
    private SpeechCredentialsProvider credentialsProvider;
    private TranscriptionCallback callback;

    public AsyncAudioTranscriptor(SpeechCredentialsProvider credentialsProvider, TranscriptionCallback callback) {
        this.credentialsProvider = credentialsProvider;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        String output;
        String filePath = params[0];
        try {
            ServiceAccountCredentials credentials = (ServiceAccountCredentials) credentialsProvider.getCredentials();
            SpeechSettings speechSettings =
                    SpeechSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                            .build();

            try (SpeechClient speech = SpeechClient.create(speechSettings)) {
                Path path = Paths.get(filePath);
                byte[] data = Files.readAllBytes(path);
                ByteString audioBytes = ByteString.copyFrom(data);

                RecognitionConfig config =
                        RecognitionConfig.newBuilder()
                                .setEncoding(RecognitionConfig.AudioEncoding.MP3)
                                .setSampleRateHertz(16000)
                                .setLanguageCode("vi-VN")
                                .build();
                RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

                // Performs speech recognition on the audio file
                OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                        speech.longRunningRecognizeAsync(config, audio);

                while (!response.isDone()) {
                    System.out.println("Waiting for response...");
                    Thread.sleep(10000);
                }

                List<SpeechRecognitionResult> results = response.get().getResultsList();

                for (SpeechRecognitionResult result : results) {
                    // There can be several alternative transcripts for a given chunk of speech. Just use the
                    // first (most likely) one here.
                    SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                    output = alternative.getTranscript();
                    return output;

                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String transcript) {
        // Update UI with the transcript or handle errors
        if (transcript != null) {
            callback.onTranscriptionCompleted(transcript);
        } else {
            // Handle error case
            callback.onTranscriptionFailed();
        }
    }

    public interface TranscriptionCallback {
        void onTranscriptionCompleted(String transcript);
        void onTranscriptionFailed();
    }
}
