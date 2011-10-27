package com.widget.socialpalette;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.widget.socialpalette.SessionEventListener.AuthorizeListener;
import com.widget.socialpalette.SessionEventListener.LogoutListener;

public class FacebookUtil {

	private static final String TAG = "FacebookUtil";
	private Facebook facebook = null;
	private Context context;
	private String[] permissions;
	private Handler mHandler;
	private Activity activity;
	private SessionListener mSessionListener = new SessionListener();;
	
	public FacebookUtil(String appId,Activity activity,Context context,String[] permissions) {
		this.facebook = new Facebook(appId);
		
		SessionInfo.restore(facebook, context);
        SessionEventListener.addAuthListener(mSessionListener);
        SessionEventListener.addLogoutListener(mSessionListener);
        
		this.context=context;
		this.permissions=permissions;
		this.mHandler = new Handler();
		this.activity=activity;
	}
	
	public void login() {
        if (!facebook.isSessionValid()) {
        	Log.v(TAG,"login-it is invalid session");
            facebook.authorize(this.activity, this.permissions,new LoginDialogListener());
        }
    }
	
	public boolean checkSessionAndLogout() {
		if (facebook.isSessionValid()) {
			logout();
			return true;
		} else {
			return false;
		}
	}
	
	public void logout() {
        SessionEventListener.onLogoutBegin();
        AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
        asyncRunner.logout(this.context, new LogoutRequestListener());
	}
	
	public void postMessageOnWall(String msg) {
		if (facebook.isSessionValid()) {
		    Bundle parameters = new Bundle();
		    parameters.putString("message", msg);
		    try {
		    	Log.d(TAG,"post now");
		    	facebook.request("me/feed", parameters,"POST");
			} catch (IOException e) {				
				e.printStackTrace();
			}
		} else {
			login();
		}
	}	

    private static class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            SessionEventListener.onLoginSuccess();
        }

        public void onFacebookError(FacebookError error) {
            SessionEventListener.onLoginError(error.getMessage());
        }
        
        public void onError(DialogError error) {
            SessionEventListener.onLoginError(error.getMessage());
        }

        public void onCancel() {
            SessionEventListener.onLoginError("Action Canceled");
        }
    }
    
    public class LogoutRequestListener extends MyRequestListener {
        public void onComplete(String response, final Object state) {
            // callback should be run in the original thread, 
            // not the background thread
            mHandler.post(new Runnable() {
                public void run() {
                    SessionEventListener.onLogoutFinish();
                }
            });
        }
    }
    
    private class SessionListener implements AuthorizeListener, LogoutListener {
        
        public void onAuthSucceed() {
            SessionInfo.save(facebook, context);
        }

        public void onAuthFail(String error) {
        }
        
        public void onLogoutBegin() {           
        }
        
        public void onLogoutFinish() {
            SessionInfo.clear(context);
        }
    }

	public Facebook getFacebook() {
		return this.facebook;
	}
}
