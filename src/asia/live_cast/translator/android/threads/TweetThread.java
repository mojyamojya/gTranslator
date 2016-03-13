package asia.live_cast.translator.android.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.preference.PreferenceManager;
import asia.live_cast.translator.android.models.TweetModel;
import asia.live_cast.translator.android.models.XAuthModel;

public class TweetThread extends Thread {
	private final Handler handler;
	private final Runnable runnable;
	private final TweetModel tmodel;
	private final XAuthModel xmodel;
	private final HttpClient client;
	private final static String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
	private final static String STATUS_UPDATE_URL = "http://api.twitter.com/1/statuses/update.json";
	private final static String X_AUTH_MODE = "client_auth";
	private final static String OAUTH_SIGNATURE_METHOD = "HMAC-SHA1";
	private final static String OAUTH_VERSION = "1.0";
	private final static String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	public TweetThread(Handler handler, Runnable runnable, TweetModel tmodel, XAuthModel xmodel) {
		this.handler = handler;
		this.runnable = runnable;
		this.tmodel = tmodel;
		this.xmodel = xmodel;
		this.client = new DefaultHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
	}
	
	private boolean getOAuthToken() {
		try {
			HttpPost post = new HttpPost(ACCESS_TOKEN_URL);
			
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(9);
			params.add(new BasicNameValuePair("x_auth_username", tmodel.getAccount()));
			params.add(new BasicNameValuePair("x_auth_password", tmodel.getPassword()));
			params.add(new BasicNameValuePair("x_auth_mode", X_AUTH_MODE));
			params.add(new BasicNameValuePair("oauth_signature",
					generateSignature(post.getMethod(), ACCESS_TOKEN_URL, params)));
			
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			final HttpResponse response = client.execute(post);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				
				String line;
				StringBuilder builder = new StringBuilder();
				
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				stream.close();
				
				String[] tokens = builder.toString().split("&");
				
				for (String token: tokens) {
					if (xmodel.getOAuthToken().equals("") || xmodel.getOAuthTokenSecret().equals("")) {
						String[] values = token.split("=");
						
						if (values.length == 2) {
							if (values[0].equals("oauth_token")) {
								xmodel.setOAuthToken(values[1]);
							}
							else if (values[0].equals("oauth_token_secret")) {
								xmodel.setOAuthTokenSecret(values[1]);
							}
							else {
								continue;
							}
						}
					}
					else {
						break;
					}
				}
				
		    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences((Context)runnable);
		    	
		    	if (preferences.getBoolean("tweet", false)) {
					Editor editor = preferences.edit();
		    		editor.putString("oauth_token", xmodel.getOAuthToken());
		    		editor.putString("oauth_token_secret", xmodel.getOAuthTokenSecret());
		    		editor.commit();
		    	}
		    	
		    	tmodel.setAuthorized(true);
				tmodel.setSuccess(true);
			}
			else {
				// 認証に失敗した場合はID、パスワードをクリアする。
				tmodel.setAccount("");
				tmodel.setPassword("");
				tmodel.setSuccess(false);
				tmodel.setAuthorized(false);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return tmodel.isSuccess();
	}
	
	private void setStatus() {
		try {
			HttpPost post = new HttpPost(STATUS_UPDATE_URL);
			
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(8);
			params.add(new BasicNameValuePair("status", tmodel.getStatus().replaceAll("\n", " ")));
			if (!tmodel.getLat().equals("") && !tmodel.getLng().equals("")) {
				params.add(new BasicNameValuePair("lat", tmodel.getLat()));
				params.add(new BasicNameValuePair("long", tmodel.getLng()));
			}
			params.add(new BasicNameValuePair("oauth_signature",
					generateSignature(post.getMethod(), STATUS_UPDATE_URL, params)));
			
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			final HttpResponse response = client.execute(post);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				tmodel.setSuccess(true);
			}
			else {
				tmodel.setSuccess(false);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		boolean result = true;
		
		if (xmodel.getOAuthToken().equals("") || xmodel.getOAuthTokenSecret().equals("")) {
			result = getOAuthToken();
		}
		
		if (result) {
			setStatus();
		}
		
		handler.post(runnable);
	}
	
	private String generateSignature(String method, String url, ArrayList<NameValuePair> params) {
		params.add(new BasicNameValuePair("oauth_consumer_key", XAuthModel.consumer_key));
		params.add(new BasicNameValuePair("oauth_nonce", UUID.randomUUID().toString()));
		params.add(new BasicNameValuePair("oauth_signature_method", OAUTH_SIGNATURE_METHOD));
		params.add(new BasicNameValuePair("oauth_timestamp", String.valueOf((new Date()).getTime()).substring(0, 10)));
		params.add(new BasicNameValuePair("oauth_version", OAUTH_VERSION));
		if (!xmodel.getOAuthToken().equals("")) {
			params.add(new BasicNameValuePair("oauth_token", xmodel.getOAuthToken()));
		}
		
		List<String> list = UrlVariablesToArray(params);
		String sigBase = URLEncoder.encode(method) + "&" + URLEncoder.encode(url) + "&" + URLEncoder.encode(join(list, "&"));
		String sigKeyBase = URLEncoder.encode(XAuthModel.consumer_secret) + "&";
		if (!xmodel.getOAuthTokenSecret().equals("")) {
			sigKeyBase += URLEncoder.encode(xmodel.getOAuthTokenSecret());
		}
		
		byte[] signature = null;
		
		try {
			SecretKeySpec key = new SecretKeySpec(sigKeyBase.getBytes(), HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(key);
			signature = mac.doFinal(sigBase.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
		return BASE64Encoder.encode(signature);
	}
	
	private List<String> UrlVariablesToArray(ArrayList<NameValuePair> params) {
		int len = params.size();
		
		List<String> arr = new ArrayList<String>();
		
		for (int i = 0; i < len; i++) {
			arr.add(params.get(i).getName() + "=" + SignatureEncode.encode(params.get(i).getValue()));
		}
		
		Collections.sort(arr);
		
		return arr;
	}
	
	private String join(List<String> list, String separator) {
		StringBuilder builder = new StringBuilder();
		
		int len = list.size();
		
		if (len > 1) {
			builder.append(list.get(0));
			
			for (int i = 1; i < len; i++) {
				builder.append(separator + list.get(i));
			}
		}
		
		return builder.toString();
	}
	
	private static class BASE64Encoder {
	    private static final char last2byte = (char) Integer.parseInt("00000011", 2);
	    private static final char last4byte = (char) Integer.parseInt("00001111", 2);
	    private static final char last6byte = (char) Integer.parseInt("00111111", 2);
	    private static final char lead6byte = (char) Integer.parseInt("11111100", 2);
	    private static final char lead4byte = (char) Integer.parseInt("11110000", 2);
	    private static final char lead2byte = (char) Integer.parseInt("11000000", 2);
	    private static final char[] encodeTable = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

	    private BASE64Encoder() {
	    }

	    public static String encode(byte[] from) {
	        StringBuffer to = new StringBuffer((int) (from.length * 1.34) + 3);
	        int num = 0;
	        char currentByte = 0;
	        for (int i = 0; i < from.length; i++) {
	            num = num % 8;
	            while (num < 8) {
	                switch (num) {
	                    case 0:
	                        currentByte = (char) (from[i] & lead6byte);
	                        currentByte = (char) (currentByte >>> 2);
	                        break;
	                    case 2:
	                        currentByte = (char) (from[i] & last6byte);
	                        break;
	                    case 4:
	                        currentByte = (char) (from[i] & last4byte);
	                        currentByte = (char) (currentByte << 2);
	                        if ((i + 1) < from.length) {
	                            currentByte |= (from[i + 1] & lead2byte) >>> 6;
	                        }
	                        break;
	                    case 6:
	                        currentByte = (char) (from[i] & last2byte);
	                        currentByte = (char) (currentByte << 4);
	                        if ((i + 1) < from.length) {
	                            currentByte |= (from[i + 1] & lead4byte) >>> 4;
	                        }
	                        break;
	                }
	                to.append(encodeTable[currentByte]);
	                num += 6;
	            }
	        }
	        if (to.length() % 4 != 0) {
	            for (int i = 4 - to.length() % 4; i > 0; i--) {
	                to.append("=");
	            }
	        }
	        return to.toString();
	    }
	}
	
	private static class SignatureEncode {
		private static final String UNRESERVEDCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~";

		public static final String encode(String s) {
	        byte[] bytes = null;
	        
			try {
				bytes = s.getBytes(HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
	        StringBuffer builder = new StringBuffer();
	        for (byte b: bytes){
	        	char c = (char) b;
	        	if (UNRESERVEDCHARS.indexOf(String.valueOf(c)) >= 0) {
	        		builder.append(String.valueOf(c));
	        	} else {
	        		builder.append("%" +
	        				String.valueOf(Integer.toHexString(b > 0 ? b : b + 256)).toUpperCase());
	        	}
	        }
	        return builder.toString();
		}
	}
}
