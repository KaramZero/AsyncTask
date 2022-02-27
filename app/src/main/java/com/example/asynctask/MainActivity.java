package com.example.asynctask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView titleTXTView;
    TextView yearTXTView;
    RatingBar ratingBar;
    TextView genreTXTView;

    boolean ready = false;

    ArrayList<HashMap<String, String>> moviesList;

    int counter = 0;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        titleTXTView = findViewById(R.id.Title);
        yearTXTView = findViewById(R.id.releaseYear);
        genreTXTView = findViewById(R.id.genre);
        ratingBar = findViewById(R.id.ratingBar);
        ratingBar.setNumStars(10);

        moviesList = new ArrayList<>();

        MyAsync myAsync = new MyAsync();
        myAsync.execute("https://miro.medium.com/max/6018/1*3rewUBdM1VKZrBGd7UDoFA.png");

        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {

                counter = 0;
                HashMap<String, String> movie = moviesList.get(0);
                titleTXTView.setText(movie.get("title"));
                yearTXTView.setText(movie.get("releaseYear"));
                ratingBar.setRating(Float.parseFloat(movie.get("rating")));
                genreTXTView.setText(movie.get("genre"));

                setImageView(movie.get("image"));

                counter++;


            }
        };


    }

    public void next(View view) {
        if (ready) {
            if (counter < moviesList.size() - 1) {
                counter++;
                HashMap<String, String> movie = moviesList.get(counter);
                titleTXTView.setText(movie.get("title"));
                yearTXTView.setText(movie.get("releaseYear"));
                genreTXTView.setText(movie.get("genre"));
                ratingBar.setRating(Float.parseFloat(movie.get("rating")));
                setImageView(movie.get("image"));
            }
        }


    }

    public void prev(View view) {
        if (ready) {
            if (counter > 0) {
                counter--;
                HashMap<String, String> movie = moviesList.get(counter);
                titleTXTView.setText(movie.get("title"));
                yearTXTView.setText(movie.get("releaseYear"));
                ratingBar.setRating(Float.parseFloat(movie.get("rating")));
                genreTXTView.setText(movie.get("genre"));
                setImageView(movie.get("image"));
            }
        }

    }

    void setImageView(String url) {
        MyAsync myAsync = new MyAsync();
        myAsync.execute(url);

    }

    public void getData(View view) {

        if (ready) {
            new Thread() {
                @Override
                public void run() {
                    String response = null;
                    try {
                        URL url = new URL("https://api.androidhive.info/json/movies.json");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        InputStream in = new BufferedInputStream(conn.getInputStream());
                        response = convertStreamToString(in);

                    } catch (MalformedURLException e) {
                        Log.e("TAG", "MalformedURLException: " + e.getMessage());
                    } catch (IOException e) {
                        Log.e("TAG", "IOException: " + e.getMessage());
                    } catch (Exception e) {
                        Log.e("TAG", "Exception: " + e.getMessage());
                    }


                    JSONObject jsonObj = null;
                    try {

                        JSONArray movies = new JSONArray(response);
                        for (int i = 0; i < movies.length(); i++) {
                            JSONObject c = movies.getJSONObject(i);
                            String title = c.getString("title");
                            String image = c.getString("image");
                            String rating = c.getString("rating");
                            String releaseYear = c.getString("releaseYear");

                            HashMap<String, String> movie = new HashMap<>();

                            JSONArray arrJson = c.getJSONArray("genre");
                            StringBuilder arr = new StringBuilder("");
                            for (int j = 0; j < arrJson.length(); j++)
                                arr.append(" , " + arrJson.getString(j));


                            movie.put("title", title);
                            movie.put("image", image);
                            movie.put("rating", rating);
                            movie.put("releaseYear", releaseYear);
                            movie.put("genre", arr.toString());

                            moviesList.add(movie);


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.i("TAG", "run: List Downloaded ");

                    handler.sendEmptyMessage(0);

                }
            }.start();
        }else     Toast.makeText(MainActivity.this, "Not ready yet", Toast.LENGTH_SHORT).show();



    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }


    Bitmap download(String url) {

        Bitmap res = null;
        URL urlobj;
        HttpsURLConnection connection;
        InputStream stream;

        try {
            urlobj = new URL(url);
            connection = (HttpsURLConnection) urlobj.openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                res = BitmapFactory.decodeStream(stream);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res != null)
            Log.i("TAG", "doInBackground:  downloaded");
        return res;
    }

    class MyAsync extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap res = null;
            res = download(strings[0]);

            return res;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            ready = true;
            if (bitmap != null)
                Toast.makeText(MainActivity.this, "Downloaded", Toast.LENGTH_SHORT).show();
        }
    }
}
