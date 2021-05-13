package com.anaskhansays.incontro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.anaskhansays.incontro.R;
import com.anaskhansays.incontro.listeners.UserListeners;
import com.anaskhansays.incontro.models.UserModel;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private List<UserModel> userModels;
    private UserListeners userListeners;
    private List<UserModel> selectedUsers;

    public UserAdapter(List<UserModel> userModels,UserListeners userListeners) {
        this.userModels = userModels;
        this.userListeners=userListeners;
        selectedUsers=new ArrayList<>();
    }

    public List<UserModel> getSelectedUsers(){
        return selectedUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.user_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(userModels.get(position));
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        TextView txtFirstLetter,txtUsername,txtEmail;
        ImageView imageAudio,imageVideo;
        ConstraintLayout userConatainer;
        ImageView imageselected;


        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFirstLetter=itemView.findViewById(R.id.txtFirstLetter);
            txtUsername=itemView.findViewById(R.id.textUsername);
            txtEmail=itemView.findViewById(R.id.textEmail);
            imageAudio=itemView.findViewById(R.id.imageVoice);
            imageVideo=itemView.findViewById(R.id.imageVideoCam);
            userConatainer=itemView.findViewById(R.id.userContainer);
            imageselected=itemView.findViewById(R.id.imageSelectedUser);

        }

        void setUserData(UserModel userModel){
            txtFirstLetter.setText(userModel.firstname.substring(0,1));
            txtUsername.setText(String.format("%s %s",userModel.firstname,userModel.lastname));
            txtEmail.setText(userModel.email);
            imageAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userListeners.intiateVoiceCall(userModel);
                }
            });

            imageVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userListeners.intiateVideoCall(userModel);
                }
            });

            userConatainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(imageselected.getVisibility()!=View.VISIBLE){
                        selectedUsers.add(userModel);
                        imageselected.setVisibility(View.VISIBLE);
                        imageVideo.setVisibility(View.GONE);
                        imageAudio.setVisibility(View.GONE);

                        userListeners.onMultipleUserAction(true);
                    }

                    return true;
                }
            });

            userConatainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imageselected.getVisibility()==View.VISIBLE){
                        selectedUsers.remove(userModel);
                        imageselected.setVisibility(View.GONE);
                        imageVideo.setVisibility(View.VISIBLE);
                        imageAudio.setVisibility(View.VISIBLE);
                        if(selectedUsers.size()==0){
                            userListeners.onMultipleUserAction(false);
                        }
                    }else{
                        if(selectedUsers.size()>0){
                            selectedUsers.add(userModel);
                            imageselected.setVisibility(View.VISIBLE);
                            imageVideo.setVisibility(View.GONE);
                            imageAudio.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }
}
