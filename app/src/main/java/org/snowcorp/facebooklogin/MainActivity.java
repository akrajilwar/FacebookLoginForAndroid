package org.snowcorp.facebooklogin;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Akshay Raj on 27-03-2017.
 * akshay@snowcorp.org
 * www.snowcorp.org
 */

public class MainActivity extends AppCompatActivity {
    private static String KEY_UID = "uid";
    private static String KEY_FIRSTNAME = "fname";
    private static String KEY_LASTNAME = "lname";
    private static String KEY_EMAIL = "email";
    private static String KEY_PROFILE_PIC = "profile_pic";

    private TextView userName, userEmail;
    private ImageView userPic;

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "org.snowcorp.facebooklogin",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        userName = (TextView) findViewById(R.id.name);
        userEmail = (TextView) findViewById(R.id.email);
        userPic = (ImageView) findViewById(R.id.user_pic);

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile", "email", "user_friends");

        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d("Login Results", loginResult.toString());
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                Log.e("response: ", response + "");
                                try {
                                    KEY_UID = object.getString("id");
                                    KEY_EMAIL = object.getString("email");
                                    KEY_FIRSTNAME = object.getString("first_name");
                                    KEY_LASTNAME = object.getString("last_name");
                                    KEY_PROFILE_PIC = "https://graph.facebook.com/" + KEY_UID + "/picture?type=large";

                                    userName.setText(KEY_FIRSTNAME + " " + KEY_LASTNAME);
                                    userEmail.setText(KEY_EMAIL);

                                    Glide.with(getApplicationContext())
                                            .load(KEY_PROFILE_PIC)
                                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                            .centerCrop()
                                            .transform(new CircleTransform(getApplicationContext()))
                                            .into(userPic);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
