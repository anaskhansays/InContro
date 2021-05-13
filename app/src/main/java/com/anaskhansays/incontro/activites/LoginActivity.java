package com.anaskhansays.incontro.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anaskhansays.incontro.R;
import com.anaskhansays.incontro.incontroUtils.Constants;
import com.anaskhansays.incontro.incontroUtils.PreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


public class LoginActivity extends AppCompatActivity {


    private EditText inputEmail,inputPassword;
    private MaterialButton signInBtn;
    private ProgressBar progressBar;

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferencesManager=new PreferencesManager(getApplicationContext());

        if(preferencesManager.getBoolean(Constants.KET_IS_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }

        findViewById(R.id.txtSignUpLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            }
        });

        inputEmail=findViewById(R.id.inputLoginEmail);
        inputPassword=findViewById(R.id.inputLoginPassword);
        signInBtn=findViewById(R.id.buttonSignIn);

        progressBar=findViewById(R.id.signInProgressBar);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputEmail.getText().toString().trim().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                    Toast.makeText(LoginActivity.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                }else if(inputPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else{
                        logIn();
                }
            }
        });



    }

    private void logIn(){
        signInBtn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){

                            DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);

                            preferencesManager.putBoolean(Constants.KET_IS_SIGNED_IN,true);
                            preferencesManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                            preferencesManager.putString(Constants.KEY_FIRST_NAME,documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                            preferencesManager.putString(Constants.KEY_LAST_NAME,documentSnapshot.getString(Constants.KEY_LAST_NAME));
                            preferencesManager.putString(Constants.KEY_EMAIL,documentSnapshot.getString(Constants.KEY_EMAIL));

                            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);

                        }

                        else{
                            progressBar.setVisibility(View.INVISIBLE);
                            signInBtn.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "Unable to sign in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}