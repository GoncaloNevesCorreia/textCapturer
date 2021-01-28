package com.joythis.android.pdm2textcapturer;

public class SharedText {
    private int mid;
    private String mText;
    private String mDate;


    public String getmText() {
        return mText;
    }

    public String getmDate() {
        return mDate;
    }

    public int getmId() { return mid; }


    public SharedText (int id, String pText, String pDate){
        this.mid = id;
        this.mText = pText;
        this.mDate = pDate;
    }//SharedText
}
