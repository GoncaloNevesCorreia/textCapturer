package com.joythis.android.pdm2textcapturer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Artur Marques on 2018.
 */

public class AmIoHttp {

    public static String io_https_ReadAll(
            String pUrl
    ){
        String ret="";
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(pUrl);

            urlConnection = (HttpsURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);

            String strLine = "";
            while ((strLine = br.readLine())!=null){
                ret+=strLine;
            }

            br.close();
            isr.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally //The finally block always executes when the try block exits
        {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }//if
        }//finally
        return ret;
    }//io_https_ReadAll

    public static String io_http_ReadAll(
        String pUrl
    ){
        String ret="";
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(pUrl);

            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);

            String strLine = "";
            while ((strLine = br.readLine())!=null){
                ret+=strLine;
            }

            br.close();
            isr.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally //The finally block always executes when the try block exits
        {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }//if
        }//finally
        return ret;
    }//io_http_ReadAll


    public final static String TAG = "@AmIoHttp";
    public static Bitmap readBitmapFromUrl(
        String pUrl
    ){
        Bitmap ret = null;

        try{
            URL url = new URL(pUrl);
            InputStream is = url.openStream();
            ret = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch (Exception e){
            Log.e(
                TAG,
                e.getMessage()
            );
        }

        return ret;
    }//readBitmapFromUrl
}//AmIoHttp
