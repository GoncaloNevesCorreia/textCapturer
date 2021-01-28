package com.joythis.android.pdm2textcapturer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<SharedText> implements View.OnClickListener {
    private ArrayList<SharedText> dataSet;

    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtText;
        TextView txtDate;
    }

    public CustomAdapter(ArrayList<SharedText> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }


    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        SharedText dataModel=(SharedText)object;

        switch (v.getId()) {


            case R.id.idBtnSpeakText:
                Toast.makeText(mContext, "Teste 123", Toast.LENGTH_LONG).show();

                //Toast.makeText(mContext, dataModel.getmText(), Toast.LENGTH_LONG).show();
                break;
        }
    }


    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        SharedText dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtText = (TextView) convertView.findViewById(R.id.idTvText);
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.idTvDate);


            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.txtText.setText(dataModel.getmText());
        viewHolder.txtDate.setText(dataModel.getmDate());
        // Return the completed view to render on screen
        return convertView;
    }
}

