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
import android.widget.Button;
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
import kotlin.reflect.KFunction;

public class MainActivity extends AppCompatActivity {
    Context mContext;

    @BindView(R.id.idEtText)
        EditText mEtText;

    @BindView(R.id.idTvCapturedText)
        TextView mTvCapturedText;

    @BindView(R.id.idLvTextCaptures)
        ListView mLvTextCaptures;

    ArrayList<SharedText> mAlTextCaptures;
    ArrayAdapter<SharedText> mAd;

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

        comportamentoDaListview();

        checkIfCalledByAnotherAppAndReceiveItsSharedData();

        syncLvTextWithDB();
    }//init



    void comportamentoDaListview() {
        mTextDB = new TextDB(mContext);
        mAlTextCaptures = new ArrayList<>();
        mAd = new CustomAdapter(mContext, mAlTextCaptures, mTextDB, MainActivity.this);
        mLvTextCaptures.setAdapter(mAd);
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
                    //mIvCapturedImage.setImageURI(uriForTheImage);
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

    final static int CALL_BACK_NUMBER = 1;
    @OnClick(R.id.idBtnSpeach)
    public void btnSpeech(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi Speak something!");

        try {
            startActivityForResult(intent, CALL_BACK_NUMBER);}
        catch (ActivityNotFoundException e) {
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case CALL_BACK_NUMBER:
                if(resultCode==RESULT_OK && null!=data){
                    ArrayList<String>result =
                            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mEtText.setText(result.get(0));

                }
        }
    }


}//MainActivity