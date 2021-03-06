/*
 * Copyright (c) 2018, 2019, 2020. Peter Kopciak, Kevin Pirner, Alexis Ringot, Florian Taurer, Matthias Zeppelzauer.
 *
 * This file is part of SoniControl app.
 *
 *     SoniControl app is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     SoniControl app is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with SoniControl app.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.ac.fhstp.sonicontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
//import android.support.design.widget.Snackbar;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.DialogFragment;
//import androidx.core.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import at.ac.fhstp.sonicontrol.rest.Detection;
import at.ac.fhstp.sonicontrol.rest.RESTController;
import at.ac.fhstp.sonicontrol.rest.SoniControlAPI;
import at.ac.fhstp.sonicontrol.ui.DetectionAsyncTask;
import at.ac.fhstp.sonicontrol.ui.DetectionDialogFragment;
import at.ac.fhstp.sonicontrol.utils.Misc;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.me.berndporr.iirj.Butterworth;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static at.ac.fhstp.sonicontrol.ConfigConstants.ON_ALLOW_SNACKBAR_DURATION;
import static at.ac.fhstp.sonicontrol.ConfigConstants.WAKEUP_COUNTER_DEFAULT;
import static at.ac.fhstp.sonicontrol.utils.Recognition.computeRecognition;


public class MainActivity extends BaseActivity implements Scan.DetectionListener, DetectionDialogFragment.DetectionDialogListener, PitchShiftPlayer.PitchShiftPlayerListener {
    private static final String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET/*, Manifest.permission.WRITE_EXTERNAL_STORAGE*//*, Manifest.permission.READ_EXTERNAL_STORAGE*/};
    private static final int REQUEST_ALL_PERMISSIONS = 42;

    private static final String TAG = "MainActivity";
    static MainActivity mainIsMain;

    private ContentLoadingProgressBar statusLoadingBar;
    private TextView textViewStatus;
    Button btnStorLoc;
    Button btnStartPause;
    Button btnSettings;
    Button btnExit;

    Scan detector;
    Location locationFinder;
    private final JSONManager jsonMan = JSONManager.getInstanceJSONManager();

    DetectionDialogFragment alert;
    AsyncTask detectionAsyncTask;
    PitchShiftPlayer pitchShiftPlayer;
    Technology sigType;
    View view;

    Random randomNotificationNumberGenerator = new Random();

    private boolean isInBackground = false;

    boolean isSignalPlayerGenerated;

    MediaPlayer sigPlayer;

    boolean saveJsonFile;
    String usedBlockingMethod;
    boolean preventiveSpoof;

    protected LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    double[] lastPosition;

    boolean entryWasAskedAgain = false;

    // Thread handling
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    public static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(NUMBER_OF_CORES + 1);

    public Handler uiHandler = new Handler(Looper.getMainLooper());

    SharedPreferences sharedPref;

    AlertDialog alertLocation = null;
    private long lastClickTime = 0;
    private static final long MIN_CLICK_INTERVAL=400;

    public AlertDialog alertSharingInfo = null;
    public AlertDialog alertRuleInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /***
         * Android bug causes the launch of a new task when using the home screen or launcher icon.
         * See: https://stackoverflow.com/a/12983901/5232306
         */
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        jsonMan.init(this.getApplicationContext());

        // Used by SpectrogramView
        Misc.setAttribute("activity", this);

        setContentView(R.layout.activity_main);

        mainIsMain = this;

        //TODO: This should be ran only at first start, maybe move it to the Service or Application.
        detector = Scan.getInstance(); //Get Scan-object if no object is available yet make a new one
        detector.init(MainActivity.this); //initialize the detector with the main method
        detector.setDetectionListener(this); // MainActivity will be notified of detections (calls onDetection)

        locationFinder = Location.getInstanceLoc(); //Get LocationFinder-object if no object is available yet make a new one
        locationFinder.init(MainActivity.this); //initialize the location-object with the main method

        statusLoadingBar = findViewById(R.id.statusLoadingBar);
        textViewStatus = findViewById(R.id.textViewStatus);
        btnStartPause = findViewById(R.id.btnStartPause); //Main button for starting and pausing the scanning process
        btnStorLoc =  findViewById(R.id.btnStorLoc); //button for getting into the storedLocations activity
        btnSettings = findViewById(R.id.btnSettings); //button for getting into the settings activity
        btnExit = findViewById(R.id.btnExit); //button for exiting the application

        // Set the tinting color for the button icons on older versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            tintCompat(btnStartPause, R.drawable.ic_play_arrow_white_48dp, R.color.colorPrimary, this);
            tintCompat(btnExit, R.drawable.baseline_stop_white_48, R.color.colorPrimary, this);
            tintCompat(btnStorLoc, R.drawable.ic_view_list_white_48dp, R.color.colorPrimary, this);
            tintCompat(btnSettings, R.drawable.ic_settings_white_48dp, R.color.colorPrimary, this);
        }

        alert = new DetectionDialogFragment();

        btnStartPause.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // double-clicking prevention, using threshold MIN_CLICK_INTERVAL
                if (SystemClock.elapsedRealtime() - lastClickTime < MIN_CLICK_INTERVAL){
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();

                onBtnStartPauseClick(v);
            }
        });

        btnStorLoc.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
            openStoredLocations();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
            openSettings();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // double-clicking prevention, using threshold MIN_CLICK_INTERVAL
                if (SystemClock.elapsedRealtime() - lastClickTime < MIN_CLICK_INTERVAL){
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                onBtnExitClick(v);
            }
        });

        // savedInstanceState will be null unless there is a configuration change
        if(savedInstanceState == null) {
        }
        getUpdatedSettings(); //get the settings

        SharedPreferences sp = getSettingsObject();
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(ConfigConstants.WAKEUP_COUNTER, ConfigConstants.WAKEUP_COUNTER_DEFAULT);
        ed.apply();
    }

    private void onBtnExitClick(View v) {
        if (SoniService.IS_SERVICE_RUNNING) {
            Intent service = new Intent(MainActivity.this, SoniService.class);
            service.setAction(ServiceConstants.ACTION.STOPFOREGROUND_ACTION);
            startService(service);
        }
        else {
            NotificationHelper.cancelStatusNotification(MainActivity.this);
        }

        if (alert.getDialog() != null) {
            alert.getDialog().dismiss();
        }
        setGUIStatePaused();
        
        // Reset state
        SharedPreferences sp = getSettingsObject();
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(ConfigConstants.PREFERENCES_APP_STATE, StateEnum.STOPPED.toString());
        // Clean the technology on disk
        ed.remove(ConfigConstants.LAST_DETECTED_TECHNOLOGY_SHARED_PREF);
        // Note: this is blocking the thread, but we want to be sure that it gets persisted.
        ed.apply();
        updateStateText();

        NotificationHelper.cancelDetectionAlertStatusNotification(getApplicationContext());

        detector.stopIO(); // release audio resources from the scanner
        Spoofer spoof = Spoofer.getInstance(); //get a spoofing object
        spoof.stopSpoofingComplete(); //stop the whole spoofing process
        MicCapture micCap = MicCapture.getInstance(); //get a microphone capture object
        micCap.stopMicCapturingComplete(); //stop the whole capturing process via the microphone
        usedBlockingMethod = null;

        if (pitchShiftPlayer != null) {
            pitchShiftPlayer.cleanup();
            pitchShiftPlayer.removeDetectionListener();
            pitchShiftPlayer = null;
        }
        locationFinder.removeGPSUpdates();

        // Stop instead of Exit
        // Stop all the background threads
        //threadPool.shutdownNow();
        //System.exit(0); //exit the application
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void onBtnStartPauseClick(View v) {
        SharedPreferences sp = this.getSettingsObject();
        String stateString = sp.getString(ConfigConstants.PREFERENCES_APP_STATE, StateEnum.STOPPED.toString());
        StateEnum state;
        try {
            state = StateEnum.fromString(stateString);
        }
        catch (IllegalArgumentException e) {
            state = StateEnum.STOPPED;
        }

        switch (state) {
            case STOPPED:
            case ON_HOLD:
                startFirewall();
                break;
            case JAMMING:
            case SCANNING:
                pauseFirewall();
                break;
        }
    }

    private void startFirewall() {
        if(!hasPermissions(MainActivity.this, PERMISSIONS)){
            // If an explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {

                Toast toast = Toast.makeText(MainActivity.this, R.string.permissionRequestExplanation, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();

                uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_ALL_PERMISSIONS);
                    }
                }, 2000);
            } else {
                // First time, no explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_ALL_PERMISSIONS);
            }
        }
        else {
            if(locationFinder.validateMicAvailability()){
                startService();
                checkForActivatedLocation();
                startDetection();
                isLocationEnabled(this);
            }else{
                activateNoMicrophoneAccessAlertDialog();
            }
        }
    }

    private void startService() {
        if (!SoniService.IS_SERVICE_RUNNING) {
            Intent service = new Intent(this.getApplicationContext(), SoniService.class);
            service.setAction(ServiceConstants.ACTION.STARTFOREGROUND_ACTION);
            startService(service);
        }
    }

    private void showRequestPermissionExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.permissionRequestExplanation);
        builder.setPositiveButton("Open the Permission Menu",new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }
        );
        builder.setNegativeButton("Back to the main menu", null);
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ALL_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0) {
                    //we will show an explanation next time the user click on start
                    showRequestPermissionExplanation();
                }
                else {
                    for (int i = 0; i < permissions.length; i++) {
                        if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                if(locationFinder.validateMicAvailability()){
                                    startService();
                                    checkForActivatedLocation();
                                    startDetection();
                                }else{
                                    activateNoMicrophoneAccessAlertDialog();
                                }
                            }
                            else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                                showRequestPermissionExplanation();
                            }
                        }
                    }
                }
            }
            case ConfigConstants.REQUEST_GPS_PERMISSION:{
                if (grantResults.length == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, R.string.toastLocationAccessDenied, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    for (int i = 0; i < permissions.length; i++) {
                        if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i])) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                locationFinder.requestGPSUpdates(this);
                            }
                            else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                                runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          Toast.makeText(MainActivity.this, R.string.toastLocationAccessDenied, Toast.LENGTH_LONG).show();
                                      }
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    public void activateAlertOnAskAgain(Technology signalType){
        entryWasAskedAgain = true;
        activateAlert(signalType);
    }


    public void activateAlert(Technology signalType) {
        SharedPreferences settings = getSettingsObject(); //get the settings

        sigType = signalType; //set the technology variable to the latest detected one

        boolean activityExists = settings.getBoolean("active", false);
        if (activityExists) {
            runOnUiThread(displayAlert);
        }
    }

    private Runnable displayAlert = new Runnable() {
        public void run() {
            if (!alert.isAdded()) {
                alert.show(getSupportFragmentManager(), "DetectionDialogFragment");
            }
            detectionAsyncTask = new DetectionAsyncTask(alert).execute(getApplicationContext()/*bufferHistory*/);
        }
    };

    /**
     * Stops blocking process. Checks which method was used and stops it.
     */
    private void stopAutomaticBlockingMethodOnAction(){
        if(usedBlockingMethod.equals(ConfigConstants.USED_BLOCKING_METHOD_SPOOFER)){
            Spoofer spoofBlock = Spoofer.getInstance();
            spoofBlock.stopSpoofingComplete();
        }else if(usedBlockingMethod.equals(ConfigConstants.USED_BLOCKING_METHOD_MICROPHONE)){
            MicCapture micBlock = MicCapture.getInstance();
            micBlock.stopMicCapturingComplete();
        }
        usedBlockingMethod = null;
    }


    public SharedPreferences getSettingsObject(){
        if (sharedPref == null)
            sharedPref = PreferenceManager.getDefaultSharedPreferences(this); //get the object with the settings
        return sharedPref;
    }

    public void getUpdatedSettings(){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        setActiveRuleInfoMenuItem(false);
        setActiveSettingsInfoMenuItem(false);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume(){ // Handles most of the app state
        super.onResume();
        isInBackground = false;
        checkFirstRunForWelcomeShowing();
        checkForJSONHistoryUpdate();

        if (pitchShiftPlayer != null) {
            pitchShiftPlayer.onResume();
        }

        PendingIntent detectionPendingIntent = NotificationHelper.getPendingIntentDetectionFlagNoCreate(getApplicationContext());

        // Store the app activeness and then check the state
        SharedPreferences sp = getSettingsObject();
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", true);
        ed.apply();
        String stateString = sp.getString(ConfigConstants.PREFERENCES_APP_STATE, StateEnum.STOPPED.toString());
        StateEnum state;
        try {
            state = StateEnum.fromString(stateString);
        }
        catch (IllegalArgumentException e) {
            state = StateEnum.STOPPED;
        }

        Intent intent = getIntent();
        switch (state) {
            case STOPPED:
                setGUIStatePaused();
                break;
            case ON_HOLD:
                NotificationHelper.activateOnHoldStatusNotification(getApplicationContext());
                setGUIStatePaused();
                break;
            case JAMMING:
                setGUIStateStarted();
                NotificationHelper.activateSpoofingStatusNotification(getApplicationContext());

                if (intent.hasExtra(ConfigConstants.EXTRA_TECHNOLOGY_DETECTED)) {
                    //TODO: We might use directly the ConfigConstants.LAST_DETECTED_TECHNOLOGY_SHARED_PREF, not using Extras ?

                    Technology technology = (Technology) intent.getExtras().get(ConfigConstants.EXTRA_TECHNOLOGY_DETECTED);
                    if (technology != null) {
                        activateAlert(technology);
                        intent.removeExtra(ConfigConstants.EXTRA_TECHNOLOGY_DETECTED);
                    }
                }
                else if (detectionPendingIntent != null) {
                    if (sigType != null) {
                        activateAlert(sigType);
                    }
                    else {
                        // in case the Activity was destroyed
                        String storedTechnology = sp.getString(ConfigConstants.LAST_DETECTED_TECHNOLOGY_SHARED_PREF, null);
                        if (storedTechnology != null) {
                            Technology lastDetectedTechnology = null;
                            try {
                                lastDetectedTechnology = Technology.fromString(storedTechnology);
                            }
                            catch (IllegalArgumentException e) {
                                //Log.d(TAG, "onResume: " + e.getMessage());
                            }
                            if (lastDetectedTechnology != null) {
                                activateAlert(lastDetectedTechnology);
                            }
                            else {
                                //Log.d(TAG, "onResume: Technology not stored correctly ?");
                            }
                        }
                    }
                }

                break;
            case SCANNING:
                setGUIStateStarted();

                if (intent.hasExtra(ConfigConstants.EXTRA_TECHNOLOGY_DETECTED)) {
                    //TODO: We might use directly the ConfigConstants.LAST_DETECTED_TECHNOLOGY_SHARED_PREF, not using Extras ?

                    Technology technology = (Technology) intent.getExtras().get(ConfigConstants.EXTRA_TECHNOLOGY_DETECTED);
                    if (technology != null) {
                        activateAlert(technology);
                        intent.removeExtra(ConfigConstants.EXTRA_TECHNOLOGY_DETECTED);
                    }
                }
                else if (detectionPendingIntent != null) {
                    if (sigType != null) {
                        activateAlert(sigType);
                    }
                    else {
                        // in case the Activity was destroyed
                        String storedTechnology = sp.getString(ConfigConstants.LAST_DETECTED_TECHNOLOGY_SHARED_PREF, null);
                        if (storedTechnology != null) {
                            Technology lastDetectedTechnology = null;
                            try {
                                lastDetectedTechnology = Technology.fromString(storedTechnology);
                            }
                            catch (IllegalArgumentException e) {
                                //Log.d(TAG, "onResume: " + e.getMessage());
                            }
                            if (lastDetectedTechnology != null) {
                                activateAlert(lastDetectedTechnology);
                            }
                            else {
                                //Log.d(TAG, "onResume: Technology not stored correctly ?");
                            }
                        }
                    }
                }
                else {
                    NotificationHelper.activateScanningStatusNotification(getApplicationContext());
                }
                break;
            default:
                // Should not happen as a default value is set above
                break;
        }

        updateStateText();
        Snackbar.make(((ViewGroup) MainActivity.this
                        .findViewById(android.R.id.content)).getChildAt(0), state.toString(),
                Snackbar.LENGTH_SHORT)
                .show();

        saveJsonFile = sp.getBoolean(ConfigConstants.SETTING_SAVE_DATA_TO_JSON_FILE, ConfigConstants.SETTING_SAVE_DATA_TO_JSON_FILE_DEFAULT);

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                if(saveJsonFile) {
                    if (!jsonMan.checkIfJsonFileIsAvailable()) { //check if a JSON File is already there in the storage
                            jsonMan.createJsonFile(); //create a JSON file
                    }
                    if (!jsonMan.checkIfSavefolderIsAvailable()) { //check if a folder for the audio files is already there in the storage
                            jsonMan.createSaveFolder(); //create a folder for the audio files
                    }
                }
            }
        });
    }

    /**
     * Update the state Textview with the corresponding localized string.
     */
    /*package-private*/ void updateStateText() {
        runOnUiThread(updateStateTextRunnable);
    }

    private Runnable updateStateTextRunnable = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sp = getSettingsObject();
            String stateString = sp.getString(ConfigConstants.PREFERENCES_APP_STATE, StateEnum.STOPPED.toString());
            StateEnum state;
            try {
                state = StateEnum.fromString(stateString);
            }
            catch (IllegalArgumentException e) {
                Log.w(TAG, "updateStateText, StateEnum changed? Error: " + e.getMessage());
                state = StateEnum.STOPPED;
            }

            // Should not be visible by default, will be set to visible if initialization is ongoing
            statusLoadingBar.setVisibility(View.GONE);
            //Would it be better to retrieve both shared preferences here directly?
            switch (state) {
                case STOPPED:
                    textViewStatus.setText(getString(R.string.textviewStatusStopped));
                    break;
                case ON_HOLD:
                    textViewStatus.setText(getString(R.string.StatusNotificationOnHoldMessage));
                    break;
                case JAMMING:
                    textViewStatus.setText(getString(R.string.StatusNotificationSpoofingMesssage));
                    break;
                case SCANNING:
                    boolean initialized = sp.getBoolean(ConfigConstants.PREFERENCES_SCANNER_INITIALIZED, false);
                    if (!initialized) { // Detector initialization is ongoing
                        textViewStatus.setText(getString(R.string.textviewStatusInitializing));
                        statusLoadingBar.setVisibility(View.VISIBLE);
                    }
                    else {
                        textViewStatus.setText(getString(R.string.StatusNotificationScanningMessage));
                    }
                    break;
                default:
                    textViewStatus.setText(getString(R.string.textviewStatusStopped));
                    break;
            }
        }
    };

    @Override
    public void onPause(){ //override the onPause method for setting a variable for checking the background-status
        super.onPause();
        isInBackground = true;

        if (pitchShiftPlayer != null) {
            pitchShiftPlayer.onPause();
        }

        // Store our shared preference
        SharedPreferences sp = getSettingsObject();
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        // Clean the technology on disk
        // ed.remove(ConfigConstants.LAST_DETECTED_TECHNOLOGY_SHARED_PREF);
        ed.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (sigPlayer != null ) {
            sigPlayer.release(); //release the resources of the player
            sigPlayer = null; //set the player variable to null
            isSignalPlayerGenerated = false; //now there is no player anymore so it's false
            setAlertUIReplayStopped(); //set the button for playing/stopping to "play"
        }
        if (isSignalPlayerGenerated && pitchShiftPlayer != null) {
            pitchShiftPlayer.cleanup();
            pitchShiftPlayer.removeDetectionListener();
            pitchShiftPlayer = null;
            isSignalPlayerGenerated = false;
            setAlertUIReplayStopped();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (pitchShiftPlayer != null) {
            pitchShiftPlayer.cleanup();
            pitchShiftPlayer.removeDetectionListener();
            pitchShiftPlayer = null;
        }
        // ??? detector.removeDetectionListener(this);
        // Release resources... But this should not be called as long as our Service runs.
        // Maybe threads, microphone, ... ?
        // threadPool.shutdownNow();
    }

    public static MainActivity getMainIsMain(){
        return mainIsMain;
    }

    public boolean getBackgroundStatus(){
        return isInBackground;
    }

    public boolean[] checkJsonAndLocationPermissions() {
        boolean[] saveJsonAndLocation = new boolean[2];
        SharedPreferences settings = getSettingsObject(); //get the settings
        saveJsonFile = settings.getBoolean(ConfigConstants.SETTING_SAVE_DATA_TO_JSON_FILE, ConfigConstants.SETTING_SAVE_DATA_TO_JSON_FILE_DEFAULT);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        boolean locationTrack = false;
        boolean locationTrackGps = settings.getBoolean(ConfigConstants.SETTING_GPS, ConfigConstants.SETTING_GPS_DEFAULT);
        boolean locationTrackNet = settings.getBoolean(ConfigConstants.SETTING_NETWORK_USE, ConfigConstants.SETTING_NETWORK_USE_DEFAULT);

        if((locationTrackGps&&isGPSEnabled)||(locationTrackNet&&isNetworkEnabled)){
            locationTrack = true;
        }

        saveJsonAndLocation[0] = saveJsonFile;
        saveJsonAndLocation[1] = locationTrack;

        return saveJsonAndLocation;
    }

    @Override
    public void onDetection(final Technology detectedTechnology, final float[] bufferHistory) {//, final int maxValueIndex) {
        // Should we start this as an AsyncTask already ?
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                handleSignal(bufferHistory); //, maxValueIndex);
            }
        });
    }

    @Override
    public void onDetectorInitialized() {
        updateStateText();
    }

    private void handleSignal(float[] bufferHistory) {
        String detectionDateAndTime = getTimeAndDateForAlert();

        bufferHistory = preProcessing(bufferHistory);

        final Technology technology = computeRecognition(bufferHistory, this.getApplicationContext());

        SharedPreferences sp = getSettingsObject();
        SharedPreferences.Editor ed = sp.edit();
        // Stores the technology on disk in case the Activity was destroyed
        ed.putString(ConfigConstants.LAST_DETECTED_TECHNOLOGY_SHARED_PREF, technology.toString());
        ed.putString(ConfigConstants.LAST_DETECTED_DATE_SHARED_PREF, detectionDateAndTime);
        ed.putInt(ConfigConstants.BUFFER_HISTORY_LENGTH_SHARED_PREF, bufferHistory.length);
        ed.apply();

        preventiveSpoof = sp.getBoolean(ConfigConstants.SETTING_PREVENTIVE_SPOOFING, ConfigConstants.SETTING_PREVENTIVE_SPOOFING_DEFAULT);
        if(preventiveSpoof) {
            if (usedBlockingMethod == null) {
                NotificationHelper.activateSpoofingStatusNotification(getApplicationContext());
                usedBlockingMethod = locationFinder.blockMicOrSpoof(this);
            }
        }

        // Move to the specific cases where the bufferHistory will be needed ?
        alert.setSpectrum(null); // Reset the visualization
        // Write bufferHistory to file (we are already in a separated Thread)
        // It will be read when the alert is actually shown.
        SignalConverter.writeFloatArray(bufferHistory, this.getFilesDir() + File.separator + ConfigConstants.BUFFER_HISTORY_FILENAME);

        boolean locationTrack;
        locationFinder.saveSignalTypeForLater(technology);
        locationTrack = this.checkJsonAndLocationPermissions()[1];

        if (locationTrack) {
            lastPosition = locationFinder.getLocation(this); //get the latest position
            locationFinder.saveLocationGPSTrackerObject();
            ed.putString(ConfigConstants.LAST_DETECTED_LOCATION_SHARED_PREF, locationFinder.getDetectedDBEntryAddres());
            ed.apply();
        }

        // Checks if the user prefers to block every location
        if (this.getSettingsObject().getBoolean(ConfigConstants.SETTING_CONTINOUS_SPOOFING, false)) { //check if the settings are set to continous spoofing
            if (locationTrack) {
                locationFinder.setPositionForContinuousSpoofing(lastPosition); //set the position for distance calculation to the latest position
            }
            NotificationHelper.activateSpoofingStatusNotification(getApplicationContext()); //activate the spoofing-status notification

            saveJsonFile = this.checkJsonAndLocationPermissions()[0];

            final float amplitude = getSettingsObject().getFloat(ConfigConstants.BUFFER_HISTORY_AMPLITUDE_SHARED_PREF, ConfigConstants.BUFFER_HISTORY_AMPLITUDE_SHARED_PREF_DEFAULT);
            if (saveJsonFile && locationTrack) {
                final String address = locationFinder.getDetectedDBEntryAddres();
                jsonMan.addJsonObject(locationFinder.getDetectedDBEntry(), technology.toString(), ConfigConstants.DETECTION_TYPE_ALWAYS_BLOCKED_HERE, address, false, amplitude, getDetectionAddressState(address)); //adding the found signal in the JSON file
                checkIfShouldBeSharedAndSendDetection(locationFinder.getDetectedDBEntry(), technology.toString(), ConfigConstants.DETECTION_TYPE_ALWAYS_BLOCKED_HERE, amplitude);
            }
            if (saveJsonFile && !locationTrack) {
                final double[] noLocation = {0,0};
                jsonMan.addJsonObject(noLocation, technology.toString(), ConfigConstants.DETECTION_TYPE_ALWAYS_BLOCKED_HERE, DetectionAddressStateEnum.NOT_AVAILABLE.toString(), false, amplitude, DetectionAddressStateEnum.NOT_AVAILABLE.getId());
                checkIfShouldBeSharedAndSendDetection(noLocation, technology.toString(), ConfigConstants.DETECTION_TYPE_ALWAYS_BLOCKED_HERE, amplitude);
            }

            locationFinder.blockMicOrSpoof(this); //try for microphone access and choose the blocking method
        } else { // The user does not prefer to block every location
            if (locationTrack && jsonMan.checkIfJsonFileIsAvailable()) { // If the user allowed location and has a JSON file
                if(locationFinder.checkExistingLocationDB(lastPosition, technology, this)){ // Check our detection DB and follow user (stored) preference if it is not a new location
                    final float amplitude = sp.getFloat(ConfigConstants.BUFFER_HISTORY_AMPLITUDE_SHARED_PREF, ConfigConstants.BUFFER_HISTORY_AMPLITUDE_SHARED_PREF_DEFAULT);
                    checkIfShouldBeSharedAndSendDetection(lastPosition, technology.toString(), ConfigConstants.DETECTION_TYPE_ALWAYS_BLOCKED_HERE, amplitude);
                }
            }
            else {
                // Notify the user
                NotificationHelper.activateDetectionAlertStatusNotification(getApplicationContext(), technology);
                this.activateAlert(technology); //open the alert dialog if the app is visible
            }
        }
    }

    /**
     * Preprocess the buffer history (containing the detected signal).
     * Convert to mono, compute RMS and apply a high pass filter.
     * @param bufferHistory Stereo interleaved audio buffer
     * @return the preprocessed audio buffer
     */
    private float[] preProcessing(float[] bufferHistory) {
        int maxValueIndex = -1;
        float maxValue = Integer.MIN_VALUE;

        float[] highPassedArray = new float[bufferHistory.length/2];

        Butterworth butterworthUp = new Butterworth();
        int bandPassFilterOrder = ConfigConstants.BANDPASS_FILTER_ORDER;
        double Fs = ConfigConstants.SCAN_SAMPLE_RATE;
        double centerFrequencyBandPass = ConfigConstants.BANDPASS_CENTER_FREQUENCY;
        double bandpassWidth = ConfigConstants.BANDPASS_WIDTH;
        butterworthUp.highPass(bandPassFilterOrder, Fs, ConfigConstants.HIGHPASS_CUTOFF_FREQUENCY);
        double squareSum = 0.0;
        for(int i = 1, counter = 0; i < bufferHistory.length; i+=2, counter++) {
            float value = (float) butterworthUp.filter((bufferHistory[i] + bufferHistory[i-1])/2);
            highPassedArray[counter] = value;
            if (value > maxValue) {
                maxValue = value;
                maxValueIndex = counter;
            }
            squareSum += value*value;
        }
        float amplitude = (float) Math.sqrt(squareSum/highPassedArray.length);

        SharedPreferences sp = getSettingsObject();
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(ConfigConstants.MAX_VALUE_INDEX_SHARED_PREF, maxValueIndex);
        ed.putFloat(ConfigConstants.BUFFER_HISTORY_AMPLITUDE_SHARED_PREF, amplitude);
        ed.apply();
        return highPassedArray;
    }

    public void startDetection(){
        SharedPreferences settings = getSettingsObject(); //get the settings
        saveJsonFile = settings.getBoolean(ConfigConstants.SETTING_SAVE_DATA_TO_JSON_FILE, ConfigConstants.SETTING_SAVE_DATA_TO_JSON_FILE_DEFAULT);

        if(saveJsonFile) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    if (!jsonMan.checkIfJsonFileIsAvailable()) { //check if a JSON File is already there in the storage
                        jsonMan.createJsonFile(); //create a JSON file
                    }
                }
            });
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    if (!jsonMan.checkIfSavefolderIsAvailable()) { //check if a folder for the audio files is already there in the storage
                        jsonMan.createSaveFolder(); //create a folder for the audio files
                    }
                }
            });
        }

        NotificationHelper.activateScanningStatusNotification(getApplicationContext()); //start the scanning-status notification
        setGUIStateStarted();
        detector.startScanning(this); //start scanning for signals
    }

    /**
     * Pauses the scan and blocking process. Reset the UI to paused state.
     * Called when clicking on the pause button.
     */
    public void pauseFirewall(){
        NotificationHelper.activateOnHoldStatusNotification(getApplicationContext()); //activate only the onHold-status notification again
        detector.pause(); // pause scanning
        if (alert.getDialog() != null) {
            alert.getDialog().dismiss();
        }
        Spoofer spoof = Spoofer.getInstance(); //get the spoofing object
        spoof.stopSpoofingComplete(); //stop the whole spoofing process
        MicCapture micCap = MicCapture.getInstance(); //get the microphone capture object
        micCap.stopMicCapturingComplete(); //stop the blocking process via the microphone
        usedBlockingMethod = null;

        setGUIStatePaused();
        SharedPreferences sp = getSettingsObject();
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(ConfigConstants.PREFERENCES_APP_STATE, StateEnum.ON_HOLD.toString());
        ed.apply();
        updateStateText();
    }

    public void setGUIStatePaused() {
        final Context context = this.getApplicationContext();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    // Set the right icon and tinting color
                    tintCompat(btnStartPause, R.drawable.ic_play_arrow_white_48dp, R.color.colorPrimary, context);
                }
                else {
                    btnStartPause.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play_arrow_white_48dp, 0, 0);
                }
                btnStartPause.setText(R.string.button_start_firewall);
            }
        });
    }

    public void setGUIStateStarted() {
        final Context context = this.getApplicationContext();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    // Set the right icon and tinting color
                    tintCompat(btnStartPause, R.drawable.ic_pause_white_48dp, R.color.colorPrimary, context);
                }
                else {
                    btnStartPause.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pause_white_48dp, 0, 0);
                }
                btnStartPause.setText(R.string.button_pause_firewall);
            }
        });
    }

    private void tintCompat(Button button, int drawableIcon, int tintColor, Context context) {
        // Source: https://stackoverflow.com/a/29890717
        Drawable drawable = ContextCompat.getDrawable(context, drawableIcon);
        // TODO: test on Android 16 and > 23, works for 22.
        Drawable wrapDrawable = DrawableCompat.wrap(drawable);
        button.setCompoundDrawablesWithIntrinsicBounds(null, wrapDrawable, null, null);
        DrawableCompat.setTint(wrapDrawable, getResources().getColor(tintColor));
    }

    public void openStoredLocations(){
        if (jsonMan.checkIfJsonFileIsAvailable()) {
            Intent myIntent = new Intent(MainActivity.this, StoredLocations.class); //redirect to the stored locations activity
            startActivityForResult(myIntent, 0);
        }
        else {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle(R.string.alert_no_json_file_title)
                    .setMessage(R.string.alert_no_json_file_message)
                    .setPositiveButton(R.string.alert_no_json_file_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getSettingsObject().edit().putBoolean(ConfigConstants.SETTING_SAVE_DATA_TO_JSON_FILE, true).apply();

                            threadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    jsonMan.createJsonFile();
                                }
                            });
                            Intent myIntent = new Intent(MainActivity.this, StoredLocations.class); //redirect to the stored locations activity
                            startActivityForResult(myIntent, 0);
                        }
                    })
                    .setNegativeButton(R.string.alert_no_json_file_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create()
            .show();
        }
    }

    public void openSettings(){
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class); //redirect to the settings activity
        startActivityForResult(myIntent, 0);
        String uniqueID = UUID.randomUUID().toString();
    }

    public void displayToast(final String toastText,final int duration){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainIsMain, toastText,duration).show();
            }
        });
    }

    public void onAlertChoice(final int spoofDecision) {
        if(usedBlockingMethod != null) {
            stopAutomaticBlockingMethodOnAction();
        }

        final float amplitude = sharedPref.getFloat(ConfigConstants.BUFFER_HISTORY_AMPLITUDE_SHARED_PREF, ConfigConstants.BUFFER_HISTORY_AMPLITUDE_SHARED_PREF_DEFAULT);

        // TODO: Function to be called in a thread, for IO (save json entry)

        saveJsonFile = checkJsonAndLocationPermissions()[0];
        boolean locationTrack = checkJsonAndLocationPermissions()[1];

        if(saveJsonFile && locationTrack && entryWasAskedAgain) {
            entryWasAskedAgain = false;
            // Update entry
            final double[] detectedSignalPosition = locationFinder.getDetectedDBEntry();
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    jsonMan.updateSpoofStatusOfRules(detectedSignalPosition, sigType.toString(), spoofDecision, MainActivity.this);
                    //Update Detection Counter
                    jsonMan.updateSignalAndImportedDetectionCounter(detectedSignalPosition, sigType.toString(), MainActivity.this);
                    jsonMan.setLatestDate(detectedSignalPosition, sigType, MainActivity.this);
                    String address = locationFinder.getDetectedDBEntryAddres();
                    jsonMan.addJsonObject(detectedSignalPosition, sigType.toString(), spoofDecision, address, true, amplitude, getDetectionAddressState(address));
                    checkIfShouldBeSharedAndSendDetection(detectedSignalPosition, sigType.toString(), spoofDecision, amplitude);
                }
            });
        }else if(saveJsonFile && locationTrack && !entryWasAskedAgain) {
            final double[] detectedSignalPosition = locationFinder.getDetectedDBEntry();
            final String address = locationFinder.getDetectedDBEntryAddres();
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    jsonMan.addJsonObject(detectedSignalPosition, sigType.toString(), spoofDecision, address, false, amplitude, getDetectionAddressState(address)); //adding the found signal in the JSON file
                    checkIfShouldBeSharedAndSendDetection(detectedSignalPosition, sigType.toString(), spoofDecision, amplitude);
                }
            });
            if (detectedSignalPosition[0] == 0 && detectedSignalPosition[1] == 0) {
                Toast toast = Toast.makeText(MainActivity.this, R.string.toast_no_location_text, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }else if(saveJsonFile&&!locationTrack){
            final double[] noLocation = {0,0};
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    jsonMan.addJsonObject(noLocation, sigType.toString(), spoofDecision, DetectionAddressStateEnum.NOT_AVAILABLE.toString(), false, amplitude, DetectionAddressStateEnum.NOT_AVAILABLE.getId());
                    checkIfShouldBeSharedAndSendDetection(noLocation, sigType.toString(), spoofDecision, amplitude);
                }
            });
        }

        alert.getDialog().dismiss(); //cancel the alert dialog
        NotificationHelper.cancelDetectionAlertStatusNotification(getApplicationContext());

        // Releasing audio resources
        if (sigPlayer != null ) {
            sigPlayer.release(); //release the resources of the player
            sigPlayer = null; //set the player variable to null
            isSignalPlayerGenerated = false; //now there is no player anymore so it's false
        }
        if (pitchShiftPlayer != null) {
            pitchShiftPlayer.cleanup();
            pitchShiftPlayer.removeDetectionListener();
            pitchShiftPlayer = null;
            isSignalPlayerGenerated = false;
            setAlertUIReplayStopped();
        }
    }

    public int getDetectionAddressState(String address){
        if(DetectionAddressStateEnum.UNKNOWN.toString().equals(address)){
            return DetectionAddressStateEnum.UNKNOWN.getId();
        }else if(DetectionAddressStateEnum.NOT_AVAILABLE.toString().equals(address)){
            return DetectionAddressStateEnum.NOT_AVAILABLE.getId();
        }else{
            return DetectionAddressStateEnum.RESOLVED.getId();
        }
    }

    public void checkIfShouldBeSharedAndSendDetection(double[] position, String technology, int spoof, float amplitude){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean shouldBeShared = sp.getBoolean(ConfigConstants.LAST_DECISION_ON_SHARING, ConfigConstants.SETTINGS_SHARING_DEFAULT_VALUE);
        if (shouldBeShared) {
            wakeupServer(position[0], position[1], Technology.fromString(technology).getId(), technology, JSONManager.returnDateString(), spoof, amplitude);
        }
    }


    // DetectionDialogListener methods ----------
    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the DetectionDialogFragment.DetectionDialogListener interface

    @Override
    public void onAlertPlayDetectedSignal(DialogFragment dialog){

        if (pitchShiftPlayer == null) {
            pitchShiftPlayer = new PitchShiftPlayer();
            pitchShiftPlayer.setDetectionListener(this);
        }
        if (!isSignalPlayerGenerated) {
            File file = new File (this.getApplicationContext().getFilesDir(), "detection.wav");

            pitchShiftPlayer.startAudio(ConfigConstants.SCAN_SAMPLE_RATE, 4410);
            pitchShiftPlayer.openFile(file.getPath(), 0, (int) file.length());
            isSignalPlayerGenerated = true;
        }

        pitchShiftPlayer.playPause();
        if (pitchShiftPlayer.isPlaying()) {
            setAlertUIReplayStarted();
        }
        else {
            setAlertUIReplayStopped();
        }
    }

    @Override
    public void onAlertBlockAlways(DialogFragment dialog){
        onAlertChoice(ConfigConstants.DETECTION_TYPE_ALWAYS_BLOCKED_HERE);
        showToastOnNoLocation();
        checkForActivatedLocation();
        locationFinder.blockMicOrSpoof(this);
        NotificationHelper.activateSpoofingStatusNotification(getApplicationContext()); //activates the notification for the spoofing process
    }

    @Override
    public void onAlertBlockThisTime(DialogFragment dialog){
        onAlertChoice(ConfigConstants.DETECTION_TYPE_BLOCKED_THIS_TIME);
        locationFinder.blockMicOrSpoof(this);
        NotificationHelper.activateSpoofingStatusNotification(getApplicationContext());
    }

    @SuppressLint("WrongConstant") // Bug in the linting, see https://stackoverflow.com/a/31979209
    @Override
    public void onAlertDismissAlways(DialogFragment dialog){
        onAlertChoice(ConfigConstants.DETECTION_TYPE_ALWAYS_DISMISSED_HERE);
        showToastOnNoLocation();
        checkForActivatedLocation();
        detector.startScanning(this); //start scanning again
        NotificationHelper.activateScanningStatusNotification(getApplicationContext()); //activates the notification for the scanning process
        Snackbar.make(((ViewGroup) MainActivity.this
                .findViewById(android.R.id.content)).getChildAt(0),
                dialog.getContext().getString(R.string.onAllowPauseFirewallHint),
                ON_ALLOW_SNACKBAR_DURATION).show();
    }

    @SuppressLint("WrongConstant") // Bug in the linting, see https://stackoverflow.com/a/31979209
    @Override
    public void onAlertDismissThisTime(DialogFragment dialog){
        onAlertChoice(ConfigConstants.DETECTION_TYPE_DISMISSED_THIS_TIME);
        detector.startScanning(this); //start scanning again
        NotificationHelper.activateScanningStatusNotification(getApplicationContext()); //activates the notification for the scanning process

        Snackbar.make(((ViewGroup) MainActivity.this
                .findViewById(android.R.id.content)).getChildAt(0),
                dialog.getContext().getString(R.string.onAllowPauseFirewallHint),
                ON_ALLOW_SNACKBAR_DURATION).show();
    }

    @Override
    public void onAlertSharingInfo(DialogFragment dialog){
        final AlertDialog.Builder alertSharingInfoDialog = new AlertDialog.Builder(this);
        alertSharingInfoDialog
                .setTitle(dialog.getContext().getString(R.string.alert_share_your_detection))
                .setMessage(dialog.getContext().getString(R.string.alert_sharing_info))
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertSharingInfo.cancel();
                    }
                });
        alertSharingInfo = alertSharingInfoDialog.show();
    }

    @Override
    public void onAlertRuleInfo(DialogFragment dialog){
        final AlertDialog.Builder alertRuleInfoDialog = new AlertDialog.Builder(this);
        alertRuleInfoDialog
                .setTitle(dialog.getContext().getString(R.string.alert_save_as_firewall_rule))
                .setMessage(dialog.getContext().getString(R.string.alert_save_firewall_info))
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertRuleInfo.cancel();
                    }
                });
        alertRuleInfo = alertRuleInfoDialog.show();
    }

    // END DetectionDialogListener methods ----------

    public static MediaPlayer generatePlayer(Context context, final DialogFragment dialog){
        File file = new File(context.getFilesDir(), "detection.wav");
        Uri uri = Uri.fromFile(file);
        MediaPlayer mediaPlayer = MediaPlayer.create(context, uri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                DetectionDialogFragment detectionDialog = (DetectionDialogFragment) dialog;
                detectionDialog.btnAlertReplay.setText(R.string.alertDialog_option_play); //set the button for playing/stopping to "play"
            }
        });
        return mediaPlayer;
    }

    public void updateJSONHistory(){
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                jsonMan.updateJSONHistory();
            }
        });
    }

    public void checkFirstRunForWelcomeShowing() {
        SharedPreferences sp = getSettingsObject();
        boolean isFirstRun = sp.getBoolean("isFirstRun", true);
        //Log.d("I am in First run", String.valueOf(isFirstRun));
        if (isFirstRun){
            openInstructions();
            sp
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
    }

    public void checkForJSONHistoryUpdate(){
        SharedPreferences sp = getSettingsObject();
        boolean isJSONHistoryNotUpdated = sp.getBoolean("isJSONHistoryNotUpdated", true);
        if (isJSONHistoryNotUpdated){
            updateJSONHistory();
            sp
                    .edit()
                    .putBoolean("isJSONHistoryNotUpdated", false)
                    .apply();
        }
    }

    public void checkForActivatedLocation(){
        locationManager = (LocationManager) this.getApplicationContext().getSystemService(LOCATION_SERVICE);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        boolean locationTrackGps = sharedPref.getBoolean(ConfigConstants.SETTING_GPS, ConfigConstants.SETTING_GPS_DEFAULT);
        boolean locationTrackNet = sharedPref.getBoolean(ConfigConstants.SETTING_NETWORK_USE, ConfigConstants.SETTING_NETWORK_USE_DEFAULT);

        int status = ActivityCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if(locationTrackGps || locationTrackNet){
            if((!(isGPSEnabled && locationTrackGps) && !(isNetworkEnabled && locationTrackNet)) || status != PackageManager.PERMISSION_GRANTED){
                activateAlertNoLocationEnabled();
            }else{

            }
        }
    }

    public void activateAlertNoLocationEnabled() {
        boolean skipMessage = sharedPref.getBoolean(ConfigConstants.SETTINGS_ALERT_LOCATION_IS_OFF_DONT_ASK_AGAIN, ConfigConstants.SETTINGS_ALERT_LOCATION_IS_OFF_DONT_ASK_AGAIN_DEFAULT);
        if (!skipMessage) {
            final AlertDialog.Builder activateLocationDialog = new AlertDialog.Builder(this);
            LayoutInflater locationAlertInflater = LayoutInflater.from(this);
            View cbLocationAlertLayout = locationAlertInflater.inflate(R.layout.alert_checkbox, null);
            final CheckBox dontShowAgain = (CheckBox)cbLocationAlertLayout.findViewById(R.id.cbDontAskAgain);
            activateLocationDialog.setView(cbLocationAlertLayout);
            activateLocationDialog.setTitle(R.string.alert_location_is_off_title)
                    .setMessage(R.string.alert_location_is_off_message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.alert_location_is_off_positive, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (dontShowAgain.isChecked()) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean(ConfigConstants.SETTINGS_ALERT_LOCATION_IS_OFF_DONT_ASK_AGAIN, true);
                                editor.apply();
                            }
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.alert_location_is_off_negative, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (dontShowAgain.isChecked()) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean(ConfigConstants.SETTINGS_ALERT_LOCATION_IS_OFF_DONT_ASK_AGAIN, true);
                                editor.apply();
                            }
                            alertLocation.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info);

            alertLocation = activateLocationDialog.show();
        }
    }

    public void showToastOnNoLocation(){
        locationManager = (LocationManager) this.getApplicationContext().getSystemService(LOCATION_SERVICE);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        boolean locationTrackGps = sharedPref.getBoolean(ConfigConstants.SETTING_GPS, ConfigConstants.SETTING_GPS_DEFAULT);
        boolean locationTrackNet = sharedPref.getBoolean(ConfigConstants.SETTING_NETWORK_USE, ConfigConstants.SETTING_NETWORK_USE_DEFAULT);

        int status = ActivityCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if(locationTrackGps || locationTrackNet){
            if((!(isGPSEnabled && locationTrackGps) && !(isNetworkEnabled && locationTrackNet)) || status != PackageManager.PERMISSION_GRANTED){
                Toast toast = Toast.makeText(MainActivity.this, R.string.toast_no_location_text, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }else{
            Toast toast = Toast.makeText(MainActivity.this, R.string.toast_no_location_text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public String getTimeAndDateForAlert(){
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat rightFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat leftFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateLeft = String.valueOf(leftFormat.format(currentTime));
        String dateRight = String.valueOf(rightFormat.format(currentTime));
        String dateString = dateRight + " - " + dateLeft;
        return dateString;
    }

    public void activateNoMicrophoneAccessAlertDialog(){
        final AlertDialog.Builder activateLocationDialog = new AlertDialog.Builder(this);
        activateLocationDialog.setTitle(R.string.alert_microphone_already_used_title)
                .setMessage(R.string.alert_microphone_already_used_message)
                .setCancelable(true)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        activateLocationDialog.show();
    }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public void wakeupServer(final double longitude, final double latitude, final int technologyid, final String technology, final String timestamp, final int spoofDecision, final float amplitude) {

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                final SoniControlAPI restService = RESTController.getRetrofitInstance().create(SoniControlAPI.class);

                restService.wakeupServer().enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()) {
                            Log.i(TAG, "post submitted to API. wakeup" + response.body().toString());
                            sendDetection(longitude, latitude, Technology.fromString(technology).getId(), technology, JSONManager.returnDateString(), spoofDecision, amplitude);
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            SharedPreferences.Editor ed = sp.edit();
                            ed.putInt(ConfigConstants.WAKEUP_COUNTER, WAKEUP_COUNTER_DEFAULT);
                            ed.apply();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        int wakeupCounter = sp.getInt(ConfigConstants.WAKEUP_COUNTER, WAKEUP_COUNTER_DEFAULT);
                        Log.e(TAG, wakeupCounter + "Unable to submit post to API. wakeup" + t);
                        if(wakeupCounter >= 0){
                            wakeupServer(longitude, latitude, Technology.fromString(technology).getId(), technology, JSONManager.returnDateString(), spoofDecision, amplitude);
                            SharedPreferences.Editor ed = sp.edit();
                            ed.putInt(ConfigConstants.WAKEUP_COUNTER, wakeupCounter - 1);
                            ed.apply();
                            Log.e(TAG, "wakeupCounter >= 0");
                        }else{
                            Log.e(TAG, "else SharedPreferences.Editor ed = sp.edit();");
                            SharedPreferences.Editor ed = sp.edit();
                            ed.putInt(ConfigConstants.WAKEUP_COUNTER, WAKEUP_COUNTER_DEFAULT);
                            ed.apply();
                        }
                    }
                });
            }
        });
    }

    public void sendDetection(final double longitude, final double latitude, final int technologyid, final String technology, final String timestamp, final int spoofDecision, final float amplitude) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                final SoniControlAPI restService = RESTController.getRetrofitInstance().create(SoniControlAPI.class);
                String filename = timestamp.replace(":","");
                String audiodata = "detection-"+technology+"-"+filename+".wav";

                restService.shareDetetion(longitude, latitude, spoofDecision, technologyid, technology, amplitude, timestamp, audiodata).enqueue(new Callback<Detection>() {
                    @Override
                    public void onResponse(Call<Detection> call, Response<Detection> response) {
                        if(response.isSuccessful()) {
                            Log.i(TAG, "post submitted to API." + response.body().toString());
                            Toast toast = Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.toast_on_success_detection_upload), Toast.LENGTH_LONG);
                            toast.show();
                            sendAudioData(restService, technology, timestamp);
                        }
                    }

                    @Override
                    public void onFailure(Call<Detection> call, Throwable t) {
                        Toast toast = Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.toast_on_failure_detection_upload), Toast.LENGTH_LONG);
                        toast.show();
                        Log.e(TAG, "Unable to submit post to API." + t);
                    }
                });
            }
        });
    }

    public void sendAudioData(SoniControlAPI restservice, String technology, String timestamp){
        File fileOld = new File(getApplicationContext().getFilesDir(), "detection.wav");
        String filename = timestamp.replace(":","");
        File fileNew = new File(getApplicationContext().getFilesDir(), "detection-"+technology+"-"+filename+".wav");
        fileOld.renameTo(fileNew);

        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(("audio/*")),
                        fileNew
                );

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("audio", fileNew.getName(), requestFile);

        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, "detection-"+technology+"-"+filename);

        restservice.uploadAudioFile(body, description).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Toast toast = Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.toast_on_success_audiodata_upload), Toast.LENGTH_LONG);
                toast.show();
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast toast = Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.toast_on_failure_audiodata_upload), Toast.LENGTH_LONG);
                toast.show();
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    /**
     * Resets the button text once the pitch-shifted replay is over.
     */
    @Override
    public void onPlayCompleted() {
        if (alert != null && alert.btnAlertReplay != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setAlertUIReplayStopped();
                }
            });
        }
    }

    private void setAlertUIReplayStopped() {
        alert.btnAlertReplay.setText(R.string.alertDialog_option_play);
        alert.btnAlertReplay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_play_arrow_white_36, 0, 0, 0);
    }

    private void setAlertUIReplayStarted() {
        alert.btnAlertReplay.setText(R.string.ButtonStopSignal);
        alert.btnAlertReplay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_pause_white_36, 0, 0, 0);
    }
}
