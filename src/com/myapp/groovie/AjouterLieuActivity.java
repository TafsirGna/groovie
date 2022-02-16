package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.AjouterLieu;
import com.myapp.groovie.classes.database.AjouterLieuDataSource;
import com.myapp.groovie.classes.database.Departement;
import com.myapp.groovie.classes.database.DepartementDataSource;
import com.myapp.groovie.classes.database.Infosdulieu;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.Lieu;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;

@SuppressLint("NewApi")
public class AjouterLieuActivity extends Activity {

	private Spinner Spinner_localisation=null;
	private EditText editText_indication;
	private EditText editText_prix;
	private EditText editText_coordonnees_lieu;
	private LocationManager locationManager;
	private String StringIndication;
	private String StringPrix;
	private Spinner Spinner_departement=null;
	private String fileAjouterLieu=Groovieparams.DBurl+"ajouter_lieu.php";
	private int idDepartement;
	private static final int TAKE_PICTURE=1;
	private Uri imageUri;
	private Bitmap bitmap;
	private RelativeLayout image_du_lieu;
	private DepartementDataSource DepartementDS;
	private List<Departement> liste_departements = new ArrayList<Departement>();
	private List<String> liste_libelle_departement=new ArrayList<String>();
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private BitmapDrawable bitmapdrawable=null;
	private UtilisateurDataSource UtilisateurDS;
	private TelephonyManager phoneManager;
	private Utilisateur phone_user;
	private LieuDataSource LieuDS;
	private AjouterLieuDataSource AjouterLieuDS;
	private String longitudeValue="0";
	private String latitudeValue="0";
	private int position_user_departement=0;
	private SensorManager sensorManager;
	private Sensor mAccelerometre;
	private SensorEventListener acceleroEventListener;
	//l'attribut qui connait l'orientation de l'appareil
	private Display mDisplay;
	private boolean paysage=false;
	private boolean paysage2;
	private SharedPreferences groovie_preferences;
	private boolean paysage_picture_preference;
	private static final int MAP_REQUEST_CODE=2;
	public final static String result_Longitude="com.myapp.groovie.AjouterLieuActivity.longitude";
	public final static String result_Latitude="com.myapp.groovie.AjouterLieuActivity.latitude";
	private String dateServeur=null;
	private InfosdulieuDataSource InfosduLieuDS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//je rend l'image derriere l'action bar visible a travers celle-ci
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		setContentView(R.layout.activity_ajouter_lieu);

		//Récupération des éléments du layout dans des variables correspondantes
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		//j'initialise le manager de capteur et de l'accelerometre
		groovie_preferences= PreferenceManager.getDefaultSharedPreferences(this);
		paysage_picture_preference=groovie_preferences.getBoolean("paysage_picture_key", false);
		sensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometre= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mDisplay= ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		image_du_lieu=(RelativeLayout) findViewById(id.ajouter_lieu_layout_illustration_location_lieu);
		editText_indication=(EditText) findViewById(R.id.TitreLieu);
		editText_prix=(EditText) findViewById(R.id.ajouter_lieu_layout_editText_prix_lieu);

		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();

		//j'initialise les dataSources 
		UtilisateurDS= new UtilisateurDataSource(AjouterLieuActivity.this);
		UtilisateurDS.open();
		LieuDS= new LieuDataSource(this);
		LieuDS.open();
		InfosduLieuDS= new InfosdulieuDataSource(this);
		InfosduLieuDS.open();
		AjouterLieuDS= new AjouterLieuDataSource(this);
		AjouterLieuDS.open();
		DepartementDS= new DepartementDataSource(AjouterLieuActivity.this);
		DepartementDS.open();

		phone_user=get_user();
		
		//gestion de la photo du lieu en fonction de la configuration
		SavedInstanceData data = (SavedInstanceData)	getLastNonConfigurationInstance();
		if(data != null)
		{
			image_du_lieu.setBackgroundDrawable(new BitmapDrawable(getResources(), data.get_bmpDraw()));
			bitmap=data.get_bmpDraw();
		}

