package com.example.geoimage;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

// This class is meant to load images from Flickr asynchronously
public class AsyncFlickrJSONData extends AsyncTask<String, Void, JSONObject> {

    private final Consumer<JSONObject> consumer;

    public AsyncFlickrJSONData(Consumer<JSONObject> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(strings[0]);
            connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            BufferedReader bufferedReader =
                    new BufferedReader(new java.io.InputStreamReader(inputStream), 1000);
            StringBuilder stringBuilder = new StringBuilder();
            for (String line = bufferedReader.readLine();
                 line != null;
                 line = bufferedReader.readLine()) {
                stringBuilder.append(line).append("\n");
            }
            inputStream.close();

            Log.i("CIO", "received from Flickr: " + stringBuilder);
            String json =
                    stringBuilder.substring("jsonFlickrApi(".length(), stringBuilder.length() - 1);
            try { // This is to check if the JSON is valid
                return new JSONObject(json);
            } catch (JSONException e) {
                Log.e("CIO", "doInBackground: Invalid JSON " + json);
                return null;
            }
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
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        this.consumer.accept(jsonObject);
    }
}
