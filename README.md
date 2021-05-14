# InContro
 A video call/meeting or conference Android application
 
To implement real-time activity like video meetings we must have some identity or destination link to the receiver. 

For that users will sign up for their account using some basic details like the first name, last name, email, and password. To store all of the user data we will be using the cloud fire store database, which is a flexible and scalable database for mobile, web, and server development from Firebase and Google Cloud Platform.

After sign up, the user will sign in to their account using email and password. We will store logged user information into shared preferences to handle auto-sign in so users don't need to enter email and password every time.

After sign in, we will display a list of other users that are signed up in our application except for the currently logged user because nobody is going to start a video meeting with himself.

In a video meeting, the user will initiate the video meeting by sending a meeting invitation to another user. To send a meeting invitation we will use firebase cloud messaging which is a cross-platform messaging solution that lets you reliably send messages at no cost. Using FCM, you can notify a client app that a new email or other data is available to sync.

Once the meeting invitation sent, the receiver has two options, accept or reject the invitation. On acceptance or rejection, the related response message will be sent to the meeting initiator or sender. If the user wants to cancel the meeting invitation then it can be done by the hang-up process, in which another remote message will be sent to the receiver to cancel the current meeting invitation. This is the complete meeting invitation process.

On acceptance of the meeting invitation, the video meeting will start using the Jitsi Meet. To implement video meetings or conferences lots of groundwork is needed, to simplify this process we will use a pre-built Jitsi Meet client which is a free, open-source project that provides web browsers and mobile applications with real-time communication (RTC) via simple application programming interfaces (APIs). It allows audio and video communication to work inside applications and web pages by allowing direct peer-to-peer communication, eliminating the need to install plugins or download native apps. This Jitsi Meet client provides the facilities like, switch sound devices, invite more people to the meeting, audio-only mode, toggle camera, tile view in case of more people. The best part is you can also start chat conversion during video meetings.
 
 
 
 Libraries Used:
1. Scalable Size Unit:
        a) sdp: https://github.com/intuit/sdp
        b) ssp: https://github.com/intuit/ssp
2. Recycler View: https://developer.android.com/jetpack...
3. Material Design: https://material.io/develop/android/d...
4. MultiDex: https://developer.android.com/studio/...
5. Retrofit: https://square.github.io/retrofit
6. Swipe Refresh Layout: https://developer.android.com/trainin...
7. Jitsi Meet SDK https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-android-sdk

To Use JitsiMeet SDK

In your project, add the Maven repository https://github.com/jitsi/jitsi-maven-repository/raw/master/releases and the dependency org.jitsi.react:jitsi-meet-sdk into your build.gradle files.

The repository typically goes into the build.gradle file in the root of your project:

allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url "https://github.com/jitsi/jitsi-maven-repository/raw/master/releases"
        }
    }
}

Dependency definitions belong in the individual module build.gradle files:

dependencies {
    // (other dependencies)
    implementation ('org.jitsi.react:jitsi-meet-sdk:2.+') { transitive = true }
}


Sample Activity Example to start a video call meeting

package org.jitsi.example;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.ReactActivityLifecycleCallbacks;

// Example
//
public class MainActivity extends FragmentActivity implements JitsiMeetActivityInterface {
    private JitsiMeetView view;

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {
        JitsiMeetActivityDelegate.onActivityResult(
                this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        JitsiMeetActivityDelegate.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new JitsiMeetView(this);
        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
            .setRoom("https://meet.jit.si/test123")
            .build();
        view.join(options);

        setContentView(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        view.dispose();
        view = null;

        JitsiMeetActivityDelegate.onHostDestroy(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            final String[] permissions,
            final int[] grantResults) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();

        JitsiMeetActivityDelegate.onHostResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        JitsiMeetActivityDelegate.onHostPause(this);
    }
}

