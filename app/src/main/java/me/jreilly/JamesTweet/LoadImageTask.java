package me.jreilly.JamesTweet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jreilly on 1/10/15.
 */
public class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageView mImageView;

    public LoadImageTask(String url, ImageView imageView) {
        this.url = url;
        this.mImageView = imageView;

    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;

        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(Bitmap result){
        super.onPostExecute(result);
        mImageView.setImageBitmap(result);
    }
}
