package com.anaskhansays.incontro.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anaskhansays.incontro.R;
import com.anaskhansays.incontro.incontroUtils.Constants;
import com.anaskhansays.incontro.networkapis.ApiClient;
import com.anaskhansays.incontro.networkapis.ApiService;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingCallActivity extends AppCompatActivity {

    private String meetingType=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);


        ImageView imageMeetingType=findViewById(R.id.imageMeetingTypeIn);
        meetingType=getIntent().getStringExtra(Constants.REMOTE_MESSAGE_CALL_TYPE);

        if(meetingType!=null){
            if(meetingType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_videocall);
            }else{
                imageMeetingType.setImageResource(R.drawable.ic_voicecall);
            }
        }

        TextView textFirstChar=findViewById(R.id.txtFirstCharIn);
        TextView textUserName=findViewById(R.id.txtUserNameIn);
        TextView textUserEmail=findViewById(R.id.txtUserEmailIn);

        String firstname=getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if(firstname!=null){
            textFirstChar.setText(firstname.substring(0,1));
        }

        textUserName.setText(String.format("%s %s",firstname,getIntent().getStringExtra(Constants.KEY_LAST_NAME)));

        textUserEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        ImageView imageAcceptInvite=findViewById(R.id.imageAcceptCallIn);
        imageAcceptInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitationResponse(
                        Constants.REMOTE_INVITE_ACCEPTED,
                        getIntent().getStringExtra(Constants.REMOTE_MESSAGE_INVITER_TOKEN)
                );
            }
        });


        ImageView imageDeclineInvite=findViewById(R.id.imageDeclineCallIn);
        imageDeclineInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitationResponse(
                        Constants.REMOTE_INVITE_DECLINED,
                        getIntent().getStringExtra(Constants.REMOTE_MESSAGE_INVITER_TOKEN)
                );
            }
        });
    }

    private void sendInvitationResponse(String type,String receiverToken){
        try{
            JSONArray tokens=new JSONArray();
            tokens.put(receiverToken);

            JSONObject body=new JSONObject();
            JSONObject data=new JSONObject();

            data.put(Constants.REMOTE_MESSAGE_TYPE,Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE,type);

            body.put(Constants.REMOTE_MESSAGE_DATA,data);
            body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS,tokens);

            sendRemoteMsg(body.toString(),type);


        } catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRemoteMsg(String remoteMsg,String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(),remoteMsg
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(Constants.REMOTE_INVITE_ACCEPTED)){

                        try{

                            URL serverURL=new URL("https://meet.jit.si");

                            JitsiMeetConferenceOptions.Builder builder=new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MESSAGE_CALL_ROOM));
                            if(meetingType.equals("audio")){
                                builder.setVideoMuted(true);
                            }

                            JitsiMeetActivity.launch(IncomingCallActivity.this,builder.build());
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(IncomingCallActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }


                    }
                    else
                    {
                        Toast.makeText(IncomingCallActivity.this, "Call Declined ", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else{
                    Toast.makeText(IncomingCallActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(IncomingCallActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BroadcastReceiver invitationResponseReciever=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type=intent.getStringExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
            if(type!=null){
                if(type.equals(Constants.REMOTE_INVITE_CANCELLED)){
                    Toast.makeText(context,"Call Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
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