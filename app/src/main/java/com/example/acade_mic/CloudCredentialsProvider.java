package com.example.acade_mic;

import android.content.Context;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.IOException;
import java.io.InputStream;

public class CloudCredentialsProvider implements CredentialsProvider {
    private Context context;

    public CloudCredentialsProvider(Context context) {
        this.context = context;
    }

    @Override
    public Credentials getCredentials() throws IOException {
        try {
            InputStream fileStream = context.getResources().openRawResource(R.raw.credential);
            return ServiceAccountCredentials.fromStream(fileStream).createScoped("https://www.googleapis.com/auth/cloud-platform", "https://www.googleapis.com/auth/cloud-platform.read-only");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}