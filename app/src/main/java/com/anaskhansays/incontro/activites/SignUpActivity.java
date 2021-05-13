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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText inputFirstName,inputLastName,inputEmail,inputPassword,inputConfirmPassword;
    private MaterialButton signUpBtn;
    private ProgressBar progressBar;
    private PreferencesManager preferencesManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        preferencesManager=new PreferencesManager(getApplicationContext());

        findViewById(R.id.imageBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.txtSignInLink).setOnClickListener(v -> onBackPressed());



        inputFirstName=findViewById(R.id.inputFirstName);
        inputLastName=findViewById(R.id.inputLastName);
        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        inputConfirmPassword=findViewById(R.id.inputConfirmPassword);

        signUpBtn=findViewById(R.id.buttonSignUp);
        progressBar=findViewById(R.id.signUpProgress);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputFirstName.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUpActivity.this, "First name cannot be empty", Toast.LENGTH_SHORT).show();
                }else if(inputLastName.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUpActivity.this, "Last name cannot be empty", Toast.LENGTH_SHORT).show();
                }else if(inputEmail.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUpActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                    Toast.makeText(SignUpActivity.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                }else if(inputPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUpActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                }else if(inputConfirmPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUpActivity.this, "Confirm Password", Toast.LENGTH_SHORT).show();
                }else if(!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())){
                    Toast.makeText(SignUpActivity.this, "Passwords do not match ", Toast.LENGTH_SHORT).show();
                }
                else{
                    signUp();
                }
            }
        });
    }

    private void signUp(){

        signUpBtn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String,Object> user=new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME,inputFirstName.getText().toString().trim());
        user.put(Constants.KEY_LAST_NAME,inputLastName.getText().toString().trim());
        user.put(Constants.KEY_EMAIL,inputEmail.getText().toString().trim());
        user.put(Constants.KEY_PASSWORD,inputPassword.getText().toString().trim());

        database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                preferencesManager.putBoolean(Constants.KET_IS_SIGNED_IN,true);
                preferencesManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                preferencesManager.putString(Constants.KEY_FIRST_NAME,inputFirstName.getText().toString().trim());
                preferencesManager.putString(Constants.KEY_LAST_NAME,inputLastName.getText().toString().trim());
                preferencesManager.putString(Constants.KEY_EMAIL,inputEmail.getText().toString().trim());

                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                signUpBtn.setVisibility(View.VISIBLE);
                Toast.makeText(SignUpActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}