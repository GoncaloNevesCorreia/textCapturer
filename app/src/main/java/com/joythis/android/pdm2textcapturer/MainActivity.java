package com.joythis.android.pdm2textcapturer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    Bitmap mResultBitmap;

    class MyAsyncTaskToDownloadSomeImageAndDisplayItInImageView
    extends AsyncTask <
        String, //datatype of the input required (URL)
        Void, //no progress control
        Bitmap //datatype of what is the result of the async operation (Bitmap)
    >
    {
        /*
        code that can NOT run in the main thread
        the parameter should be the URL to download
         */
        @Override
        protected Bitmap doInBackground(String... strings) {
            String strUrl = strings[0];
            Bitmap bitmapForTheImage;
            bitmapForTheImage = AmIoHttp.readBitmapFromUrl(strUrl);
            return bitmapForTheImage;

            /*
            try {
                 bitmapForTheImage = AmIoHttp.readBitmapFromUrl(strUrl);
            }//try
            catch (Exception e){
                //disaster!
                return null;
            }//catch
            return bitmapForTheImage;

             */
        }//doInBackground

        //auto-called when doInBackground ends
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap!=null){
                mResultBitmap = bitmap; //example of how to hold the result, past onPostExecute
                mIvCapturedImage.setImageBitmap(bitmap);
            }//if

            super.onPostExecute(bitmap);
        }//onPostExecute
    }//MyAsyncTaskToDownloadSomeImageAndDisplayItInImageView

    class MyAsyncTaskThatPostsToAWebServiceTheWhenAndTheWhatThatWasShared
    extends AsyncTask <String /*inputs*/, Void /*progress*/, String /*return of doInBackground*/> {

        @Override
        protected String doInBackground(String... strings) {
            String strWhen = strings[0];
            String strWhat = strings[1];
            /*String strServerResponse =
                postShare(
                    strWhen,
                    strWhat
                );
            return strServerResponse; */
            return "";
        }//doInBackground
    }//MyAsyncTask



    Context mContext;

    @BindView(R.id.idEtText)
        EditText mEtText;

    @BindView(R.id.idTvCapturedText)
        TextView mTvCapturedText;

    @BindView(R.id.idIvCapturedImage)
        ImageView mIvCapturedImage;


    ArrayList<SharedText> mAlTextCaptures;
    ArrayAdapter<SharedText> mAd;
    ListView mLvTextCaptures;
    TextDB mTextDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capturer_rl);

        init(savedInstanceState);
    }//onCreate

    void init(Bundle pBundle){
        mContext = this;
        ButterKnife.bind(this);

        mLvTextCaptures = findViewById(R.id.idLvTextCaptures);
        mAlTextCaptures = new ArrayList<>();
        mTextDB = new TextDB(mContext);
        mAd = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mAlTextCaptures);
        mLvTextCaptures.setAdapter(mAd);

        comportamentoDaListview();


        checkIfCalledByAnotherAppAndReceiveItsSharedData();

        syncLvTextWithDB();
    }//init

    void comportamentoDaListview() {

        mAlTextCaptures = new ArrayList<>();


        mAd = new CustomAdapter(mAlTextCaptures, getApplicationContext());

        mLvTextCaptures.setAdapter(mAd);
        mLvTextCaptures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SharedText dataModel= mAlTextCaptures.get(position);



            }
        });
    }

    void checkIfCalledByAnotherAppAndReceiveItsSharedData(){
        Intent intentHowWasICalled = getIntent();

        if (intentHowWasICalled!=null){
            String strAction = intentHowWasICalled.getAction();

            boolean bItWasTheUserInterfaceOrAndroidStudio =
                strAction.equals(Intent.ACTION_MAIN);

            boolean bIsItActionSend =
                strAction.equals(Intent.ACTION_SEND); //share!

            //is this a share situation?
            if (bIsItActionSend){
                String strType = intentHowWasICalled.getType(); //text/html text/plain

                boolean bIsItSharedText =
                    strType.startsWith("text/");

                boolean bIsItSharedImage =
                    strType.startsWith("image/");

                if (bIsItSharedText) {
                    //receive the shared text
                    String strSharedText =
                        intentHowWasICalled.getStringExtra(
                            Intent.EXTRA_TEXT
                        );

                    //display the received text in mTvCapturedText
                    mTvCapturedText.setText(strSharedText);

                    mTextDB.insertText(strSharedText);
                }//if

                if (bIsItSharedImage){
                    //content://djsaljdalskdj
                    //proof of concept : image interception from 3rd party app
                    Uri uriForTheImage =
                        intentHowWasICalled.getParcelableExtra(
                            Intent.EXTRA_STREAM
                        );
                    mIvCapturedImage.setImageURI(uriForTheImage);
                }
            }//if
        }//if
    }//checkIfCalledByAnotherAppAndReceiveItsSharedData


    void syncLvTextWithDB() {
        ArrayList<SharedText> alTemp = mTextDB.selectAll();
        if (alTemp!=null && alTemp.size()>0){
            mAlTextCaptures.clear();
            for (SharedText c : alTemp){
                mAlTextCaptures.add(c);
            }//for
            mAd.notifyDataSetChanged();
        }//if
    }



    //attaches a menu resource (XML) to a runtime Activity
    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        MenuInflater minf = getMenuInflater();

        if (minf!=null){
            minf.inflate(
                R.menu.my_menu, //XML
                pMenu //runtime Java structure which represents the app's menu
            );
        }//if

        return super.onCreateOptionsMenu(pMenu);
    }//onCreateOptionsMenu

    //set the behavior for each option
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem pItem) {
        switch(pItem.getItemId()){
            case R.id.idMenuItemLoadImageFromUrl:
                String strTestUrl =
                    "https://arturmarques.com/edu/pdm2/pdm2.png";

                //will not work!
                /*
                loadImageFromUrlAndDisplayItInImageView(
                    strTestUrl,
                    mIvCapturedImage
                );
                 */
                MyAsyncTaskToDownloadSomeImageAndDisplayItInImageView t;
                t = new MyAsyncTaskToDownloadSomeImageAndDisplayItInImageView();
                t.execute(strTestUrl);
                break;
        }//switch
        return super.onOptionsItemSelected(pItem);
    }//onOptionsItemSelected



    int especial;
    //save stuff here!
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (outState!=null){
            outState.putInt("KEY_SPECIAL", especial);
        }
        super.onSaveInstanceState(outState);
    }

    //recover stuff here
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState!=null){
            boolean bHasKey =
                savedInstanceState.containsKey("KEY_SPECIAL");

            if (bHasKey){
                especial = savedInstanceState.getInt("KEY_SPECIAL");
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    //will surely be called!
    //opportunity for saving
    @Override
    protected void onPause() {
        super.onPause();
    }

    @OnClick(R.id.idBtnSave)
    public void saveText() {
        String data = mEtText.getText().toString();
        mTextDB.insertText(data);
        mEtText.setText("");
        syncLvTextWithDB();
    }

    @OnClick(R.id.idBtnSpeach)
    public void btnSpeech(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi Speak something!");

        try {
            startActivityForResult(intent, 1);}
        catch (ActivityNotFoundException e) {
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK && null!=data){
                    ArrayList<String>result =
                            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mEtText.setText(result.get(0));

                }
        }
    }


}//MainActivity