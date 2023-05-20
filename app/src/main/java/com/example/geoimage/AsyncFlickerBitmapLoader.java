package com.example.geoimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class AsyncFlickerBitmapLoader extends AsyncTask<String, Void, Bitmap> {
    private final Consumer<Bitmap> consumer;

    public AsyncFlickerBitmapLoader(Consumer<Bitmap> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(strings[0]);
            connection = (HttpURLConnection) url.openConnection();

            BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            Log.i("CIO", "received from Flickr: " + connection.getResponseCode());
            return bitmap;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        consumer.accept(bitmap);
    }
}
