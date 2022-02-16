package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.AjouterLieu;
import com.myapp.groovie.classes.database.AjouterLieuDataSource;
import com.myapp.groovie.classes.database.Consommation;
import com.myapp.groovie.classes.database.ConsommationDataSource;
import com.myapp.groovie.classes.database.DepartementDataSource;
import com.myapp.groovie.classes.database.Infosdulieu;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.Lieu;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.Item_details_layout_adapter;

public class LieuDetailsActivity extends Activity {

	private ListView listView_details_lieu;
	private String[] listeStringDetails= new String[] {"Departement","Longitude", "Latitude","Prix actuelle","Abonne(s)","Créé le","Créé par","Dernière consommation"};
	private String[] listeStringValues;
	private int[] listeIconeDetails= new int[]{R.drawable.icone_localisation,R.drawable.icone_longitude,R.drawable.icone_latitude,R.drawable.icone_prix,R.drawable.icone_like,R.drawable.icone_calendrier,R.drawable.ic_action_profil,R.drawable.icone_calendrier};
	private List<HashMap<String, Object>> details_liste;
	private LieuDataSource LieuDS;
	private Lieu CurrentLieu=null;
	private UtilisateurDataSource UtilisateurDS;
	private DepartementDataSource DepartementDS;
	private Button button_abonner;
	private String ajouter_mes_lieux_file=Groovieparams.DBurl+"ajouter_a_mes_lieux.php";
	private String modifier_lieu_file=Groovieparams.DBurl+"modifier_prix.php";
	private String enregistrer_consommation_file=Groovieparams.DBurl+"enregistrer_consommation.php";
	private AjouterLieuDataSource AjouterLieuDS;
	private TelephonyManager phoneManager;
	private Utilisateur phone_user;
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private boolean status_lieu_ajoute;
	//private ImageView imageView_localisation_lieu;
	private RelativeLayout image_localisation_lieu;
	private EditText editText_prix;
	private  InfosdulieuDataSource InfosduLieuDS;
	private EditText editText_quantite;
	private ConsommationDataSource ConsommationDS;
	private CheckBox checkbox_entrerprix;
	private static final int TAKE_PICTURE=1;
	private Uri imageUri;
	private BitmapDrawable bitmapdrawable=null;
	private Bitmap bitmap;
	private String modifier_image_file=Groovieparams.DBurl+"modifier_image_lieu.php";
	private Display mDisplay;
	private boolean paysage=false;
	private boolean paysage2;
	private SharedPreferences groovie_preferences;
	private boolean paysage_picture_preference;
	private SensorManager sensorManager;
	private Sensor mAccelerometre;
	private SensorEventListener acceleroEventListener;
	private byte[] image_lieu;
	private SavedInstanceData savedData=null;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lieu_details);

		//j'initialise les variables sus-mentionnées

		groovie_preferences= PreferenceManager.getDefaultSharedPreferences(this);
		paysage_picture_preference=groovie_preferences.getBoolean("paysage_picture_key", false);
		sensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometre= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mDisplay= ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		image_localisation_lieu= (RelativeLayout) findViewById(id.lieu_details_layout_imageLieu);
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();
		listView_details_lieu= (ListView) findViewById(id.lieu_details_layout_listView_detailsLieu);
		phoneManager=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		LieuDS= new LieuDataSource(this);
		LieuDS.open();
		ConsommationDS= new ConsommationDataSource(this);
		ConsommationDS.open();
		InfosduLieuDS= new InfosdulieuDataSource(this);
		InfosduLieuDS.open();
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		DepartementDS= new DepartementDataSource(this);
		DepartementDS.open();
		AjouterLieuDS= new AjouterLieuDataSource(this);
		AjouterLieuDS.open();
		phone_user=get_user();
		button_abonner= (Button) findViewById(id.lieu_details_layout_button_abonner);

		//je récupère l'intent d'appel de cette activity
		Intent intent_details=getIntent();
		CurrentLieu=LieuDS.get_Lieu(intent_details.getIntExtra("idLieu", 0));
		status_lieu_ajoute= intent_details.getBooleanExtra("status_lieu", false);
		image_lieu= intent_details.getByteArrayExtra("image_lieu");
		//String image_lieu= intent_details.getStringExtra("image_lieu");
		String ActivityParent=intent_details.getStringExtra("activityparent");

		//je récupère l'actionbar 
		ActionBar action_bar=getActionBar();
		action_bar.setTitle(CurrentLieu.get_titre());

		//gestion de la photo du lieu en fonction de la configuration
		SavedInstanceData data = (SavedInstanceData)	getLastNonConfigurationInstance();
		if(data == null)
		{
			if (image_lieu!=null && image_lieu.length!=3 && image_lieu.length!=0)
			{
				bitmapdrawable= new BitmapDrawable(getResources(), FonctionsLibrary.getImageBitmap(image_lieu));
				image_localisation_lieu.setBackgroundDrawable(bitmapdrawable);
			}
		}
		else{
			image_localisation_lieu.setBackgroundDrawable(new BitmapDrawable(getResources(), data.get_bmpDraw()));
			bitmap=data.get_bmpDraw();
		}

		afficher_details_lieu();

		if (ActivityParent.equals("meslieuxactivity"))
		{
			//je gère ici l'affichage de bouton abonner en fonction de l'activité parent
			if (intent_details.getIntExtra("modifier_prix", 3)==1)
			{
				RelativeLayout layout= (RelativeLayout) RelativeLayout.inflate(LieuDetailsActivity.this, R.layout.layout_modifierlieu_dialogbox, null);
				LinearLayout intitule_quantite= (LinearLayout) layout.findViewById(id.layout_modifierlieu_dialogbox_groupe_intitule_quantite);
				LinearLayout intitule_prix= (LinearLayout) layout.findViewById(id.layout_modifierlieu_dialogbox_groupe_intitule_prix);
				EditText editText_quantite=(EditText) layout.findViewById(id.layout_modifierlieu_dialogbox_editText_quantite);
				editText_prix=(EditText) layout.findViewById(id.layout_modifierlieu_dialogbox_editText_prix);
				CheckBox checkbox_entrerprix=(CheckBox) layout.findViewById(id.layout_modifierlieu_dialogbox_checkbox_entrerprix);

				AlertDialog.Builder confirmation;
				intitule_prix.setVisibility(View.VISIBLE);
				editText_prix.setVisibility(View.VISIBLE);
				confirmation= new AlertDialog.Builder(LieuDetailsActivity.this);
				confirmation.setCancelable(true);
				confirmation.setView(layout);
				confirmation.setPositiveButton("valider", dialog_confirmation_yes_prix_listener);
				confirmation.show();
			}
			button_abonner.setVisibility(View.GONE);
		}
		else 
		{
			if (status_lieu_ajoute==true)
			{
				//je dissimule le bouton si cette activité a été appelé a partir de de l'activité parent lieu accueilactivity et déjà ajouté
				button_abonner.setVisibility(View.GONE);
			}
		}

		listView_details_lieu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				switch (position) {
				case 3:
				{
					RelativeLayout layout= (RelativeLayout) RelativeLayout.inflate(LieuDetailsActivity.this, R.layout.layout_modifierlieu_dialogbox, null);
					LinearLayout intitule_quantite= (LinearLayout) layout.findViewById(id.layout_modifierlieu_dialogbox_groupe_intitule_quantite);
					LinearLayout intitule_prix= (LinearLayout) layout.findViewById(id.layout_modifierlieu_dialogbox_groupe_intitule_prix);
					EditText editText_quantite=(EditText) layout.findViewById(id.layout_modifierlieu_dialogbox_editText_quantite);
					editText_prix=(EditText) layout.findViewById(id.layout_modifierlieu_dialogbox_editText_prix);
					CheckBox checkbox_entrerprix=(CheckBox) layout.findViewById(id.layout_modifierlieu_dialogbox_checkbox_entrerprix);

					AlertDialog.Builder confirmation;
					intitule_prix.setVisibility(View.VISIBLE);
					editText_prix.setVisibility(View.VISIBLE);
					confirmation= new AlertDialog.Builder(LieuDetailsActivity.this);
					confirmation.setCancelable(true);
					confirmation.setView(layout);
					confirmation.setPositiveButton("valider", dialog_confirmation_yes_prix_listener);
					confirmation.show();

					break;
				}
				case 7:
				{
					RelativeLayout layout= (RelativeLayout) RelativeLayout.inflate(LieuDetailsActivity.this, R.layout.layout_modifierlieu_dialogbox, null);
					LinearLayout intitule_quantite= (LinearLayout) layout.findViewById(id.layout_modifierlieu_dialogbox_groupe_intitule_quantite);
					final LinearLayout intitule_prix= (LinearLayout) layout.findViewById(id.layout_modifierlieu_dialogbox_groupe_intitule_prix);
					RelativeLayout groupe_editText_quantite= (RelativeLayout) layout.findViewById(id.layout_modifierlieu_dialogbox_groupe_editText_quantite);
					editText_quantite=(EditText) layout.findViewById(id.layout_modifierlieu_dialogbox_editText_quantite);
					editText_prix=(EditText) layout.findViewById(id.layout_modifierlieu_dialogbox_editText_prix);
					checkbox_entrerprix=(CheckBox) layout.findViewById(id.layout_modifierlieu_dialogbox_checkbox_entrerprix);
					checkbox_entrerprix.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
							// TODO Auto-generated method stub
							if (arg1==true)
							{
								intitule_prix.setVisibility(View.VISIBLE);
								editText_prix.setVisibility(View.VISIBLE);
							}
							else
							{
								intitule_prix.setVisibility(View.GONE);
								editText_prix.setVisibility(View.GONE);
							}
						}
					});
					AlertDialog.Builder confirmation;
					intitule_quantite.setVisibility(View.VISIBLE);
					groupe_editText_quantite.setVisibility(View.VISIBLE);
					checkbox_entrerprix.setVisibility(View.VISIBLE);

					confirmation= new AlertDialog.Builder(LieuDetailsActivity.this);
					confirmation.setCancelable(true);
					confirmation.setView(layout);
					confirmation.setPositiveButton("valider", dialog_confirmation_yes_quantiteETprix_listener);
					confirmation.show();
					break;
				}
				default:
					break;
				}
			}
		});
		button_abonner.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				networkInfo= connectivityManager.getActiveNetworkInfo();
				if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
					Toast.makeText(LieuDetailsActivity.this, "Aucun réseau disponible!", Toast.LENGTH_LONG).show();
				else{
					if (AjouterLieuDS.get_nombre_favoris(phone_user.getIdUtilisateur())<=10)
						ajouter_a_mes_lieux(CurrentLieu.get_idLieu());
					else
						Toast.makeText(LieuDetailsActivity.this, "Vous ne pouvez ajouter plus de 10 favoris!", Toast.LENGTH_LONG).show();
				}
			}
		});
		//lorsque l'utilisateur clique sur l'image, on a:
		image_localisation_lieu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final Dialog box=new Dialog(LieuDetailsActivity.this);
				ListView dialogbox_listview=new ListView(LieuDetailsActivity.this);

				ArrayAdapter<String> adapter= new ArrayAdapter<String>(LieuDetailsActivity.this, android.R.layout.simple_list_item_1);
				adapter.add("Visualiser la position sur la carte");
				//Si l'utilisateur a déjà ajouté ce lieu
				if (status_lieu_ajoute==true)
					adapter.add("Modifier l'image du lieu");
				dialogbox_listview.setAdapter(adapter);
				dialogbox_listview.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						// TODO Auto-generated method stub
						if (position==0)
						{
							Intent map_intent= new Intent(LieuDetailsActivity.this, MapActivity.class);
							startActivity(map_intent);
						}
						if (position==1)
							takePhoto();
						//pickImage();

						box.dismiss();
					}
				});
				box.setTitle("Actions");
				box.setContentView(dialogbox_listview);
				box.show();
			}
		});

		//je note que la notification a été vu par l'utilisateur
		Infosdulieu entree= InfosduLieuDS.get_entree_reference(CurrentLieu.get_idLieu());
		if (entree!=null)
		{
			entree.set_status(1);
			InfosduLieuDS.updateInfosduLieu(entree);
		}

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

	public void pickImage()
	{
		Intent intent=new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		//imageUri=Uri.fromFile(getOuputPhotoFile());
		//intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

		startActivityForResult(intent, TAKE_PICTURE);
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
			Toast.makeText(LieuDetailsActivity.this, "Failed to create storage directory.",Toast.LENGTH_LONG).show();
			return null;
		}
		String timeStamp= new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.UK).format(new Date());
		return new File(directory.getPath()+ File.separator+ "IMG_"+timeStamp+".jpg");
	}
	private void afficher_details_lieu()
	{
		details_liste= new ArrayList<HashMap<String,Object>>();
		Infosdulieu entree=InfosduLieuDS.get_entree_InfosduLieu(phone_user.getIdUtilisateur(), CurrentLieu.get_idLieu(), InfosduLieuDS.getAllEntrees());
		String prix=new String();
		if (entree!=null)
			prix=String.valueOf(entree.get_prixmodifie());
		else
			prix="0";

		String user_of_modification= new String(); String dateModif=new String();
		if (InfosduLieuDS.get_entree_InfosduLieu(phone_user.getIdUtilisateur(), CurrentLieu.get_idLieu(), InfosduLieuDS.getAllEntrees())!=null)
		{
			//user_of_modification=((InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())==null || UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_modified_by())==null) ? "?" : UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_modified_by()).getPseudo())
			user_of_modification=((UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_InfosduLieu(phone_user.getIdUtilisateur(), CurrentLieu.get_idLieu(), InfosduLieuDS.getAllEntrees()).get_modified_by())==null) ? "?":UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_InfosduLieu(phone_user.getIdUtilisateur(), CurrentLieu.get_idLieu(), InfosduLieuDS.getAllEntrees()).get_modified_by()).getPseudo());
			dateModif=FonctionsLibrary.formatDateTime(InfosduLieuDS.get_entree_InfosduLieu(phone_user.getIdUtilisateur(), CurrentLieu.get_idLieu(), InfosduLieuDS.getAllEntrees()).get_dateModification());
		}
		else
		{
			user_of_modification="?";
			dateModif="?";
		}
		listeStringValues=new String[] {get_departement_libelle(CurrentLieu.get_idDepartement()),
				String.valueOf(CurrentLieu.get_longitude()),
				String.valueOf(CurrentLieu.get_latitude()),
				prix+" "+Groovieparams.monnaie+" modifié par "+user_of_modification+ " le "+ dateModif,
				LieuDS.get_nombre_Abonnees(CurrentLieu.get_idLieu())+" utilisateurs qui sont abonnés à ce lieu",
				FonctionsLibrary.formatDateTime(CurrentLieu.get_dateCreation()), ((UtilisateurDS.get_utilisateur(CurrentLieu.get_idUtilisateur())==null) ? "Administrateur" : UtilisateurDS.get_utilisateur(CurrentLieu.get_idUtilisateur()).getPseudo()),
		"Jamais"};

		HashMap<String, Object> element;
		for (int i=0;i<listeStringDetails.length;i++)
		{
			element=new HashMap<String, Object>();
			element.put("libelleDetails", listeStringDetails[i]);
			if (i==3 || i==7)
			{
				element.put("valueDetails", Html.fromHtml("<font color=#0000FF>"+listeStringValues[i]+"</font>"));
			}
			else
			{
				element.put("valueDetails", Html.fromHtml(listeStringValues[i]));
			}
			details_liste.add(element);
		}
		Item_details_layout_adapter details_lieu_Adapter= new Item_details_layout_adapter(LieuDetailsActivity.this, details_liste, listeIconeDetails);
		listView_details_lieu.setAdapter(details_lieu_Adapter);
	}
	private void ajouter_a_mes_lieux(final int id_lieu)
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;
				//Envoi de la commande http
				try{
					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("idLieu", String.valueOf(id_lieu)));
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(ajouter_mes_lieux_file);
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
					// e.printStackTrace();
				}

				// Parse  les données JSON
				try{
					JSONObject jObject = new JSONObject(result);
					final String res=jObject.getString("res");
					final String date=jObject.getString("date");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (res.equals("false"))
								Toast.makeText(LieuDetailsActivity.this, "Erreur lors de l'enregistrement du lieu dans vos préférences!", Toast.LENGTH_SHORT).show();
							else
							{
								ajouter_mon_lieu_localement(date);
								Toast.makeText(LieuDetailsActivity.this, "Le lieu a été ajouté à vos préférences avec succès!", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}

	private void ajouter_mon_lieu_localement(String date)
	{
		AjouterLieu entree= new AjouterLieu();
		entree.set_dateAjout(date);
		entree.set_idLieu(CurrentLieu.get_idLieu());
		entree.set_idUtilisateur(phone_user.getIdUtilisateur());		
		AjouterLieuDS.createEntree(entree);

		//je dissimule le bouton d'abonnement
		button_abonner.setVisibility(View.GONE);
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

	private DialogInterface.OnClickListener dialog_confirmation_yes_prix_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			if (editText_prix.getText().toString().equals(""))
			{
				Toast.makeText(LieuDetailsActivity.this, "Vous n'avez entré aucun prix!", Toast.LENGTH_LONG).show();
				return;
			}
			modifier_prix(editText_prix.getText().toString());
		}
	};

	private void modifier_prix(final String prix)
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;
				//Envoi de la commande http
				try{
					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));
					nameValuePair.add(new BasicNameValuePair("idLieu", String.valueOf(CurrentLieu.get_idLieu())));
					nameValuePair.add(new BasicNameValuePair("prix", prix));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(modifier_lieu_file);
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
					// e.printStackTrace();
				}

				// Parse  les données JSON
				try{
					Log.e("fast", result);
					JSONObject jObject = new JSONObject(result);
					final String res=jObject.getString("res");
					final String date=jObject.getString("date");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (res.equals("false"))
								Toast.makeText(LieuDetailsActivity.this, "Erreur lors de la modification du prix! Veuillez réessayer!", Toast.LENGTH_LONG).show();
							else
							{
								enregistrer_modification_prix_localement(date);
								Toast.makeText(LieuDetailsActivity.this, "Prix modifié et visible par vos followers!", Toast.LENGTH_LONG).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}
	private void enregistrer_modification_prix_localement(String date)
	{
		List<Infosdulieu> liste_entrees= InfosduLieuDS.getAllEntrees();
		Infosdulieu entree= new Infosdulieu(phone_user.getIdUtilisateur(), CurrentLieu.get_idLieu(), date, Integer.parseInt(editText_prix.getText().toString()), phone_user.getIdUtilisateur(),0);
		if (InfosduLieuDS.get_entree_InfosduLieu(phone_user.getIdUtilisateur(), CurrentLieu.get_idLieu(), liste_entrees)==null)
			InfosduLieuDS.createInfosdulieu(entree);
		else
			InfosduLieuDS.updateInfosduLieu(entree);
		afficher_details_lieu();
	}
	private DialogInterface.OnClickListener dialog_confirmation_yes_quantiteETprix_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub

			if (!checkbox_entrerprix.isChecked())
			{
				String quantite=editText_quantite.getText().toString();
				if (quantite.equals(""))
				{
					Toast.makeText(LieuDetailsActivity.this, "Vous avez laissé un champ quantité vide! Réessayez!", Toast.LENGTH_LONG).show();
					return;
				}
				//je calcule le cout de la présente consommation afin de l'inscrire dans la base de données
				float cout= ((InfosduLieuDS.get_entree_reference(CurrentLieu.get_idLieu())==null) ? 0 : InfosduLieuDS.get_entree_reference(CurrentLieu.get_idLieu()).get_prixmodifie()*Integer.parseInt(quantite));

				enregistrer_consommation(CurrentLieu.get_idLieu(), Integer.parseInt(quantite),cout);
			}
			else
			{
				String quantite=editText_quantite.getText().toString();
				String prix=editText_prix.getText().toString();
				if (quantite.equals("") && prix.equals(""))
				{
					Toast.makeText(LieuDetailsActivity.this, "Vous avez laissé un champ vide! Réessayez!", Toast.LENGTH_LONG).show();
					return;
				}

				//je calcule le cout de la présente consommation afin de l'inscrire dans la base de données
				float cout= Integer.parseInt(prix) * Integer.parseInt(quantite);

				enregistrer_consommation(CurrentLieu.get_idLieu(), Integer.parseInt(quantite), cout);
				modifier_prix(prix);
			}
		}
	};

	private void enregistrer_consommation(final int idLieu, final int quantite, final float cout)
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;
				//Envoi de la commande http
				try{
					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("idLieu", String.valueOf(idLieu)));
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));
					nameValuePair.add(new BasicNameValuePair("quantite", String.valueOf(quantite)));
					nameValuePair.add(new BasicNameValuePair("cout", String.valueOf(cout)));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(enregistrer_consommation_file);
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
					// e.printStackTrace();
				}

				// Parse  les données JSON
				try{
					Log.e("tag", result);
					JSONObject jObject = new JSONObject(result);
					final String res=jObject.getString("res");
					final int idConso=jObject.getInt("idConso");
					final String date=jObject.getString("date");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (res.equals("false"))
								Toast.makeText(LieuDetailsActivity.this, "Erreur lors de l'enregistrement de la consommation!", Toast.LENGTH_SHORT).show();
							else
							{
								ConsommationDS.createConsommation(new Consommation(idConso, CurrentLieu.get_idLieu(), phone_user.getIdUtilisateur(), date, quantite, cout,0));
								Toast.makeText(LieuDetailsActivity.this, "Consommation enregistrée avec succés!", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}
	private String get_departement_libelle(int idDepartement)
	{
		for (int i=0;i<DepartementDS.getAllDepartements().size();i++)
		{
			if (idDepartement==DepartementDS.getAllDepartements().get(i).get_idDepartement())
				return DepartementDS.getAllDepartements().get(i).get_libelleDepartement();
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lieu_details, menu);
		return true;
	}

	private void modifier_image_lieu(final String image)
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;
				//Envoi de la commande http
				try{
					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("idLieu", String.valueOf(CurrentLieu.get_idLieu())));
					nameValuePair.add(new BasicNameValuePair("image", image));
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(modifier_image_file);
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
					// e.printStackTrace();
				}

				// Parse  les données JSON
				try{
					Log.e("tag", result);
					JSONObject jObject = new JSONObject(result);
					final String res=jObject.getString("res");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (res.equals("false"))
								Toast.makeText(LieuDetailsActivity.this, "Erreur lors de la modification!", Toast.LENGTH_SHORT).show();
							else
							{
								Toast.makeText(LieuDetailsActivity.this, "Image modifiée!", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.lieu_details_menu_action_valider:

			if (bitmap!=null)
			{
				networkInfo= connectivityManager.getActiveNetworkInfo();
				if ((networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
				{
					new Thread(new Runnable() {
						@Override
						public void run() {

							//Récuperation du lieu afin de modifier son image d'illustration
							Bitmap rBmp=FonctionsLibrary.getResizedBitmap(bitmap, 160,160);
							ByteArrayOutputStream stream= new ByteArrayOutputStream();
							rBmp.compress(CompressFormat.PNG, 100, stream);
							CurrentLieu.set_Picture(stream.toByteArray());

							LieuDS.updateLieu(CurrentLieu);
						}
					}).start();
					modifier_image_lieu(FonctionsLibrary.BitMapToString(FonctionsLibrary.getResizedBitmap(bitmap, 160,160)));
				}
				else
					Toast.makeText(LieuDetailsActivity.this, "Connexion impossible!", Toast.LENGTH_LONG).show();
			}
			LieuDetailsActivity.this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
					/*
					InputStream stream= null;
					if (bitmap!=null)
						bitmap.recycle();
					stream=getContentResolver().openInputStream(data.getData());
					bitmap=BitmapFactory.decodeStream(stream);
					bitmapdrawable= new BitmapDrawable(getResources(), bitmap);
					image_localisation_lieu.setBackgroundDrawable(bitmapdrawable);
					 */
					bitmap=android.provider.MediaStore.Images.Media.getBitmap(contentResolver, selectImage);
					bitmapdrawable= new BitmapDrawable(getResources(), bitmap);
					image_localisation_lieu.setBackgroundDrawable(bitmapdrawable);
					Toast.makeText(LieuDetailsActivity.this, "Chargement effectuée!", Toast.LENGTH_LONG).show();
				}
				catch (Exception e)
				{
					Toast.makeText(LieuDetailsActivity.this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
					Log.e("Camera", e.toString());
				}
			}
		}
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
