package sling.com.freecast;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.slingmedia.slingPlayer.slingClient.SlingAsync;
import com.slingmedia.slingPlayer.slingClient.SlingBaseData;
import com.slingmedia.slingPlayer.slingClient.SlingBoxIdentityParams;
import com.slingmedia.slingPlayer.slingClient.SlingCallback;
import com.slingmedia.slingPlayer.slingClient.SlingClient;
import com.slingmedia.slingPlayer.slingClient.SlingRequestStatus;
import com.slingmedia.slingPlayer.slingClient.SlingSession;
import com.slingmedia.slingPlayer.slingClient.SlingSessionConstants;
import com.slingmedia.slingPlayer.slingClient.SlingStatsInfo;
import com.slingmedia.slingPlayer.slingClient.SlingStatus;
import com.slingmedia.slingPlayer.slingClient.SlingTimeShiftInfo;

import static com.slingmedia.slingPlayer.slingClient.SlingSessionConstants.ESlingRequestResultCode.ESlingRequestStatusSuccess;
import static com.slingmedia.slingPlayer.slingClient.SlingSessionConstants.ESlingRequestResultCode.ESlingRequestStatusSuccessState;
import static com.slingmedia.slingPlayer.slingClient.SlingSessionConstants.SlingVideoEventDefaultMask;
import static com.slingmedia.slingPlayer.slingClient.SlingSessionConstants.SlingVideoEventTakeOverMask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 *    "CB6455C3E2CDB828547A85B84DE1678F"
 *    "E1:E27D0D7BDC52497877D16DBD2F1EFF692D620E56BC192FB087171BFD8846E73A"
 */
