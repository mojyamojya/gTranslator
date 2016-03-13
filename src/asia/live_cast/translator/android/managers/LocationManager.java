package asia.live_cast.translator.android.managers;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.LocationListener;
import android.os.Handler;
import android.widget.Toast;
import asia.live_cast.translator.android.R;
import asia.live_cast.translator.android.gTranslator;

public class LocationManager {
	private Context context;
	private LocationListener glistener;
    private LocationListener nlistener;
    private android.location.LocationManager manager;
    private ProgressDialog dialog;
    private Timer timer;

    public LocationManager(Context context, LocationListener glistener, LocationListener nlistener) {
		this.glistener = glistener;
		this.nlistener = nlistener;
    	this.context = context;
    }
    
	public boolean requestLocationUpdates() {
		manager = (android.location.LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		String provider = manager.getBestProvider(criteria, true);
		if (provider == null) {
			return false;
		}
		manager.getLastKnownLocation(provider);
		manager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0, 0, glistener);
		manager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0, 0, nlistener);

		timer = new Timer();
		timer.schedule(new ListenerRemover(), 15000);
		
		showProgressDialog(R.string.tweet_location_progress_message);
		
		return true;
	}

    private void showProgressDialog(int id) {
    	if (dialog == null) {
    		dialog = new ProgressDialog(context);
    		dialog.setIndeterminate(true);
    	}
		dialog.setMessage(context.getText(id));
    	dialog.show();
    }
	
	public void removeAllUpdates() {
		dialog.dismiss();
		
		manager.removeUpdates(glistener);
		manager.removeUpdates(nlistener);
		
		timer.cancel();
	}
	
	private class ListenerRemover extends TimerTask {
		Handler handler = new Handler();

		@Override
		public void run() {
			handler.post(new Runnable() {

				public void run() {
					removeAllUpdates();
					
					Toast.makeText(
							context,
							context.getText(R.string.location_status_error_messae), 
							Toast.LENGTH_LONG).show();
					
					if (context instanceof gTranslator) {
						((gTranslator)context).setAddress(context.getString(R.string.location_failed_message));
					}
				}
				
			});
			
		}
		
	}
}
