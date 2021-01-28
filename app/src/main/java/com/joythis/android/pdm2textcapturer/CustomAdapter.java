package com.joythis.android.pdm2textcapturer;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

public class CustomAdapter extends ArrayAdapter<SharedText> {
    private MainActivity mainActivity;
    private TextToSpeech mTTS;
    Context mContext; //Context of client Activity(ies)
    int mLayout; //layout resource to which we must adapt
    ArrayList<SharedText> mAlText; //the data to adapt
    TextDB mdb;


    // View lookup cache
    private static class ViewHolder {
        TextView txtText;
        TextView txtDate;
        Button btnSpeakText;
        Button btnRemoveItem;
    }

    public CustomAdapter(Context context, ArrayList<SharedText> data, TextDB db, MainActivity activity) {
        super(context, R.layout.row_item, data);
        this.mAlText = data;
        this.mContext=context;
        this.mLayout = R.layout.row_item;
        this.mdb = db;
        this.mainActivity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater linf = (LayoutInflater)
                (LayoutInflater)
                        mContext.getSystemService
                                (mContext.LAYOUT_INFLATER_SERVICE);

        if (linf!=null){
            convertView =
                    linf.inflate(mLayout, parent,false);

            ViewHolder viewHolder = new ViewHolder();

            SharedText c = mAlText.get(position);
            int id = c.getmId();
            String strText = c.getmText();
            String strDate = c.getmDate();
            viewHolder.txtText = convertView.findViewById(R.id.idTvText);
            viewHolder.txtDate = convertView.findViewById(R.id.idTvDate);
            viewHolder.btnSpeakText = convertView.findViewById(R.id.idBtnSpeakText);
            viewHolder.btnRemoveItem = convertView.findViewById(R.id.idBtnRemoveItem);

            viewHolder.txtText.setText(strText);
            viewHolder.txtDate.setText(strDate);

            mTTS = new TextToSpeech(mContext, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        viewHolder.btnSpeakText.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            });

            viewHolder.btnSpeakText.setOnClickListener(v -> {
                speak(strText);
            });


            viewHolder.btnRemoveItem.setOnClickListener(v -> {
                mdb.remove(id);
                mainActivity.syncLvTextWithDB();

                Toast.makeText(mContext, String.valueOf(id), Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }

        return super.getView(position, convertView, parent);
    }

    private void speak(String strText) {
        final int pitch = 1;
        final int speed = 1;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(strText, TextToSpeech.QUEUE_FLUSH, null);
    }
}

