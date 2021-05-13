package com.anaskhansays.incontro.activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anaskhansays.incontro.R;
import com.anaskhansays.incontro.adapters.UserAdapter;
import com.anaskhansays.incontro.incontroUtils.Constants;
import com.anaskhansays.incontro.incontroUtils.PreferencesManager;
import com.anaskhansays.incontro.listeners.UserListeners;
import com.anaskhansays.incontro.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UserListeners {

    private PreferencesManager preferencesManager;
    private List<UserModel> userModelList;
    private UserAdapter userAdapter;
    private TextView txtErrorMsg;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageConference;

    private int REQUEST_CODE_BATTERY_OPTIMIZATION=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferencesManager=new PreferencesManager(getApplicationContext());

        TextView txtTitle=findViewById(R.id.textTitle);
        txtTitle.setText(String.format(
                "%s %s",
                preferencesManager.getString(Constants.KEY_FIRST_NAME),
                preferencesManager.getString(Constants.KEY_LAST_NAME)
        ));

        findViewById(R.id.textSignOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    signOut();
            }
        });


        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful() && task.getResult()!=null){
                    sendFCMTokentoDB(task.getResult().getToken());
                }
            }
        });

        RecyclerView userRecyclerView=findViewById(R.id.usersList);
        txtErrorMsg=findViewById(R.id.txtErrorMsgs);
        imageConference=findViewById(R.id.imageConference);

        userModelList=new ArrayList<>();
        userAdapter=new UserAdapter(userModelList,this);
        userRecyclerView.setAdapter(userAdapter);
        swipeRefreshLayout=findViewById(R.id.swipeRefLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsersList);
        getUsersList();
        checkForBatteryOptimization();

    }

    private void getUsersList(){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        swipeRefreshLayout.setRefreshing(false);
                        String mUserId=preferencesManager.getString(Constants.KEY_USER_ID);
                        if(task.isSuccessful() && task.getResult()!=null){
                            userModelList.clear();
                                for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                                    if(mUserId.equals(documentSnapshot.getId())){
                                        continue;
                                    }
                                    UserModel userModel=new UserModel();
                                    userModel.firstname=documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                                    userModel.lastname=documentSnapshot.getString(Constants.KEY_LAST_NAME);
                                    userModel.email=documentSnapshot.getString(Constants.KEY_EMAIL);
                                    userModel.token=documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                    userModelList.add(userModel);
                                }
                                if(userModelList.size()>0){
                                    userAdapter.notifyDataSetChanged();
                                }
                                else{
                                    txtErrorMsg.setText(String.format("%s","No users available"));
                                    txtErrorMsg.setVisibility(View.VISIBLE);
                                }
                        }
                        else{
                            txtErrorMsg.setText(String.format("%s","No users available"));
                            txtErrorMsg.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }


    private void  sendFCMTokentoDB(String token){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferencesManager.getString(Constants.KEY_USER_ID)
        );

        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Unable to send token: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void signOut(){
        Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferencesManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String,Object> updates=new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                preferencesManager.clearPreferences();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Unable to sign out at the moment"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void intiateVideoCall(UserModel userModel) {
        if(userModel.token==null || userModel.token.trim().isEmpty()){
            Toast.makeText(this, userModel.firstname+" "+userModel.lastname+" is not available for video call", Toast.LENGTH_SHORT).show();
        }
        else{
            Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
            intent.putExtra("user",userModel);
            intent.putExtra("type","video");
            startActivity(intent );

        }
    }

    @Override
    public void intiateVoiceCall(UserModel userModel) {
        if(userModel.token==null || userModel.token.trim().isEmpty()){
            Toast.makeText(this, userModel.firstname+" "+userModel.lastname+" is not available for call right now", Toast.LENGTH_SHORT).show();
        }
        else{
            Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
            intent.putExtra("user",userModel);
            intent.putExtra("type","audio");
            startActivity(intent);
        }
    }

    @Override
    public void onMultipleUserAction(Boolean isMutipleUserSelected) {
            if(isMutipleUserSelected){
                imageConference.setVisibility(View.VISIBLE);
                imageConference.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
                        intent.putExtra("selectedUser",new Gson().toJson(userAdapter.getSelectedUsers()));
                        intent.putExtra("type","video");
                        intent.putExtra("isMultiple",true);
                        startActivity(intent);
                    }
                });
            }
            else{
                imageConference.setVisibility(View.GONE);
            }
    }


    private void checkForBatteryOptimization(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            PowerManager powerManager=(PowerManager)getSystemService(POWER_SERVICE);
            if(!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Battery optimization is enabled. It can interrupt running background services.");
                builder.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent =new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivityForResult(intent,REQUEST_CODE_BATTERY_OPTIMIZATION);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE_BATTERY_OPTIMIZATION){
            checkForBatteryOptimization();
        }
    }
}