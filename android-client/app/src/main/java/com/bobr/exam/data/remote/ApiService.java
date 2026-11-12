package com.bobr.exam.data.remote;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {

    @POST("api/auth/send-code")
    Call<ResponseBody> sendCode(@Body EmailRequest body);

    @POST("api/auth/check-code")
    Call<ResponseBody> checkCode(@Body VerifyRequest body);

    @POST("api/auth/verify-email")
    Call<ResponseBody> verifyEmail(@Body VerifyRequest body);

    @POST("api/auth/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetRequest body);

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("api/user/me")
    Call<UserProfile> getMyProfile(@Header("Authorization") String token);

    @POST("api/user/setup")
    Call<UserProfile> setup(@Header("Authorization") String token, @Body SetupRequest body);

    @PUT("api/user/profile")
    Call<UserProfile> updateProfile(@Header("Authorization") String token, @Body ProfileUpdateRequest body);

    @PUT("api/user/password")
    Call<ResponseBody> changePassword(@Header("Authorization") String token, @Body PasswordChangeRequest body);

    @DELETE("api/user")
    Call<ResponseBody> deleteAccount(@Header("Authorization") String token);

    @GET("api/course/my")
    Call<List<CourseUnit>> getMyCourse(@Header("Authorization") String token);

    @GET("api/leaderboard")
    Call<List<LeaderEntry>> getLeaderboard();

    @POST("api/progress/submit")
    Call<SubmitResponse> submit(@Header("Authorization") String token, @Body SubmitRequest body);

    @POST("api/ai/explain")
    Call<AiResponse> explain(@Body AiRequest body);

    class RegisterRequest {
        public String email; public String nickname; public String password; public String language;
        public RegisterRequest(String e, String n, String p, String l) { email=e; nickname=n; password=p; language=l; }
    }
    class EmailRequest {
        public String email;
        public EmailRequest(String e) { email = e; }
    }
    class VerifyRequest {
        public String email; public String code;
        public VerifyRequest(String e, String c) { email = e; code = c; }
    }
    class ResetRequest {
        public String email; public String code; public String newPassword;
        public ResetRequest(String e, String c, String p) { email = e; code = c; newPassword = p; }
    }
    class LoginRequest {
        public String email; public String password;
        public LoginRequest(String e, String p) { email=e; password=p; }
    }
    class AuthResponse { public String access_token; public String token_type; }
    class UserProfile {
        public String id; public String email; public String nickname; public String examType;
        public Integer grade; public String subjects; public String language; public String avatar;
        public int xp; public int streak; public int hearts;
    }
    class SetupRequest {
        public int grade; public List<String> subjects;
        public SetupRequest(int g, List<String> s) { grade=g; subjects=s; }
    }
    class ProfileUpdateRequest {
        public String nickname; public String email; public String avatar; public String language;
        public ProfileUpdateRequest(String n, String e, String a, String l) { nickname=n; email=e; avatar=a; language=l; }
    }
    class PasswordChangeRequest {
        public String oldPassword; public String newPassword;
        public PasswordChangeRequest(String o, String n) { oldPassword=o; newPassword=n; }
    }
    class LeaderEntry { public int rank; public String nickname; public int xp; public int streak; public String avatar; }
    class TaskDto {
        public String id; public String subject; public String examType; public int grade; public int examNumber;
        public String topic; public int difficulty; public String type; public String questionText; public String optionsJson; public String source;
    }
    class CourseUnit { public String subject; public int grade; public List<TaskDto> tasks; }
    class SubmitRequest {
        public String taskId; public String userAnswer;
        public SubmitRequest(String t, String u) { taskId=t; userAnswer=u; }
    }
    class SubmitResponse { public boolean correct; public String correctAnswer; public String explanation; public int xp; public int hearts; public int streak; }
    class AiRequest {
        public String taskId; public String examType; public String subject; public String taskText; public String userAnswer;
        public AiRequest(String id, String e, String s, String t, String u) { taskId=id; examType=e; subject=s; taskText=t; userAnswer=u; }
    }
    class AiResponse { public String explanation; public String mascot_state; public boolean mock; }
}
