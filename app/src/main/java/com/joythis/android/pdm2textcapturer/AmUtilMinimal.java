package com.joythis.android.pdm2textcapturer;

import android.app.Activity;

import java.io.File;

public class AmUtilMinimal {
    Activity mA;

    public AmUtilMinimal(
        Activity pA
    ){
        mA = pA;
    }

    public String whereIsThePrivateInternalStorage(){
        File pis = mA.getFilesDir();
        return pis.getAbsolutePath();
    }//whereIsThePrivateInternalStorage
}