		//je récupère la racine principal
		editText_coordonnees_lieu=(EditText) findViewById(R.id.ajouter_lieu_layout_editText_coordonnees);
		locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);	
		Spinner_departement = (Spinner) findViewById(R.id.ajouter_lieu_layout_spinner_departement_lieu);

		//A la création de l'activité, je remplis l'editText des coordonnées actuelles de l'utilisateur
		Location mylocation=null; String nameProvider=null;
		//ArrayList<LocationProvider> providers = new	ArrayList<LocationProvider>();
		List<String> names = locationManager.getProviders(true);
		for(String name : names){
			//providers.add(locationManager.getProvider(name));
			mylocation=locationManager.getLastKnownLocation(name);
			nameProvider=name;
			if (mylocation!=null) break;
		}
		final String tmpProvider=nameProvider;

		if (mylocation!=null){
			editText_coordonnees_lieu.setText(mylocation.getLongitude()+" ; "+mylocation.getLatitude());
			longitudeValue=String.valueOf(mylocation.getLongitude());
			latitudeValue=String.valueOf(mylocation.getLatitude());
		}
		else
			Toast.makeText(AjouterLieuActivity.this, "Le gps n'est pas activé! Veuillez réessayer !", Toast.LENGTH_LONG).show();

		//lorsque l'utilisateur clique sur l'editText des coordonnées, on lui indique :
		editText_coordonnees_lieu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(AjouterLieuActivity.this, "Le champ Coordonnées est en lecture seule!", Toast.LENGTH_SHORT).show();
			}
		});

		//je rend l'actionbar transparent
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#64000000")));

		//affichage des départements dans le spinner
		afficher_departement();

		//je présélectionne le département de l'utilisateur
		Spinner_departement.setSelection(position_user_departement);

		//je mets en forme le layout du spinner de localisation

		Spinner_localisation=(Spinner) findViewById(id.ajouter_lieu_layout_spinner_choix_localisation);
		List<String> liste= new ArrayList<String>();
		liste.add("Ma position actuelle");
		liste.add("Une autre position");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, liste);
		//Le layout par défaut est android.R.layout.simple_spinner_dropdown_item

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner_localisation.setAdapter(adapter);

		//j'ajoute un listener lorsqu'on effectue un choix du mode de localisation
		Spinner_localisation.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				if (position==0)
				{

					Location mylocation=locationManager.getLastKnownLocation(tmpProvider);
					if (mylocation!=null){
						editText_coordonnees_lieu.setText(mylocation.getLongitude()+" ; "+mylocation.getLatitude());
						longitudeValue=String.valueOf(mylocation.getLongitude());
						latitudeValue=String.valueOf(mylocation.getLatitude());
					}
					else
						Toast.makeText(AjouterLieuActivity.this, "Le gps n'est pas activé! Veuillez réessayer !", Toast.LENGTH_LONG).show();

				}
				if (position==1)
				{
					Intent map_intent= new Intent(AjouterLieuActivity.this, MapActivity.class);
					map_intent.putExtra("identifiant", 1);
					map_intent.putExtra("draggable", 1);
					startActivityForResult(map_intent, MAP_REQUEST_CODE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		image_du_lieu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final Dialog box=new Dialog(AjouterLieuActivity.this);
				ListView dialogbox_listview=new ListView(AjouterLieuActivity.this);

				ArrayAdapter<String> adapter= new ArrayAdapter<String>(AjouterLieuActivity.this, android.R.layout.simple_list_item_1);
				adapter.add("Visualiser sur la carte");
				dialogbox_listview.setAdapter(adapter);
				dialogbox_listview.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						// TODO Auto-generated method stub
						if (position==0)
						{
							Intent map_intent= new Intent(AjouterLieuActivity.this, MapActivity.class);
							map_intent.putExtra("identifiant", 1);
							map_intent.putExtra("draggable", 0);
							startActivity(map_intent);
						}
						box.dismiss();
					}
				});
				box.setTitle("Actions");
				box.setContentView(dialogbox_listview);
				box.show();
			}
		});


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

		Spinner_departement.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				//je récupère le numero du departement sélectionné
				idDepartement=liste_departements.get(position).get_idDepartement();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		if (paysage_picture_preference==true)
		{
			//Initialisation de SensorEventListener sur l'accelerometre
			acceleroEventListener=new SensorEventListener() {

				@SuppressWarnings("deprecation")
				@Override
				public void onSensorChanged(SensorEvent event) {
					// TODO Auto-generated method stub
					//je réagis en fonction de l'orientation du terminal en fonction des trois états , landscape, landscape inversé, portrait et portrait inversé
					switch(mDisplay.getOrientation())
					{
					case Surface.ROTATION_0:
						//Toast.makeText(AjouterLieuActivity.this,"0" , Toast.LENGTH_LONG).show();
						paysage2=false;
						break;

					case Surface.ROTATION_90:
					{
						//Toast.makeText(AjouterLieuActivity.this,"90" , Toast.LENGTH_LONG).show();
						paysage2=true;
						break;
					}

					case Surface.ROTATION_180:
						//Toast.makeText(AjouterLieuActivity.this,"180" , Toast.LENGTH_LONG).show();
						paysage2=false;
						break;

					case Surface.ROTATION_270:
					{
						//Toast.makeText(AjouterLieuActivity.this,"270" , Toast.LENGTH_LONG).show();
						paysage2=true;
						break;
					}
					}
					if (paysage!=paysage2)
					{
						paysage=paysage2;
						if (paysage==true)
						{
							//iniatialisation du timer pour demander à l'utilisateur s'il veut prendre une phote au bout de 15seconds
							takePhoto();
						}
					}
				}

				@Override
				public void onAccuracyChanged(Sensor arg0, int arg1) {
					// TODO Auto-generated method stub
				}
			};
		}
	}

	private void valider_donnees()
	{
		StringIndication=editText_indication.getText().toString();
		StringPrix= editText_prix.getText().toString();

		if (StringIndication.equals(""))
		{
			Toast.makeText(AjouterLieuActivity.this, "Le champ Indication est vide! Veuillez le remplir!", Toast.LENGTH_SHORT).show();
			editText_indication.requestFocus();
			return;
		}
		if (editText_coordonnees_lieu.getText().toString().equals(""))
		{
			Toast.makeText(AjouterLieuActivity.this, "Le champ Localisation est vide! Veuillez le remplir!", Toast.LENGTH_SHORT).show();
			Spinner_localisation.requestFocus();
			return;
		}
		if (StringPrix.equals("") || StringPrix.equals("0") || StringPrix.equals("00") || StringPrix.equals("00") || StringPrix.equals("000"))
		{
			Toast.makeText(AjouterLieuActivity.this, "Le champ Prix est vide! Veuillez le remplir!", Toast.LENGTH_SHORT).show();
			editText_prix.requestFocus();
			return;
		}

		AlertDialog.Builder confirmation= new AlertDialog.Builder(AjouterLieuActivity.this);
		confirmation.setCancelable(true);
		confirmation.setMessage("Confirmez-vous les informations entrées?");
		confirmation.setTitle("Confirmation");
		confirmation.setPositiveButton("OUI", yeslistener);
		confirmation.setNegativeButton("NON", null);
		confirmation.show();
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
	private DialogInterface.OnClickListener yeslistener =new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			networkInfo= connectivityManager.getActiveNetworkInfo();
			if ((networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
			{
				if (bitmap==null)
				{
					AlertDialog.Builder confirmation= new AlertDialog.Builder(AjouterLieuActivity.this);
					confirmation.setCancelable(true);
					confirmation.setMessage("Voulez-vous ajouter une photo à ce lieu?");
					confirmation.setTitle("Confirmation");
					confirmation.setPositiveButton("OUI", yesPictureListener);
					confirmation.setNegativeButton("NON", noPictureListener);
					confirmation.show();
				}
				else
				{
					ProgressDialog progressDialog = new ProgressDialog(AjouterLieuActivity.this);
					progressDialog.setMessage("Please wait...");
					progressDialog.setIndeterminate(true);
					progressDialog.setCancelable(false);
					progressDialog.show();
					//j'enregistre les données du lieu dans la base de données
					ajouter_lieu(idDepartement,StringIndication,longitudeValue,latitudeValue,StringPrix);
				}
			}
			else
			{
				Toast.makeText(AjouterLieuActivity.this, "Aucun réseau disponible!", Toast.LENGTH_LONG).show();
			}
		}
	};
	private DialogInterface.OnClickListener yesPictureListener =new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			takePhoto();
		}
	};
	private DialogInterface.OnClickListener noPictureListener =new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			ProgressDialog progressDialog = new ProgressDialog(AjouterLieuActivity.this);
			progressDialog.setMessage("Please wait...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
			ajouter_lieu(idDepartement,StringIndication,longitudeValue,latitudeValue,StringPrix);
		}
	};
	/*
	private byte[] bitmap_to_byte(Bitmap bmp)
	{
		//File f= new File(this.getCacheDir(), filename);
		//f.createNewFile();

		ByteArrayOutputStream stream= new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 90, stream);
		byte[] bt= stream.toByteArray();
		return bt;

		//FileOutputStream filestream= new FileOutputStream(f);
		//filestream.write(bt);
		//filestream.flush();
		//filestream.close();

	}
	 */

	private void ajouter_lieu(int idDepartement, String indication, String longitude, String latitude, String prix)
	{
		final ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("idDepartement", String.valueOf(idDepartement)));
		nameValuePair.add(new BasicNameValuePair("indication", indication));
		nameValuePair.add(new BasicNameValuePair("longitude", longitude));
		nameValuePair.add(new BasicNameValuePair("latitude", latitude));
		nameValuePair.add(new BasicNameValuePair("prix", prix));
		nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));
		if (bitmap==null)
			nameValuePair.add(new BasicNameValuePair("picture", "null"));
		else
			nameValuePair.add(new BasicNameValuePair("picture", FonctionsLibrary.BitMapToString(FonctionsLibrary.getResizedBitmap(bitmap, 160, 160))));

		//Log.e("monlog", departement);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					HttpClient httpClient=new DefaultHttpClient();
					Log.d("log_tag", fileAjouterLieu);
					HttpPost httpPost=new HttpPost(fileAjouterLieu);
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
					HttpResponse response=httpClient.execute(httpPost);
					HttpEntity entity= response.getEntity();
					is=entity.getContent();
				}
				catch(Exception e){
					Log.e("log_tag", "Error in http connection " + e.toString());
					//e.printStackTrace();
				}

				// Conversion de la requte en string
				try{

					BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						//Log.e("log",line);
						sb.append(line + "\n");
					}
					is.close(); 
					result=sb.toString();
				}catch(Exception e){
					Log.e("log_tag", "Error converting result " + e.toString());
				}

				// Parse  les données JSON

				try{
					Log.e("t",result);
					final boolean resultat;//int j=0;
					JSONObject jObject= new JSONObject(result);
					resultat=((jObject.getString("res").equals("true")) ? true : false);
					final int idLieu=jObject.getInt("idLieu");
					final String date=jObject.getString("date");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (resultat)
							{
								enregistrer_localement_lieu(idLieu,date);
								Toast.makeText(AjouterLieuActivity.this, "Enregistrement effectué avec succès!" ,Toast.LENGTH_SHORT).show();
								AjouterLieuActivity.this.finish();
							}
							else
							{
								Toast.makeText(AjouterLieuActivity.this, "Erreur lors de l'enregistrement!" ,Toast.LENGTH_SHORT).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}

	private void enregistrer_localement_lieu(int idLieu,String date)
	{
		Lieu lieu= new Lieu(idLieu, phone_user.getIdUtilisateur(), idDepartement, StringIndication, ((longitudeValue.equals("")) ? 0 : Double.parseDouble(longitudeValue)), ((latitudeValue.equals("")) ? 0 : Double.parseDouble(latitudeValue)), date, ((bitmap==null) ? null : FonctionsLibrary.bitmap_to_byte(FonctionsLibrary.getResizedBitmap(bitmap, 160, 160))));
		LieuDS.createLieu(lieu);
		AjouterLieu entree=new AjouterLieu(phone_user.getIdUtilisateur(), idLieu, date);
		AjouterLieuDS.createEntree(entree);

		//Enregistrement de prix entré
		Infosdulieu infoslieu= new Infosdulieu(phone_user.getIdUtilisateur(), idLieu, date, Integer.parseInt(editText_prix.getText().toString()), phone_user.getIdUtilisateur(),1);
		InfosduLieuDS.createInfosdulieu(infoslieu);

	}

	public void afficher_departement()
	{
		liste_departements= new ArrayList<Departement>();
		liste_departements=DepartementDS.getAllDepartements();
		liste_libelle_departement=new ArrayList<String>();
		for (int i=0;i<liste_departements.size();i++)
		{
			if (phone_user.getIdDepartement()==liste_departements.get(i).get_idDepartement())
				position_user_departement=i;
			liste_libelle_departement.add(liste_departements.get(i).get_libelleDepartement());			
		}
		ArrayAdapter<String> departement_adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, liste_libelle_departement);

		departement_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner_departement.setAdapter(departement_adapter);
	}

	//cette fonction me permet de prendre une photo a partir du composant par defaut de camera des terminaux android
	private void takePhoto()
	{
		/*
		SimpleDateFormat dateFormat= new SimpleDateFormat("yyyymmddhhmmss");
		String date= dateFormat.format(new Date());
		String photoFile="Picture_"+ date+".jpg";

		Intent intentPhoto=new Intent("android.media.action.IMAGE_CAPTURE");
		Toast.makeText(this, Environment.getExternalStorageDirectory().toString(), Toast.LENGTH_LONG).show();
		File photo=new File(Environment.getExternalStorageDirectory(), photoFile);
		intentPhoto.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
		imageUri=Uri.fromFile(photo);
		 */

		Intent intentPhoto=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//File photo= getOuputPhotoFile();
		imageUri=Uri.fromFile(getOuputPhotoFile());
		intentPhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intentPhoto, TAKE_PICTURE);
	}

	//cette fonction permet de récuperer un outputPhotofile
	private File getOuputPhotoFile()
	{
		File directory= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getPackageName());
		if (!directory.exists() && !directory.mkdirs())
		{
			Log.e("tag", "Failed to create storage directory.");
			Toast.makeText(AjouterLieuActivity.this, "Failed to create storage directory.",Toast.LENGTH_LONG).show();
			return null;
		}
		String timeStamp= new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.UK).format(new Date());
		return new File(directory.getPath()+ File.separator+ "IMG_"+timeStamp+".jpg");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode)
		{

		case TAKE_PICTURE:
			if (resultCode==Activity.RESULT_OK)
			{
				try 
				{
					Uri selectImage= imageUri;
					getContentResolver().notifyChange(selectImage, null);
					ContentResolver contentResolver=getContentResolver();

					bitmap=android.provider.MediaStore.Images.Media.getBitmap(contentResolver, selectImage);
					bitmapdrawable= new BitmapDrawable(getResources(), bitmap);
					image_du_lieu.setBackgroundDrawable(bitmapdrawable);
					Toast.makeText(AjouterLieuActivity.this, "Chargement effectuée!", Toast.LENGTH_LONG).show();
				}
				catch (Exception e)
				{
					Toast.makeText(AjouterLieuActivity.this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
					Log.e("Camera", e.toString());
				}

			}
			break;
		case MAP_REQUEST_CODE:
			if (resultCode==Activity.RESULT_OK)
			{
				Double longi=data.getDoubleExtra(result_Longitude,0),lag=data.getDoubleExtra(result_Latitude,0);
				if (longi!=0 && lag!=0)
					editText_coordonnees_lieu.setText(longi+" ; "+lag);
				else
					Toast.makeText(AjouterLieuActivity.this, "Coordonnées non valides! Veuillez reprendre!", Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ajouter_lieu, menu);
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.ajouter_lieu_menu_action_takePicture:
			takePhoto();
			return true;
		case android.R.id.home:
			//onBackPressed();
			return true;
		case R.id.ajouter_lieu_menu_action_valider:
			valider_donnees();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		sensorManager.registerListener(acceleroEventListener, mAccelerometre,SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		sensorManager.unregisterListener(acceleroEventListener, mAccelerometre);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (bitmap==null)
			return null;
		else
			return new SavedInstanceData(bitmap);
	}

	public class SavedInstanceData{
		private Bitmap bmp;
		public SavedInstanceData(Bitmap bmp)
		{
			this.bmp=bitmap;
		}
		public void set_bmpDraw(Bitmap bmp)
		{
			this.bmp=bmp;
		}
		public Bitmap get_bmpDraw()
		{
			return bmp;
		}
	}
}