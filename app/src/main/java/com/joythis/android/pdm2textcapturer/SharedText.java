package com.joythis.android.pdm2textcapturer;

public class SharedText {
    private String mText;
    private String mDate;


    public String getmText() {
        return mText;
    }

    public String getmDate() {
        return mDate;
    }

    public SharedText (String pText, String pDate){
        this.mText = pText;
        this.mDate = pDate;
    }//SharedText


    @Override
    public String toString(){
        String strRet = "";

        strRet =
                String.format(
                        "%s [%s]",
                        this.mText,
                        this.mDate
                );

        return strRet;
    }//toString



}
