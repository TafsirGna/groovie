package com.myapp.groovie;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.myapp.groovie.classes.database.AjouterLieu;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.Lieu;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.GPSUpdateReceiver;
import com.myapp.groovie.classes.objects.Groovieparams;

@SuppressLint("NewApi")
public class MapActivity extends Activity {

	//private MapController mapController;
	private GoogleMap map;
	private LatLng lieu=new LatLng(6.403, 2.341);
	private LocationManager locationManager;
	private LieuDataSource LieuDS;
	private Utilisateur phone_user;
	private UtilisateurDataSource UtilisateurDS;
	private TelephonyManager phoneManager;
	private InfosdulieuDataSource InfosduLieuDS;
	private int id_activite_parent;
	private int draggable;
	private Marker markerPlace;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		//je récupère les variables sus-cités
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		LieuDS= new LieuDataSource(this);
		LieuDS.open();
		InfosduLieuDS= new InfosdulieuDataSource(this);
		InfosduLieuDS.open();
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		phone_user=get_user();

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_layout_mapView_carte))
				.getMap();
		//j'active le service de localisation afin de recevoir les notifications de localisation
		locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Intent intent_location_receiver= new Intent(this, GPSUpdateReceiver.class);
		PendingIntent location_pending=PendingIntent.getBroadcast(this, 0, intent_location_receiver, PendingIntent.FLAG_UPDATE_CURRENT);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 150,location_pending);


		//je récupère l'intent de l'activité appelante
		Intent intent_appelant= getIntent();
		id_activite_parent=intent_appelant.getIntExtra("identifiant", 0);

		//En fonction de la nature de l'activité appelante, on a :
		//Si l'activité appelante est AjouterlieuActivity
		if (id_activite_parent==1)
		{
			draggable=intent_appelant.getIntExtra("draggable", 2);
			Location mylocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (mylocation!=null)
			{
				lieu= new LatLng(mylocation.getLatitude(), mylocation.getLongitude());
			}
			markerPlace = map.addMarker(new MarkerOptions().position(lieu)
					.title("Moi")
					.snippet("Vous êtes ici"));
			if (draggable==1)
				markerPlace.setDraggable(true);
		}
		//Si l'activité appelante est  AcceuilActivity
		if (id_activite_parent==2)
		{
			int identifiant_lieu=intent_appelant.getIntExtra("identifiant_lieu", 0);
			if (identifiant_lieu!=0)
			{
				Lieu current_lieu=LieuDS.get_Lieu(identifiant_lieu);
				markerPlace = map.addMarker(new MarkerOptions().position(lieu)
						.title(current_lieu.get_titre())
						.snippet(String.valueOf("au prix actuel de "+((InfosduLieuDS.get_entree_reference(current_lieu.get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(current_lieu.get_idLieu()).get_prixmodifie())+
								" "+Groovieparams.monnaie+" modifié par "+((InfosduLieuDS.get_entree_reference(current_lieu.get_idLieu())==null) ? "?" : UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(current_lieu.get_idLieu()).get_modified_by()).getPseudo()))));
			}
		}
		//Si l'activité appelante est meslieuxActivity
		if (id_activite_parent==3)
		{
			Cursor c=LieuDS.get_places_of(phone_user.getIdUtilisateur());
			c.moveToFirst();
			while (!c.isAfterLast()) {
				Lieu mon_lieu= new Lieu(c.getInt(0), c.getInt(6), c.getInt(1), c.getString(2), c.getDouble(4), c.getDouble(3), c.getString(5),c.getBlob(7));
				AjouterLieu entree= new AjouterLieu(c.getInt(6), c.getInt(0), c.getString(7));

				//je materialise le marker pour chaque lieu de l'utilisateur
				markerPlace =map.addMarker(new MarkerOptions().position(new LatLng(mon_lieu.get_latitude(), mon_lieu.get_longitude()))
						.title(mon_lieu.get_titre())
						.snippet(String.valueOf("au prix actuel de "+((InfosduLieuDS.get_entree_reference(mon_lieu.get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(mon_lieu.get_idLieu()).get_prixmodifie())+
								" "+Groovieparams.monnaie+" modifié par "+((InfosduLieuDS.get_entree_reference(mon_lieu.get_idLieu())==null) ? "?" : UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(mon_lieu.get_idLieu()).get_modified_by()).getPseudo()))));

				c.moveToNext();
			}
		}

		// Move the camera instantly to hamburg with a zoom of 15.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(lieu, 5));

		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

		/*
		Marker kiel = map.addMarker(new MarkerOptions()
		.position(KIEL)
		.title("Kiel")
		.snippet("Kiel is cool")
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_launcher)));
		 */
		//MapView mapView= (MapView) findViewById(id.map_layout_mapView_carte);
		//mapView.setBuiltInZoomControls(true);
		//mapController= mapView.getController();
		//mapController.setZoom(17);
		//mapView.setSatellite(true);
	}

	private Utilisateur get_user()
	{
		List<Utilisateur> liste_users=UtilisateurDS.getAllUtilisateurs();
		int liste_user_size=liste_users.size();
		String id_device=phoneManager.getDeviceId();
		for (int i=0;i<liste_user_size;i++)
		{
			if (id_device.equals(liste_users.get(i).getIdDevice()))
			{
				return liste_users.get(i);
			}
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.map_menu_action_ok:
			if (id_activite_parent==1 && draggable==1)
			{
				Intent result = new Intent();
		        result.putExtra(AjouterLieuActivity.result_Longitude, markerPlace.getPosition().longitude);
		        result.putExtra(AjouterLieuActivity.result_Latitude, markerPlace.getPosition().latitude);
		        setResult(RESULT_OK, result);
			}
			this.finish();
			//pickImage();
			return true;
		case android.R.id.home:
			//onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 150, new LocationListener() {

				@Override
				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onProviderEnabled(String arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onProviderDisabled(String arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onLocationChanged(Location location) {
					// TODO Auto-generated method stub
				}
			});
	 */
}

