package com.anaskhansays.incontro.incontroUtils;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS="users";
    public static final String KEY_FIRST_NAME="first_name";
    public static final String KEY_LAST_NAME="last_name";
    public static final String KEY_EMAIL="email";
    public static final String KEY_PASSWORD="password";

    public static final String KEY_USER_ID="user_id";
    public static final String KEY_FCM_TOKEN="fcm_token";


    public static final String KEY_PREFERENCE_NAME="incontroPreference";
    public static final String KET_IS_SIGNED_IN="isSignedIn";

    public static final String REMOTE_MSG_AUTHORIZATION="Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE="Content-Type";

    public static final String REMOTE_MESSAGE_TYPE="type";
    public static final String REMOTE_MESSAGE_INVITE="invitation";
    public static final String REMOTE_MESSAGE_CALL_TYPE="callType";
    public static final String REMOTE_MESSAGE_INVITER_TOKEN="invitingToken";
    public static final String REMOTE_MESSAGE_DATA="data";
    public static final String REMOTE_MESSAGE_REGISTRATION_IDS="registration_ids";

    public static final String REMOTE_MESSAGE_INVITATION_RESPONSE="invitationResponse";

    public static final String REMOTE_INVITE_ACCEPTED="accepted";
    public static final String REMOTE_INVITE_DECLINED="declined";

    public static final String REMOTE_INVITE_CANCELLED="cancelled";

    public static final String REMOTE_MESSAGE_CALL_ROOM="callRoom";




    public static HashMap<String,String> getRemoteMessageHeaders(){
        HashMap<String,String> headers=new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=<YOUR_SERVER_AUTH_KEY_HER>"
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE,"application/json");
        return headers;
    }
}
