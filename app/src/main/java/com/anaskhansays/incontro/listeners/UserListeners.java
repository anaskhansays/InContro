package com.anaskhansays.incontro.listeners;

import com.anaskhansays.incontro.models.UserModel;

public interface UserListeners {

    void intiateVideoCall(UserModel userModel);
    void intiateVoiceCall(UserModel userModel);
    void onMultipleUserAction(Boolean isMutipleUserSelected);

}
