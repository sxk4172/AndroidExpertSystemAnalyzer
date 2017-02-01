package com.example.sourabh_deshkulkarni.androidexpertsystemanalyzer;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Context context;
    public static StringBuffer recommendations = new StringBuffer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        final Button butt_calc = (Button)findViewById(R.id.scanButton);


        butt_calc.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                recommendations = new StringBuffer();
                int weightage = calculateSecurityLevelWeightage();
                String securityLevel ="";
                if(weightage >75 ){
                    securityLevel = "System has low security level with " + weightage+" % risk";
                }else if(weightage <= 75 && weightage >= 50){
                    securityLevel = "System has moderate security level with " + weightage+" % risk";
                }else if(weightage < 50 ){
                    securityLevel = "System has high security level with only " + weightage+" % risk";
                }
                Intent intent = new Intent(getApplicationContext(),ResultActivity.class);
                intent.putExtra("Message",securityLevel);
                intent.putExtra("Weightage",weightage);
                intent.putExtra("recommendations",recommendations.toString());
                startActivity(intent);


            }
        });
    }
    private boolean validateMicAvailability(){
        Boolean available = true;
        AudioRecord recorder =
                new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT, 44100);
        try{
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED ){
                available = true;

            }
        } finally{
            recorder.release();
            recorder = null;
        }

        return false;




    }
    public boolean isCameraUsebyApp() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) camera.release();
        }
        return false;
    }
    public static boolean isRooted() {

        // get from build info
        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }

        // check if /system/app/Superuser.apk is present
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e1) {
            // ignore
        }

        // try executing commands
        return canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
    }

    // executes a command on the system
    private static boolean canExecuteCommand(String command) {
        boolean executedSuccesfully;
        try {
            Runtime.getRuntime().exec(command);
            executedSuccesfully = true;
        } catch (Exception e) {
            executedSuccesfully = false;
        }

        return executedSuccesfully;
    }
    public boolean checkBlueToothOpen(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.d("bluetooth", "device does not support bluetooth");
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                return true;
            }
        }
        return false;
    }
    public boolean checkSystemVersion(){
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if( !(currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) ) {
            return true;
        }
        return false;
    }
    public boolean checkMockLocation(){
        if (!(Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")) {
            return true;
        }
        //Log.d("location" , ""+risk);
        return false;
    }
    public boolean checkAccessibility(){
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            if (accessibilityEnabled==1) {
                return true;
            }
            Log.d("ACCESSIBILITY", "" + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d("error", "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        return false;
    }
    public boolean checkDeviceProvision(){
        int deviceProvisioned = 0;
        try {
            deviceProvisioned = Settings.Secure.getInt(this.getContentResolver(), android.provider.Settings.Global.DEVICE_PROVISIONED);
            if (!(deviceProvisioned==1)) {
                return true;
            }
            //  Log.d("deviceProvisioned", "" + risk);
        } catch (Settings.SettingNotFoundException e) {
            Log.d("error", "Error finding setting, device provisioning to not found: " + e.getMessage());
        }
        return false;
    }
    public int calculateSecurityLevelWeightage(){
        int weightage =0;
        Camera camera = null;
        int adb = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
        int lockPatternVisible = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCK_PATTERN_VISIBLE,2);
        int sysPropertyVersion= Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.SYS_PROP_SETTING_VERSION, 2);
        int passwordShown = Settings.Secure.getInt(context.getContentResolver(), Settings.System.TEXT_SHOW_PASSWORD, 0);
        int checkNoMarketApp = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 2);
        int devSettingsEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 2);
        int numWifiConn = Settings.Secure.getInt(context.getContentResolver(), Settings.Global.WIFI_NUM_OPEN_NETWORKS_KEPT, 2);
        int passSpeechEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD, 0);
        if(adb ==1){
            weightage = 4;
            recommendations.append("* Keep USB debugging off.\n");//done
        }
        if(lockPatternVisible==1){
            weightage = weightage + 6;
            recommendations.append("* Lock Pattern is visible, turn it off. \n");//done
        }
        if(sysPropertyVersion!=1){
            weightage = weightage + 5;
            recommendations.append("* Update Systems security version. \n");//done
        }

        if(passwordShown==1){
            weightage = weightage + 6;
            recommendations.append("* Keep Password show text off.\n");
        }
        if(checkNoMarketApp==1){
            weightage = weightage + 8;
            recommendations.append("* Avoid downloading Non-Market apps.\n");//done
        }
        if(devSettingsEnabled==1){
            weightage = weightage + 5;
            recommendations.append("* Developer settings are enabled, they can pose security threat.\n");//done
        }
        if(numWifiConn >=1 ){
            weightage = weightage + 5;
            recommendations.append("* Avoid open wifi connections.\n");//done
        }
        if(passSpeechEnabled ==1 ){
            weightage = weightage + 5;
            recommendations.append("* Keep Password speech translation off.\n");//done
        }
        if(isRooted()){
            weightage = weightage + 10;
            recommendations.append("* Avoid rooting the device for better protection.\n");//done
        }if(isCameraUsebyApp()){
            weightage = weightage + 7;
            recommendations.append("* Camera is being used by some app in background.\n");//done
        }
        if(validateMicAvailability()){
            weightage = weightage + 5;
            recommendations.append("* Mic is being used in the background, please check. \n");
        }
        if(checkBlueToothOpen()){
            weightage = weightage + 5;
            recommendations.append("* Bluetooth is on, turn it off if not using.\n");//Done
        }
        if(checkSystemVersion()){
            weightage = weightage + 11;
            recommendations.append("* Please update System OS to get latest security patches.\n");//done
        }
        if(checkAccessibility()){
            weightage = weightage + 5;
            recommendations.append("* Please turn off accessibility for better security. \n");//done
        }
        if (checkDeviceProvision()) {
            weightage = weightage + 5;
            recommendations.append("* Device is not provisioned. \n");//done
        }
        if(checkMockLocation()){
            weightage = weightage + 5;
            recommendations.append("* Real location has been sent to the developer apps. \n");//done
        }

        return weightage;

    }



}
