package com.example.acade_mic;

import android.content.Context;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.IOException;
import java.io.InputStream;

public class SpeechCredentialsProvider implements CredentialsProvider {

    private Context context;

    public SpeechCredentialsProvider(Context context) {
        this.context = context;
    }

    @Override
    public Credentials getCredentials() throws IOException {
        try {
            InputStream fileStream = context.getResources().openRawResource(R.raw.credential);
            return ServiceAccountCredentials.fromStream(fileStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}