public class MainActivity extends AppCompatActivity implements
        SlingCallback.SlingClientCallback, SlingCallback.SessionCallback, SlingCallback.SessionVideoCallback,
        View.OnClickListener {
    private static final String TAG = "MainActivity";

    private static final String PREF_NAME = "FreeCast_Test";
    private static final String FINDER_ID_PREF_KEY = "finder-id";
    private static final String PASSWORD_PREF_KEY = "password";
    private static final String RECEIVER_ID_PREF_KEY = "receiver-id";
    private static final String CHANNEL_NUMBER_PREF_KEY = "channel-number";

    private int _initRequestId;
    private int _reqIdStart;
    private int _reqIdStop;
    private int _reqIdTuneLive;
    private int _reqIdStopVideo;

    private SlingSession _session = null;

    private EditText _finderIdEditText;
    private EditText _passwordEditText;
    private EditText _receiverIdEditText;
    private EditText _channelNumberEditText;
    private Button _startSessionButton;
    private Button _stopSessionButton;
    private Button _startVideoButton;
    private Button _stopVideoButton;
    private View _viewForVideo;
    private TextView _requestStatusTextView;
    private TextView _sessionStatusTextView;
    private TextView _videoStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseView();

        fillValuesFromPreference();

        initialiseSlingClient();
    }

    @Override
    public void onPause() {
        super.onPause();
        TestStopVideo(false);
    }

    private void initialiseView() {
        Log.d(TAG, "initialiseView ++");
        _finderIdEditText = (EditText) findViewById(R.id.finderid);
        _passwordEditText = (EditText) findViewById(R.id.password);
        _receiverIdEditText = (EditText) findViewById(R.id.receiverid);
        _channelNumberEditText = (EditText) findViewById(R.id.channelnumber);

        _startSessionButton = (Button) findViewById(R.id.startsession);
        _stopSessionButton = (Button) findViewById(R.id.stopsession);
        _startVideoButton = (Button) findViewById(R.id.startvideo);
        _stopVideoButton = (Button) findViewById(R.id.stopvideo);

        _viewForVideo = findViewById(R.id.videoContainer);

        _requestStatusTextView = (TextView) findViewById(R.id.request_result);
        _sessionStatusTextView = (TextView) findViewById(R.id.session_status);
        _videoStatusTextView = (TextView) findViewById(R.id.video_status);

        _startSessionButton.setOnClickListener(this);
        _stopSessionButton.setOnClickListener(this);
        _startVideoButton.setOnClickListener(this);
        _stopVideoButton.setOnClickListener(this);
        Log.d(TAG, "initialiseView --");
    }

    private void initialiseSlingClient() {
        Log.d(TAG, "initialiseSlingClient ++");
        EngineInitParams initParams = new EngineInitParams(getApplicationContext());
        SlingClient.setAvoidUICallback(false);
        SlingRequestStatus reqInitStatus = SlingClient.init(initParams, this);
        if (reqInitStatus != null) {
            _initRequestId = reqInitStatus.getRequestId();
        }
        Log.d(TAG, "initialiseSlingClient --");
    }

    private void TestStart() {
        Log.d(TAG, "TestStart ++");
        SlingBoxIdentityParams boxIdentityParams; boxIdentityParams = SlingClient.createSlingBoxIdentityParams();

        String fId = _finderIdEditText.getText().toString();
        String pass = _passwordEditText.getText().toString();
        String receiverID = _receiverIdEditText.getText().toString();

        boxIdentityParams.setFinderId(fId);
        boxIdentityParams.setPassword(pass);
        boxIdentityParams.setReceiverID(receiverID);
        _session = SlingClient.createSlingSession(boxIdentityParams, this);
        SlingRequestStatus reqStartStatus = _session.start();

        String reqStatus = "";

        if(reqStartStatus != null){
            _reqIdStart = reqStartStatus.getRequestId();
            if(_reqIdStart != -1){
                if((ESlingRequestStatusSuccessState. getValue() == reqStartStatus.getCode())) {
                    reqStatus = "Session already Started";
                    Log.d(TAG, reqStatus);
                } else {
                    reqStatus = "Session Start in progress";
                    Log.d(TAG, reqStatus);
                }
            } else {
                reqStatus = "Session Start Failed";
                Log.d(TAG, reqStatus);
            }
        } else {
            reqStatus = "Session Start Failed";
            Log.d(TAG, reqStatus);
        }

        showRequestStatus(reqStatus);

        Log.d(TAG, "TestStart--");
    }

    private void TestStop() {
        Log.d(TAG, "TestStop ++");

        String reqStatus = "";

        if (null != _session) {
            SlingRequestStatus reqStopStatus = _session.stop();
            if (reqStopStatus != null) {
                _reqIdStop = reqStopStatus.getRequestId();
                if (_reqIdStop != -1) {
                    int code = reqStopStatus.getCode();
                    if ((ESlingRequestStatusSuccess.getValue() == code)) {
                        reqStatus = "Session Stop success";
                        Log.d(TAG, reqStatus);
                    } else {
                        reqStatus = "Session Stop Failed";
                        Log.d(TAG, reqStatus);
                    }
                } else {
                    reqStatus = "Session Stop Failed";
                    Log.d(TAG, reqStatus);
                }
            }
        }

        showRequestStatus(reqStatus);

        Log.d(TAG, "TestStop --");
    }

    private void TestTune(View videoView, String strChannel, boolean aConflictTakeover) {
        Log.d(TAG, "TestTune ++");
        int iVideoMask = SlingVideoEventDefaultMask;
        if(aConflictTakeover){
            iVideoMask = iVideoMask | SlingVideoEventTakeOverMask; }
        _session.registerVideoView(videoView, this);
        SlingRequestStatus reqTuneLiveStatus = _session.tune(strChannel);

        String reqStatus = "";

        if(reqTuneLiveStatus != null){
            _reqIdTuneLive = reqTuneLiveStatus.getRequestId();
            if(_reqIdTuneLive != -1) {
                int code = reqTuneLiveStatus.getCode();
                if (ESlingRequestStatusSuccess.getValue() == code) {
                    reqStatus = "Tune Success";
                    Log.d(TAG, reqStatus);
                } else {
                    reqStatus = "Tune Failure";
                    Log.d(TAG, reqStatus);
                }
            } else {
                reqStatus = "Tune Failure";
                Log.d(TAG, reqStatus);
            }
        }

        showRequestStatus(reqStatus);

        Log.d(TAG, "TestTune --");
    }

    private void TestStopVideo(boolean aVideoRequestConflicted) {
        Log.d(TAG, "TestStopVideo ++");
        if(_session != null) {
            SlingRequestStatus reqStopVideoStatus = _session.stopVideo(aVideoRequestConflicted);
            String reqStatus = "";

            if (reqStopVideoStatus != null) {
                _reqIdStopVideo = reqStopVideoStatus.getRequestId();
                if (_reqIdStopVideo != -1) {
                    int code = reqStopVideoStatus.getCode();
                    if (ESlingRequestStatusSuccess.getValue() == code) {
                        reqStatus = "StopVideo Success";
                        Log.d(TAG, reqStatus);
                    } else {
                        reqStatus = "StopVideo Failure";
                        Log.d(TAG, reqStatus);
                    }
                } else {
                    reqStatus = "StopVideo Failure";
                    Log.d(TAG, reqStatus);
                }
            }

            showRequestStatus(reqStatus);
        }
        Log.d(TAG, "TestStopVideo --");
    }

    private void saveValuesToPreference() {
        Log.d(TAG, "saveValuesToPreference ++");

        String finderId = _finderIdEditText.getText() != null ? _finderIdEditText.getText().toString() : "";
        String password = _passwordEditText.getText() != null ? _passwordEditText.getText().toString() : "";
        String receiverId = _receiverIdEditText.getText() != null ? _receiverIdEditText.getText().toString() : "";
        String channelNumber = _channelNumberEditText.getText() != null ? _channelNumberEditText.getText().toString() : "";

        Log.d(TAG, "fillValuesFromPreference, finderid: " + finderId + ", password: " + password +
                ", receiverId: " + receiverId + ", channelnumber: " + channelNumber);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0);
        pref.edit().putString(FINDER_ID_PREF_KEY, finderId).apply();
        pref.edit().putString(PASSWORD_PREF_KEY, password).apply();
        pref.edit().putString(RECEIVER_ID_PREF_KEY, receiverId).apply();
        pref.edit().putString(CHANNEL_NUMBER_PREF_KEY, channelNumber).apply();

        Log.d(TAG, "saveValuesToPreference --");
    }

    private void fillValuesFromPreference() {
        Log.d(TAG, "fillValuesFromPreference ++");

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0);
        String finderId = pref.getString(FINDER_ID_PREF_KEY, "");
        String password = pref.getString(PASSWORD_PREF_KEY, "");
        String receiverId = pref.getString(RECEIVER_ID_PREF_KEY, "");
        String channelNumber = pref.getString(CHANNEL_NUMBER_PREF_KEY, "");

        Log.d(TAG, "fillValuesFromPreference, finderid: " + finderId + ", password: " + password +
                ", receiverId: " + receiverId + ", channelnumber: " + channelNumber);

        _finderIdEditText.setText(finderId);
        _passwordEditText.setText(password);
        _receiverIdEditText.setText(receiverId);
        _receiverIdEditText.setText(receiverId);
        _channelNumberEditText.setText(channelNumber);

        Log.d(TAG, "fillValuesFromPreference --");
    }

    private void showRequestStatus(String result) {
        _requestStatusTextView.setText(result);
    }

    private void showSessionStatus(String result) {
        _sessionStatusTextView.setText(result);
    }

    private void showVideoStatus(String result) {
        _videoStatusTextView.setText(result);
    }

    private String getRequestResultString(int code) {
        Log.d(TAG, "getRequestResultString, code: " + code);
        String strRequestResultStatus = null;

        SlingSessionConstants.ESlingRequestResultCode resultCode = SlingSessionConstants.ESlingRequestResultCode.values()[code];

        switch(resultCode){
            case ESlingRequestStatusSuccess: strRequestResultStatus = "success"; break;
            case ESlingRequestStatusMethodNotSupported: strRequestResultStatus = "reason not supported"; break;
            case ESlingRequestRequestStatusFailure: strRequestResultStatus = "reason unknown"; break;
            case ESlingRequestStatusNotInitialized: strRequestResultStatus = "reason not initialised"; break;
            case ESlingRequestStatusInvalidPassword: strRequestResultStatus = "reason invalid password"; break;
            case ESlingRequestStatusStreamsBusy: strRequestResultStatus = "reason streams busy"; break;
            case ESlingRequestStatusStreamDisabled: strRequestResultStatus = "reason stream disabled"; break;
            case ESlingRequestStatusUnConfiguredBox: strRequestResultStatus = "reason unconfigured box"; break;
            case ESlingRequestStatusTimeout: strRequestResultStatus = "time out"; break;
            case ESlingRequestStatusBoxConnectionFailed: strRequestResultStatus = "reason connection failed"; break;
            case ESlingRequestStatusBoxOffline: strRequestResultStatus = "reason box offline"; break;
            case ESlingRequestStatusInvalidParameters: strRequestResultStatus = "reason invalid parameters"; break;
            case ESlingRequestStatusInvalidOperation: strRequestResultStatus = "reason invalid opration"; break;
            case ESlingRequestStatusInternalError: strRequestResultStatus = "reason internal error"; break;
        }

        return strRequestResultStatus;
    }

    //begin: View.OnClickListener
    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick");
        switch (view.getId()) {
            case R.id.startsession:
                saveValuesToPreference();
                TestStart();
                break;
            case R.id.stopsession:
                TestStop();
                break;
            case R.id.startvideo:
                String tuneCh = _channelNumberEditText.getText().toString();
                TestTune(_viewForVideo, tuneCh != null && tuneCh.length() > 0 ? tuneCh : "", false);
                break;
            case R.id.stopvideo:
                TestStopVideo(false);
                break;
        }
    }
    //end: View.OnClickListener

    //begin: SlingCallback.SlingClientCallback
    @Override
    public void OnSlingClientEvent(SlingStatus slingStatus) {
        Log.d(TAG, "OnSlingClientEvent");
    }

    @Override
    public void OnSlingRequestResult(SlingRequestStatus slingRequestStatus) {
        Log.d(TAG, "OnSlingRequestResult");
        String reqStatus = "";
        int reqId = slingRequestStatus.getRequestId();
        if(_initRequestId == reqId) {
            reqStatus = "Initialization successfully done";
        } else if(_reqIdStart == reqId){
            reqStatus = "Session start completed with " + getRequestResultString(slingRequestStatus.getCode());
        } else if(_reqIdStop == reqId){
            reqStatus = "Session stop completed with " + getRequestResultString(slingRequestStatus.getCode());
        } else if(_reqIdTuneLive == reqId) {
            reqStatus = "Tune start completed with " + getRequestResultString(slingRequestStatus.getCode());
        } else if(_reqIdStopVideo == reqId) {
            reqStatus = "Stop video completed with " + getRequestResultString(slingRequestStatus.getCode());
            _viewForVideo.setBackgroundColor(ContextCompat.getColor(this, R.color.videoView_background));
        }

        showRequestStatus(reqStatus);

        Log.d(TAG, "OnSlingRequestResult: " + reqStatus);
    }
    //end: SlingCallback.SlingClientCallback

    //begin: SlingCallback.SessionCallback
    @Override
    public void OnSlingSessionEvent(SlingStatus slingStatus) {
        Log.d(TAG, "OnSlingSessionEvent");

        String strSessionEvent = null;
        int dwEventCode = slingStatus.getCode();
        Log.d(TAG, "OnSlingSessionEvent event code" + dwEventCode);

        SlingSessionConstants.ESlingSessionStatusEvents eventCode = SlingSessionConstants.ESlingSessionStatusEvents.values()[dwEventCode];
        switch(eventCode){
            case ESlingSessionStatusNotConnected: strSessionEvent = "Not connected"; break;
            case ESlingSessionStatusLocating: strSessionEvent = "Locating..."; break;
            case ESlingSessionStatusConnecting: strSessionEvent = "Connecting..."; break;
            case ESlingSessionStatusConnected: strSessionEvent = "Connected"; break;
            case ESlingSessionStatusReady: strSessionEvent = "Ready"; break;
            case ESlingSessionStatusDisconnecting: strSessionEvent = "Disconnecting..."; break;
            case ESlingSessionStatusReconnecting: strSessionEvent = "Re-connecting..."; break;

            case ESlingSessionStatusInternalStatus: strSessionEvent = "UnknownCode("+eventCode+")"; break;
        }

        showSessionStatus(strSessionEvent);

        Log.d(TAG, "OnSlingSessionEvent: " + strSessionEvent);
    }

    @Override
    public void OnSlingSessionEvent(SlingAsync slingAsync) {
        Log.d(TAG, "OnSlingSessionEvent");
    }
    //end: SlingCallback.SessionCallback

    //begin: SlingCallback.SessionVideoCallback
    @Override
    public void OnSlingVideoEvent(SlingStatus slingStatus) {
        Log.d(TAG, "OnSlingVideoEvent");

        String strVideoEvent = null;
        int dwEventCode = slingStatus.getCode();
        Log.d(TAG, "OnSlingVideoEvent event code" + dwEventCode);
        SlingSessionConstants.ESlingVideoStatusEvents eventCode = SlingSessionConstants.ESlingVideoStatusEvents.values()[dwEventCode];

        switch (eventCode) {
            case ESlingVideoStatusNotStreaming:
                strVideoEvent = "Not streaming";
                break;
            case ESlingVideoStatusStreaming:
                strVideoEvent = "Streaming...";
                break;
            case ESlingVideoStatusStarting:
                strVideoEvent = "Starting...";
                break;
            case ESlingVideoStatusBuffering:
                strVideoEvent = "Buffering...";
                break;
            case ESlingVideoStatusOptimising:
                strVideoEvent = "Optimising...";
                break;
            case ESlingVideoStatusWeakSignal:
            case ESlingVideoStatusUnConfiguredBox:
            case ESlingVideoStatusAudioTracksAvailable:
            case ESlingVideoStatusCCTracksAvailable:
            case ESlingVideoStatusPaused:
            case ESlingVideoStatusPlaying:
                strVideoEvent = eventCode.name();
                break;
            default:
                break;
        }

        showVideoStatus(strVideoEvent);

        Log.d(TAG, "OnSlingVideoEvent: " + strVideoEvent);
    }

    @Override
    public void OnSlingVideoEvent(SlingAsync slingAsync) {
        Log.d(TAG, "OnSlingVideoEvent");
    }

    @Override
    public void OnSlingVideoError(SlingStatus slingStatus) {
        Log.d(TAG, "OnSlingVideoError");
    }

    @Override
    public void OnSlingVideoTimeShiftInfo(SlingTimeShiftInfo slingTimeShiftInfo) {
        Log.d(TAG, "OnSlingVideoTimeShiftInfo");
    }

    @Override
    public void OnSlingVideoStatsInfo(SlingStatsInfo slingStatsInfo) {
        Log.d(TAG, "OnSlingVideoStatsInfo");
    }

    @Override
    public void OnSlingVideoInternalDetails(boolean b, boolean b1, int i, int i1) {
        Log.d(TAG, "OnSlingVideoInternalDetails");
    }

    @Override
    public void OnSlingVideoRequestResponse(SlingRequestStatus slingRequestStatus, int i, ArrayList<SlingBaseData> arrayList) {

    }

    @Override
    public void OnSlingVideoThumbnailEvent(SlingStatus slingStatus) {

    }

    //end: SlingCallback.SessionVideoCallback
}