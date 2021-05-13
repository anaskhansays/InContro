package com.anaskhansays.incontro.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anaskhansays.incontro.R;
import com.anaskhansays.incontro.incontroUtils.Constants;
import com.anaskhansays.incontro.incontroUtils.PreferencesManager;
import com.anaskhansays.incontro.models.UserModel;
import com.anaskhansays.incontro.networkapis.ApiClient;
import com.anaskhansays.incontro.networkapis.ApiService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;
    private String invitingToken=null;
    private String callRoom=null;
    private String meetingType=null;

    private TextView textFirstChar;
    private TextView textUserName;
    private TextView textUserEmail;


    private int rejectionCount=0;
    private int totalReceivers=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);


        preferencesManager=new PreferencesManager(getApplicationContext());

        ImageView imageMeetingType=findViewById(R.id.imageMeetingType);
        meetingType=getIntent().getStringExtra("type");

        if(meetingType!=null){
            if(meetingType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_videocall);
            }else{
                imageMeetingType.setImageResource(R.drawable.ic_voicecall);
            }
        }



        textFirstChar=findViewById(R.id.txtFirstCharOut);
        textUserName=findViewById(R.id.txtUserNameOut);
        textUserEmail=findViewById(R.id.txtUserEmailOut);

        UserModel userModel=(UserModel)getIntent().getSerializableExtra("user");

        if(userModel!=null){
             textFirstChar.setText(userModel.firstname.substring(0,1));
             textUserName.setText(String.format("%s %s",userModel.firstname,userModel.lastname));
             textUserEmail.setText(userModel.email);
        }

        ImageView imageDisconnectCall=findViewById(R.id.imageDeclineCallOut);
        imageDisconnectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getIntent().getBooleanExtra("isMultiple",false)){
                    Type type=new TypeToken<ArrayList<UserModel>>(){}.getType();
                    ArrayList<UserModel> receivers=new Gson().fromJson(getIntent().getStringExtra("selectedUser"),type);
                    cancelInvite( null,receivers);
                }
                else{
                    if(userModel!=null){
                        cancelInvite(userModel.token,null);
                    }
                }

            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful() && task.getResult()!=null){
                    invitingToken=task.getResult().getToken();
                    if(meetingType!=null){
                        if(getIntent().getBooleanExtra("isMultiple",false)){
                            Type type=new TypeToken<ArrayList<UserModel>>(){}.getType();
                            ArrayList<UserModel> receivers=new Gson().fromJson(getIntent().getStringExtra("selectedUser"),type);
                            if(receivers!=null){
                                totalReceivers=receivers.size();
                            }
                            initiateMeeting(meetingType,null,receivers);
                        }else{
                            if(userModel!=null){
                                totalReceivers=1;
                                initiateMeeting(meetingType,userModel.token,null);
                            }
                        }
                    }

                }
            }
        });



    }


    private void initiateMeeting(String callType, String receiverToken, ArrayList<UserModel> receivers){
        try {
            JSONArray tokens=new JSONArray();

            if(receiverToken!=null){
                tokens.put(receiverToken);
            }

            if(receivers!=null && receivers.size()>0){
                StringBuilder userNames=new StringBuilder();
                for(int i=0;i<receivers.size();i++){
                    tokens.put(receivers.get(i).token);
                    userNames.append(receivers.get(i).firstname).append(" ").append(receivers.get(i).lastname).append("\n");
                }
                textFirstChar.setVisibility(View.GONE);
                textUserEmail.setVisibility(View.GONE);
                textUserName.setText(userNames.toString());
            }


            tokens.put(receiverToken);
            JSONObject body=new JSONObject();
            JSONObject data=new JSONObject();

            data.put(Constants.REMOTE_MESSAGE_TYPE,Constants.REMOTE_MESSAGE_INVITE);
            data.put(Constants.REMOTE_MESSAGE_CALL_TYPE,callType);
            data.put(Constants.KEY_FIRST_NAME,preferencesManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME,preferencesManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL,preferencesManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MESSAGE_INVITER_TOKEN,invitingToken);
            callRoom=
                    preferencesManager.getString(Constants.KEY_USER_ID)+"_"+
                            UUID.randomUUID().toString().substring(0,5);
            data.put(Constants.REMOTE_MESSAGE_CALL_ROOM,callRoom);


            body.put(Constants.REMOTE_MESSAGE_DATA,data);
            body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS,tokens);

            sendRemoteMsg(body.toString(),Constants.REMOTE_MESSAGE_INVITE);

        } catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMsg(String remoteMsg,String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(),remoteMsg
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(Constants.REMOTE_MESSAGE_INVITE)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Call Connected", Toast.LENGTH_SHORT).show();
                    }
                    else if(type.equals(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Call Cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else{
                    Toast.makeText(OutgoingInvitationActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(OutgoingInvitationActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelInvite(String receiverToken,ArrayList<UserModel> receivers){
        try{
            JSONArray tokens=new JSONArray();

            if(receiverToken!=null){
                tokens.put(receiverToken);
            }

            if(receivers!=null && receivers.size()>0){
                for(UserModel userModel:receivers){
                    tokens.put(userModel.token);
                }
            }

            tokens.put(receiverToken);

            JSONObject body=new JSONObject();
            JSONObject data=new JSONObject();

            data.put(Constants.REMOTE_MESSAGE_TYPE,Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE,Constants.REMOTE_INVITE_CANCELLED);

            body.put(Constants.REMOTE_MESSAGE_DATA,data);
            body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS,tokens);

            sendRemoteMsg(body.toString(),Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);


        } catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver invitationResponseReciever=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type=intent.getStringExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
            if(type!=null){
                if(type.equals(Constants.REMOTE_INVITE_ACCEPTED)){
                    try {

                        URL serverURL=new URL("https://meet.jit.si");

                        JitsiMeetConferenceOptions.Builder builder=new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(callRoom);

                        if(meetingType.equals("audio")){
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this,builder.build());
                        finish();

                    } catch (Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else if(type.equals(Constants.REMOTE_INVITE_DECLINED)){
                    rejectionCount+=1;
                    if(rejectionCount == totalReceivers ){
                        Toast.makeText(context,"Call Declined", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReciever,
                new IntentFilter(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
            invitationResponseReciever
        );
    }
}