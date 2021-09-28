package com.example.imirror.videoActivity;

/* listener相關 */
public interface UsersListener {

    void initiateVideoMeeting(User user);
    void initiateAudioMeeting(User user);
    void onMultipleUsersAction(Boolean isMultipleUsersSelected);

}
