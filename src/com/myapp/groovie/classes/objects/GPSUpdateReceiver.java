package com.myapp.groovie.classes.objects;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

public class GPSUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		 Location location = (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
	}

}
