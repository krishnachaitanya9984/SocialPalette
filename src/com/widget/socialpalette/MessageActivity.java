package com.widget.socialpalette;


import java.util.ArrayList;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MessageActivity  extends Activity{

	private static final String TAG = "MessageActivity";
	private static final int NONE = 0;	
	
	Twitter mTwitter;
	AlertDialog mAlert;
	private FacebookUtil mFacebookUtil;	
	private OAuthProvider mProvider;
	private CommonsHttpOAuthConsumer mConsumer;
	private final Handler mHandler = new Handler();	
	
	
	final Runnable mTwitterMaxCharactersNotification = new Runnable() {
        public void run() {
        	showToast(R.string.twittermaxcharacter);        	
        }
	 };
	 
	 final Runnable mTweetSuccessNotification = new Runnable() {
        public void run() {
        	showToast(R.string.twittersuccess);        	
        }
	 };
	
	 final Runnable mUpdateFacebookNotification = new Runnable() {
        public void run() {
        	showToast(R.string.facebooksuccess);
        }
	 };
	 
	 final Runnable mLogoutNotification = new Runnable() {
        public void run() {
        	showToast(R.string.logout);
        }
	 };
	 
	 final Runnable mTwitterLogoutNotification = new Runnable() {
	        public void run() {
	        	showToast(R.string.signout);
	        }
		 };
		 
	 final Runnable mAlreadyLogoutNotification = new Runnable() {
        public void run() {
        	showToast(R.string.alreadylogout);        	
        }
	 };
	 
 
	    
	 final Runnable mErrorNotification = new Runnable() {
        public void run() {
        	showToast(R.string.error);        	
        }
	 };
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
    	System.setProperty("http.keepAlive", "false");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);        
        Log.d(TAG,"onCreate");
        try {
        	 ImageView facebookView = (ImageView) findViewById(R.id.fbImage);        
             ImageView twitterView = (ImageView) findViewById(R.id.twitterImage);
             ImageView speakNow = (ImageView) findViewById(R.id.speakBtn);
             
             facebookView.setOnClickListener(facebookViewListener);        
             twitterView.setOnClickListener(twitterViewListener); 
             speakNow.setOnClickListener(speakNowListener);
             
             this.mFacebookUtil = new FacebookUtil(Constants.FACEBOOK_APPID, this, 
             		getApplicationContext(), new String[] {Constants.FACEBOOK_PERMISSION});
             mTwitter = new TwitterFactory().getInstance();
     		 mTwitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
             
             checkTwitterLogin();
             getConsumerProvider();        
             CookieSyncManager.createInstance(getApplicationContext());   
             
        } catch (Exception e) {
        	Log.e(TAG,"exception is:"+e.getMessage());
        }
       
        
        
    }
    
    private void showToast(int msg) {
    	Toast toast = Toast.makeText(getBaseContext(), msg , Toast.LENGTH_LONG);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();
    }
    
    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, Constants.SPEECH_RECOGNITION);
        startActivityForResult(intent, Constants.VOICE_RECOGNITION_REQUEST_CODE);
    }
    
    private OnClickListener speakNowListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			try{
				startVoiceRecognitionActivity();
			} catch (Exception e) {
				Log.e(TAG,"exception is:"+ e.getMessage());
			}
							
		}    	
    };
    
    private OnClickListener facebookViewListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			try{
				EditText messageBox = (EditText) findViewById(R.id.messageBox); 
				String myMessage = messageBox.getText().toString();
				
				if(myMessage.length() == 0) {
					mHandler.post(mErrorNotification);
				} else {
					postMessage();
				}
			} catch (Exception e) {
				Log.e(TAG,"exception is:"+ e.getMessage());
			}
							
		}    	
    };
    
    private OnClickListener twitterViewListener = new OnClickListener() {
		@Override
		public void onClick(View v) {	
			
			try{
				EditText messageBox = (EditText) findViewById(R.id.messageBox); 
				String myMessage = messageBox.getText().toString();
				
				if(myMessage.length() == 0) {
					mHandler.post(mErrorNotification);
				} else if(myMessage.length() > Constants.MSG_LENGTH) {
					mHandler.post(mTwitterMaxCharactersNotification);
				} else {
					checkTwitterAccessToken();
				}
			} catch (Exception e) {
				Log.e(TAG,"exception is:"+ e.getMessage());
			}
					
		}    	
    };
    
        
    @Override
    protected void onPause() {
      super.onPause();
    }

    @Override
    protected void onResume() {
      super.onResume();     
    }    
   
    //Twitter Apis
    private void twitterOAuth() {
		try {
			mConsumer = new CommonsHttpOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
			mProvider = new DefaultOAuthProvider(Constants.REQUEST_TOKEN_URI , Constants.ACCESS_TOKEN_URI ,
											Constants.AUTHORIZE_URI);
			String authUrl = mProvider.retrieveRequestToken(mConsumer, Constants.CALLBACKURL);
			setConsumerProvider();
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
		} catch (Exception e) {
			Log.e(TAG,"Exception is"+ e.getMessage());
		}
	}
    
    private void checkTwitterLogin() {
		AccessToken a = getAccessToken();
		if (a==null) return;

		mTwitter = new TwitterFactory().getInstance();
		mTwitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
		mTwitter.setOAuthAccessToken(a);
		((MyApplication)getApplication()).setTwitter(mTwitter);
	}    
 
  
    @Override
    protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent!=null ){
			Uri uri = intent.getData();
			if (uri != null && uri.toString().startsWith(Constants.CALLBACKURL)) {
				String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
				try {						
					mProvider.retrieveAccessToken(mConsumer, verifier);
					tweet();
				} catch (Exception e) {
					Log.e(TAG,"Exception is"+ e.getMessage());
				}
			}
		}
	}
    
    private void checkTwitterAccessToken() {
		AccessToken a = getAccessToken();
		if (a==null) {
			Log.d(TAG,"now login");
			twitterOAuth();	
		} else {
			Log.d(TAG,"now tweet");
			tweet();
		}
	}
    
    private AccessToken getAccessToken() {
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
		String token = settings.getString(Constants.ACCESSTOKEN_TOKEN, "");
		String tokenSecret = settings.getString(Constants.ACCESSTOKEN_SECRET, "");
		if (token!=null && tokenSecret!=null && !"".equals(tokenSecret) && !"".equals(token)){
			return new AccessToken(token, tokenSecret);
		}
		return null;
	}
    
    private void storeAccessToken(AccessToken a) {
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Constants.ACCESSTOKEN_TOKEN, a.getToken());
		editor.putString(Constants.ACCESSTOKEN_SECRET, a.getTokenSecret());
		editor.commit();
	}
    
    private void clearAccessToken() {
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Constants.ACCESSTOKEN_TOKEN, "");
		editor.putString(Constants.ACCESSTOKEN_SECRET, "");
		editor.commit();
	}   
    
    private void tweet() {    	
    	try {
    		if(mConsumer != null) {
    			Log.d(TAG,"tweet");
    			AccessToken a = new AccessToken(mConsumer.getToken(), mConsumer.getTokenSecret());
        		storeAccessToken(a);

        		mTwitter = new TwitterFactory().getInstance();
        		mTwitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
        		mTwitter.setOAuthAccessToken(a);
        		((MyApplication)getApplication()).setTwitter(mTwitter);

        		EditText messageBox = (EditText) findViewById(R.id.messageBox); 
        		String myMessage = messageBox.getText().toString();	
        		mTwitter.updateStatus(myMessage);
        		mHandler.post(mTweetSuccessNotification);
    		} else {
    			// when mConsumer object is null,it means u have lost all the data about the user
    			//You have also lost credentials too.Now you have to login into the account.
    			//this happens only when u restart the phone...
    			twitterOAuth();
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    		Log.d(TAG,"myexception is"+e.getMessage());
    		
    		if(e.getMessage().toString().contains(Constants.DUPLICATE)) {
    			Toast.makeText(this, R.string.duplicate, Toast.LENGTH_LONG).show();
    		}
    	}    	
    }
    
    private void twitterLogout() {  
    	
    	AccessToken a = getAccessToken();
		if (a==null) {
			mHandler.post(mAlreadyLogoutNotification);
		} else {
			clearCookies(getApplicationContext());
	    	clearAccessToken();    	
	    	twitterOAuth();
			mHandler.post(mTwitterLogoutNotification);
		}
    	
    }
    
    private static void clearCookies(Context context) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    
    private void getConsumerProvider() {
		OAuthProvider prov = ((MyApplication)getApplication()).getProvider();
		if (prov!=null){
			mProvider = prov;
		}
		CommonsHttpOAuthConsumer cons = ((MyApplication)getApplication()).getConsumer();
		if (cons!=null){
			mConsumer = cons;
		}
	}
    
    private void setConsumerProvider() {
		if (mProvider!=null){
			((MyApplication)getApplication()).setProvider(mProvider);
		}
		if (mConsumer!=null){
			((MyApplication)getApplication()).setConsumer(mConsumer);
		}
	}
    
    
    //Facebook Api's
	private String getFacebookMsg() {		
		 String myMessage;		
		 EditText messageBox = (EditText) findViewById(R.id.messageBox);		 
		 myMessage = messageBox.getText().toString();		 
		 return myMessage;
	}	
	
	private void postMessage() {		
		if (mFacebookUtil.getFacebook().isSessionValid()) {
			postMessageInThread();
		} else {
			SessionEventListener.AuthorizeListener listener = new SessionEventListener.AuthorizeListener() {
				
				@Override
				public void onAuthSucceed() {
					Log.d(TAG,"onAuthSucceed");
					postMessageInThread();
				}
				
				@Override
				public void onAuthFail(String error) {
					Log.d(TAG,"onAuthFail");
				}
			};
			SessionEventListener.addAuthListener(listener);
			mFacebookUtil.login();
		}
	}

	private void postMessageInThread() {
		Thread t = new Thread() {
			public void run() {		    	
		    	try {		    		
		    		 String myMessage = getFacebookMsg();
		    		 if(null != myMessage) {
		    			 mFacebookUtil.postMessageOnWall(myMessage);
		    			 mHandler.post(mUpdateFacebookNotification);		    			 
		    		 } else {
		    			 mHandler.post(mErrorNotification);
		    		 }					
				} catch (Exception ex) {
					Log.e(TAG, "Error sending msg"+ex.getMessage());
				}
		    }
		};
		t.start();
	}   
	
	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
	  menu.addSubMenu(NONE, 0, NONE, R.string.logout_menu);
	  return true;
	 }
	
	@Override
	 public boolean onOptionsItemSelected(MenuItem item) {
	  switch(item.getItemId()) {
	  case 0:
		  launchLogoutDialog();
		  default:
			  break;
	  }
	  return true;
	 }
	
	private void facebookLogout () {
		try{
			if(mFacebookUtil.checkSessionAndLogout()) {
				mHandler.post(mLogoutNotification);
			} else {
				mHandler.post(mAlreadyLogoutNotification);
			}
		} catch (Exception e) {
			Log.e(TAG,"exception is:"+ e.getMessage());
		}	
	}
	
	
	private void launchLogoutDialog() {
		 ArrayList<String> items; 
		 try {
			 items = new ArrayList<String>();
			 items.clear();
			 items.add(getResources().getString(R.string.facebook));
			 items.add(getResources().getString(R.string.twitter));
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 //builder.setTitle(R.string.logout_menu);
			 builder.setAdapter(new SocialListAdapter(this,items ),  new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int item) {
		           switch(item) {
		           case 0:
		        	   facebookLogout();
		        	   break;
		           case 1:
		        	   twitterLogout();
		        	   break;
		        	   default:
		        		   break;
		           }
		       }
			 });
		   mAlert = builder.create();
		   mAlert.show(); 
		 } catch (Exception e) {
			 Log.e(TAG,"exception is: "+e.getMessage());
		 }
		   
	}
	
	        
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (requestCode == Constants.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            for(String match: matches) {
            	Log.d(TAG,"match is"+ match);
            	EditText messageBox = (EditText) findViewById(R.id.messageBox); 
				String myMessage = messageBox.getText().toString();
            	if(match.contains(Constants.FACEBOOK)) {    				
    				if(myMessage.length() == 0) {
    					mHandler.post(mErrorNotification);
    				} else {
    					postMessage();
    				}
            	} else {            		    				
    				if(myMessage.length() == 0) {
    					mHandler.post(mErrorNotification);
    				} else if(myMessage.length() > Constants.MSG_LENGTH) {
    					mHandler.post(mTwitterMaxCharactersNotification);
    				} else {
    					checkTwitterAccessToken();
    				}
            	}
            	break;
            }
        }
      super.onActivityResult(requestCode, resultCode, data);        
    }    
    
}
