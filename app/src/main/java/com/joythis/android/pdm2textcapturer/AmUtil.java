package com.joythis.android.pdm2textcapturer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class AmUtil {
    public final static String E_EMPTY_STRING =
            "cannot parse an empty string";

    public final static String E_CANNOT_PARSE_INT_FROM_STRING =
            "cannot parse int from existing string";

    public final static String E_NO_EDITTEXT =
            "null EditText is not parseable";

    public final static String TAG_AM_UTIL = "@AmUtil";

    Activity mActivity;

    public AmUtil(Activity pA){
        this.mActivity = pA;
    }//AmUtil

    public void fb(
        String pStrMsg
    ){
        Toast t = Toast.makeText(
            this.mActivity,
            pStrMsg,
            Toast.LENGTH_LONG
        );
        t.show();
    }//fb


    public Map<Integer, ArrayList<String>>
        identifyPermissionsGrantedAndDenied(
            String[] paNecessaryPermissions
    )
    {
        Map<Integer, ArrayList<String>> retMap = new HashMap<>();
        ArrayList<String> alGranted = new ArrayList<>();
        ArrayList<String> alDenied = new ArrayList<>();

        for (String permission : paNecessaryPermissions){
            boolean bGranted =
                ContextCompat.checkSelfPermission(
                    mActivity,
                    permission
                ) == PackageManager.PERMISSION_GRANTED;

            boolean bDenied =
                    ContextCompat.checkSelfPermission(
                            mActivity,
                            permission
                    ) == PackageManager.PERMISSION_DENIED;

            if (bGranted) alGranted.add(permission);
            if (bDenied) alDenied.add(permission);
        }//for

        retMap.put(/* Integer 0 */ PackageManager.PERMISSION_GRANTED, alGranted);
        retMap.put(/* Integer -1 */ PackageManager.PERMISSION_DENIED, alDenied);

        return retMap;
    }//identifyPermissionsGrantedAndDenied

    public void requestNecessaryPermissionsNotYetGranted(
        String[] paNecessaryPermissions,
        int piCallBackCodeForWhenTheUserResponds
    ){
        /*
        do not request permissions already granted
         */
        Map<Integer, ArrayList<String>> map =
                this.identifyPermissionsGrantedAndDenied
                    (paNecessaryPermissions);

        ArrayList<String> alDenied = map.get(PackageManager.PERMISSION_DENIED);

        if(alDenied.size()>0){
            //convert from ArrayList<String> to String[]
            String[] aDenied = new String[alDenied.size()];
            alDenied.toArray(aDenied);
            this.mActivity.requestPermissions(
                //alDenied, //invalid syntax , because String[] is expected
                aDenied,
                piCallBackCodeForWhenTheUserResponds
            );
        }//if

    }//requestNecessaryPermissionsNotYetGranted

    /*
    receives a list of necessary permissions
    returns a String which states the status of each necessary permission
     */
    public String permissionsStatusToString(
        String[] paNecessaryPermissions
    ){
       String strRet = "";

       Map<Integer, ArrayList<String>> map =
        this.identifyPermissionsGrantedAndDenied(paNecessaryPermissions);

       ArrayList<String> alGranted =
               map.get(PackageManager.PERMISSION_GRANTED);
       ArrayList<String> alDenied =
               map.get(PackageManager.PERMISSION_DENIED);

       strRet+="GRANTED:\n";
       for(String spg : alGranted) strRet+=spg+"\n";

       strRet+="\n";
       strRet+="DENIED:\n";
       for(String spd : alDenied) strRet+=spd+"\n";

       return strRet;
    }//permissionsStatusToString

    public void populateSpinnerWithOptions(
        Spinner pSpn,
        String[] pOptions
    ){
        if (pSpn!=null && pOptions!=null && pOptions.length>0){
            ArrayAdapter<String> ad = new ArrayAdapter<>(
                this.mActivity,
                android.R.layout.simple_spinner_item,
                pOptions
            );
            ad.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            );
            pSpn.setAdapter(ad);
            ad.notifyDataSetChanged();
        }
    }

    public static String CalendarToString(
            Calendar pC
    ){
        String strRet = "";

        int year, month, day, hour, minutes, seconds;
        year = pC.get(Calendar.YEAR);
        month = pC.get(Calendar.MONTH)+1;
        day = pC.get(Calendar.DATE);
        hour = pC.get(Calendar.HOUR_OF_DAY);//CTRL^Q
        minutes = pC.get(Calendar.MINUTE);
        seconds = pC.get(Calendar.SECOND);

        String strYear, strMonth, strDay,
                strHour, strMinutes, strSeconds;

        strYear = String.valueOf(year);
        strMonth = AmUtil.addZeroIfNeededForHaving2Digits(month);
        strDay = AmUtil.addZeroIfNeededForHaving2Digits(day);
        strHour = AmUtil.addZeroIfNeededForHaving2Digits(hour);
        strMinutes = AmUtil.addZeroIfNeededForHaving2Digits(minutes);
        strSeconds = AmUtil.addZeroIfNeededForHaving2Digits(seconds);

        strRet = String.format(
                "%s-%s-%s %s:%s:%s",
                strYear,
                strMonth,
                strDay,
                strHour,
                strMinutes,
                strSeconds
        );

        return strRet;
    }//CalendarToString

    public static Calendar CalendarFromString(
            String pStrCalendar //Y-M-D hh:mm:ss
    ) throws Exception //consequence: use try{}catch{Exception e} at the caller
    {
        Calendar ret = Calendar.getInstance();
        ret.clear();

        int year, month, day, hour, minutes, seconds;

        //TODO: init year .. seconds
        String[] aCalendarParts = pStrCalendar.split(" ");
        boolean bCaution = aCalendarParts.length==2;
        if (bCaution){
            String strDate = aCalendarParts[0];
            String strTime = aCalendarParts[1];

            String[] aDateParts = strDate.split("-");
            String[] aTimeParts = strTime.split(":");
            boolean bSecondCaution = aDateParts.length==3 && aTimeParts.length==3;
            if (bSecondCaution){
                try {
                    year = Integer.parseInt(aDateParts[0]);
                    month = Integer.parseInt(aDateParts[1]);
                    day = Integer.parseInt(aDateParts[2]);

                    hour = Integer.parseInt(aTimeParts[0]);
                    minutes = Integer.parseInt(aTimeParts[1]);
                    seconds = Integer.parseInt(aTimeParts[2]);

                    ret.set(Calendar.YEAR, year);
                    ret.set(Calendar.MONTH, month-1);
                    ret.set(Calendar.DATE, day);
                    ret.set(Calendar.HOUR_OF_DAY, hour);
                    ret.set(Calendar.MINUTE, minutes);
                    ret.set(Calendar.SECOND, seconds);

                    return ret;
                }//try
                catch(Exception e){
                    //failure in extracting numbers from the date or the time
                    String strError = e.getMessage().toString();
                    Log.e(TAG_AM_UTIL, strError);
                }//catch
            }//if second caution
        }//if first caution

        String strError = "Could NOT parse Calendar string!";
        Log.e(TAG_AM_UTIL, strError);
        throw new Exception(strError);

        //return null; //never happens
    }//CalendarFromString

    public static int randomInt(
            int pMin,
            int pMax
    ){
        Random r = new Random();
        int iMax = Math.max(pMin, pMax);
        int iMin = Math.min(pMin, pMax);
        int iAmplitude = iMax-iMin+1;
        int iJump = r.nextInt(iAmplitude);
        int iDest = iMin+ iJump;
        return iDest;
    }//randomInt

    private static String
    addZeroIfNeededForHaving2Digits(
            int pSomeNumber
    )
    {
        if (pSomeNumber<10)
            return "0"+pSomeNumber;
        else
            return String.valueOf(pSomeNumber);
    }//addZeroIfNeededForHaving2Digits

    public static enum COMPARISON_TYPES {
        COMPARE_TYPE_EXACT_MATCH_CASE_SENSITIVE,
        COMPARE_TYPE_EXACT_MATCH_CASE_INSENSITIVE,
        COMPARE_TYPE_CONTAINS_CASE_SENSITIVE,
        COMPARE_TYPE_CONTAINS_CASE_INSENSITIVE,
    }
    public static int arrayStringContainsElement(
        String[] pA,
        String pE,
        COMPARISON_TYPES pComparisonType
    ){
        if (pA!=null){
            int iHowMany = pA.length;
            for (int idx=0; idx<iHowMany; idx++){
                String strCurrent = pA[idx];
                boolean bMatchCaseInsensitive = strCurrent.equalsIgnoreCase(pE);
                boolean bMatchCaseSensitive = strCurrent.equals(pE);
                boolean bMatchContainsCaseSensitive = strCurrent.indexOf(pE)!=-1;
                boolean bMatchContainsCaseInsensitive = strCurrent.toLowerCase().indexOf(pE.toLowerCase())!=-1;

                boolean bMatch = false;
                switch (pComparisonType){
                    case COMPARE_TYPE_EXACT_MATCH_CASE_INSENSITIVE:
                        bMatch = bMatchCaseInsensitive;
                        break;
                    case COMPARE_TYPE_EXACT_MATCH_CASE_SENSITIVE:
                        bMatch = bMatchCaseSensitive;
                        break;
                    case COMPARE_TYPE_CONTAINS_CASE_SENSITIVE:
                        bMatch = bMatchContainsCaseSensitive;
                        break;
                    case COMPARE_TYPE_CONTAINS_CASE_INSENSITIVE:
                        bMatch = bMatchContainsCaseInsensitive;
                        break;
                }//switch

                if (bMatch) return idx;
            }
        }
        return -1;
    }//arrayStringContainsElement

    public static Float readFloatFromEditText (EditText pEt) throws Exception {
        if (pEt!=null){
            String strEt = pEt.getText().toString().trim();
            try{
                float f = Float.parseFloat(strEt);
                return f;
            }
            catch (Exception e){
                throw (e);
            }
        }//if
        else{
            throw new Exception("No EditText");
        }//else
    }//readFloatFromEditText

    public static int readIntFromEt (
            EditText pEt
    ) throws Exception
    {
        if (pEt!=null){
            String strEt =
                    pEt.getText().toString().trim();

            Boolean bEmptyString = strEt.isEmpty();

            if (bEmptyString){
                //cannot extract int from empty string
                throw new Exception (AmUtil.E_EMPTY_STRING);
            }//if
            else{
                try {
                    int i = Integer.parseInt(strEt);
                    return i;
                }//try
                catch (Exception e){
                    throw new Exception
                            (AmUtil.E_CANNOT_PARSE_INT_FROM_STRING);
                }//catch
            }//else
        }//if we have a valid EditText
        else{
            throw new Exception
                    (AmUtil.E_NO_EDITTEXT);
        }//else
    }//readIntFromEt

    /*
        receives the name of a file, located in the "private internal storage"
        returns the entire text content of that file
         */
    public String genericPrivateInternalStorageFileReader(
            String pFileName
    ){
        String strAll = "";
        try{
            FileInputStream fis = mActivity.openFileInput(
                    pFileName
            );
            if (fis!=null){
                InputStreamReader isr = new InputStreamReader(
                        fis,
                        StandardCharsets.UTF_8
                );
                char c; int i;
                final int END_OF_FILE = -1;
                while ((i=isr.read())!=END_OF_FILE){
                    c = (char)i; //cast the byte to a char
                    strAll+=c; //concatenate the char to the already read file contents
                }//while
                isr.close();
            }//if
            fis.close();
        }//try
        catch(Exception e){
            /*
            e.g. : file does not exist
             */
            Log.e(TAG_AM_UTIL, e.getMessage().toString());
        }//catch
        return strAll;
    }//genericPrivateInternalStorageFileReader

    /*
    receives a file name and the content, to be written into the "private internal storage"
    returns true on success, false on failure
     */
    public boolean genericPrivateInternalStorageFileWriter(
            String pFileName,
            String... pContent
    ){
        try {
            FileOutputStream fos =
                    mActivity.openFileOutput(pFileName, Activity.MODE_PRIVATE);

            if (fos!=null){
                OutputStreamWriter osw = new OutputStreamWriter(
                        fos,
                        StandardCharsets.UTF_8
                );
                for (String strPartial : pContent){
                    osw.write(strPartial);
                }//for
                osw.close();
            }//if
            fos.close();

            return true;
        }//try
        catch (Exception e){
            Log.e(TAG_AM_UTIL, e.getMessage().toString());
            return false;
        }//catch
    }//genericPrivateInternalStorageFileWriter

    /*
        receives the text for an about to be created new Button
        receives the already existing LinearLayout where the dyn created Button
        is to be added
        receives an already existing click listener to be assigned to the
        about to be created new Button
     */
    void createNewButtonInLinearLayout (
            String pStrButtonText, //text for the new Button
            LinearLayout pLayoutWhereToAddTheNewButton, //LL where to add the Button (can NOT be null)
            Button.OnClickListener pButtonClickHandler //object that handles the behavior for the new Button
    )
    {
        Button btnNewNumber = new Button(mActivity);
        btnNewNumber.setText(pStrButtonText);
        LinearLayout.LayoutParams wh = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnNewNumber.setLayoutParams(wh);
        btnNewNumber.setOnClickListener(pButtonClickHandler);

        pLayoutWhereToAddTheNewButton.addView(btnNewNumber);
    }//createNewButtonInLinearLayout

    public void actionQuit(){
        Intent intentQuitToMain = new Intent(Intent.ACTION_MAIN);
        intentQuitToMain.addCategory(Intent.CATEGORY_HOME);
        intentQuitToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intentQuitToMain);
        mActivity.finish();
    }//actionQuit


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

            /*
            //this would eliminate the new line separators
            String strLine = "";
            while ((strLine = br.readLine())!=null){
                ret+=strLine;
            }
            */

            int i;
            while((i=br.read())!=-1){
                char c = (char) i;
                ret+=c;
            }//while

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

}
