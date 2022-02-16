package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.myapp.groovie.R.drawable;
import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Departement;
import com.myapp.groovie.classes.database.DepartementDataSource;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.ParamsUtilisateur;
import com.myapp.groovie.classes.database.ParamsUtilisateurDataSource;
import com.myapp.groovie.classes.database.Participer;
import com.myapp.groovie.classes.database.ParticiperDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.Item_details_layout_adapter;

public class ProfilUtilisateur extends Activity {

	private UtilisateurDataSource UtilisateurDS;
	private Utilisateur CurrentUser;
	private String[] listeStringDetails= new String[] {"Departement","Email", "Telephone","Follower(s)"};
	private String[] listeStringValues;
	private ListView details_utilisateur_listView;
	private List<HashMap<String, Object>> details_liste;
	private DepartementDataSource DepartementDS;
	private Button layout_button_suivre;
	private ParticiperDataSource ParticiperDS;
	private GroupeDataSource GroupeDS;
	private Utilisateur MonUser;
	private ImageView imageView_utilisateur;
	private TelephonyManager phoneManager;
	private String integrer_groupe_file=Groovieparams.DBurl+"integrer_user_groupe.php";
	private int[] listeIconeDetails= new int[]{R.drawable.icone_localisation,
			R.drawable.icone_ecrire_message,
			R.drawable.icone_appeler,
			R.drawable.icone_like};
	private ParamsUtilisateurDataSource ParamsUtilisateurDS;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profil_utilisateur);

		//j'initialise les variables 
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		DepartementDS= new DepartementDataSource(this);
		DepartementDS.open();
		ParamsUtilisateurDS= new ParamsUtilisateurDataSource(this);
		ParamsUtilisateurDS.open();
		ParticiperDS= new ParticiperDataSource(this);
		ParticiperDS.open();
		GroupeDS= new GroupeDataSource(this);
		GroupeDS.open();
		phoneManager=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		MonUser=get_user();
		imageView_utilisateur=(ImageView) findViewById(id.profil_utilisateur_layout_ImageUser);
		details_utilisateur_listView= (ListView) findViewById(id.profil_utilisateur_layout_listView_detailsUtilisateur);
		layout_button_suivre=(Button) findViewById(id.profil_utilisateur_layout_button_follow);
		details_liste= new ArrayList<HashMap<String,Object>>();
		//je récupère l'intent d'appel de l'activité ProfilUtilisateur
		Intent profil_intent= getIntent();
		int idUtilisateur=profil_intent.getIntExtra("idUtilisateur", 0);
		CurrentUser=get_user_from_idUtilisateur(idUtilisateur);
		ParamsUtilisateur paramsutilisateur=ParamsUtilisateurDS.get_paramsUtilisateur(CurrentUser.get_idParam(), ParamsUtilisateurDS.getAllEntrees());

		// remplissage des données de l'utilisateur avant leur affichage
		if (CurrentUser!=null)
		{
			//je récupère le bar d'actions et renomme la barre
			ActionBar action_bar= getActionBar();
			action_bar.setTitle(CurrentUser.getPseudo());

			listeStringValues=new String[] {get_departement_libelle(CurrentUser.getIdDepartement()),CurrentUser.getEmail(),CurrentUser.getTelephone(),GroupeDS.get_nombre_followers(CurrentUser.getIdUtilisateur())+" utilisateurs qui suivent ce profil"};

		}

		// Limiation de la visibilité des données de l'utilisateur en fonction de son status par rapport a son status
		boolean status_utilisateur= ParticiperDS.isMyMember(CurrentUser.getIdGroupe(), MonUser.getIdUtilisateur());

		//gestion de l'affichage de la photo de l'utilisateur 
		if (CurrentUser.getPhoto()!=null && CurrentUser.getPhoto().length!=3)
		{
			if (paramsutilisateur.get_visibilitePhoto()==2 || (paramsutilisateur.get_visibilitePhoto()==1 && status_utilisateur==true))
				imageView_utilisateur.setImageBitmap(Bitmap.createScaledBitmap(getImageBitmap(CurrentUser.getPhoto()), 640, 640, true));
			else
				imageView_utilisateur.setImageResource(drawable.icon_user);
		}
		else
			imageView_utilisateur.setImageResource(drawable.icon_user);

		//Affichage des détails de l'utilisateur
		HashMap<String, Object> element;
		for (int i=0;i<listeStringDetails.length;i++)
		{
			if (i==1 || i==2)
			{
				if (paramsutilisateur.get_visibiliteCoordonnees()==2 || (paramsutilisateur.get_visibiliteCoordonnees()==1 && status_utilisateur==true))
				{
					element=new HashMap<String, Object>();
					element.put("libelleDetails", listeStringDetails[i]);
					element.put("valueDetails", Html.fromHtml(listeStringValues[i]));
					details_liste.add(element);
				}
			}
			else
			{
				element=new HashMap<String, Object>();
				element.put("libelleDetails", listeStringDetails[i]);
				element.put("valueDetails", Html.fromHtml(listeStringValues[i]));
				details_liste.add(element);
			}
		}
		Item_details_layout_adapter details_lieu_Adapter= new Item_details_layout_adapter(ProfilUtilisateur.this, details_liste, listeIconeDetails);
		details_utilisateur_listView.setAdapter(details_lieu_Adapter);

		//lorsque l'utilisateur clique sur le numero de telephone, l'email
		details_utilisateur_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if(position==1)
				{
					Intent intentEmail= new Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto", CurrentUser.getEmail(), null));
					intentEmail.putExtra(Intent.EXTRA_SUBJECT, "");
					intentEmail.putExtra(Intent.EXTRA_TEXT, "");
					startActivity(Intent.createChooser(intentEmail, "Send Email..."));
				}
				if (position==2)
				{
					Uri uri_telephone=Uri.parse("tel:"+CurrentUser.getTelephone());
					Intent intent_telephone=new Intent(Intent.ACTION_DIAL,uri_telephone);
					startActivity(intent_telephone);
				}
			}
		});
		isfollowed();
		layout_button_suivre.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//Toast.makeText(ProfilUtilisateur.this, "reste à developper",Toast.LENGTH_LONG).show();
				integrer_groupe("j'integre", CurrentUser.getIdGroupe());
			}
		});

		//lorsque l'utilisateur clique sur l'image une activité lui apparait afin de mieux voir la photo
		imageView_utilisateur.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent_picture_user= new Intent(ProfilUtilisateur.this, PhotoUserActivity.class);
				intent_picture_user.putExtra("photo", CurrentUser.getPhoto());
				intent_picture_user.putExtra("titre", CurrentUser.getPseudo());
				intent_picture_user.putExtra("activityParent", "utilisateur");
				startActivity(intent_picture_user);
			}
		});
	}

	private Bitmap getImageBitmap(byte[] image)
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}
	private void integrer_groupe(final String cas,final int user_selected)
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
					nameValuePair.add(new BasicNameValuePair("idGroupe", String.valueOf(user_selected)));
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(MonUser.getIdUtilisateur())));
					nameValuePair.add(new BasicNameValuePair("cas", cas));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(integrer_groupe_file);
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
					JSONObject jObject = new JSONObject(result);
					final String res=jObject.getString("res");
					final String date=jObject.getString("date");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (res.equals("true"))
							{
								enregistrer_demande_localement(user_selected, date);
								Toast.makeText(ProfilUtilisateur.this, "Demande envoyée!", Toast.LENGTH_SHORT).show();
							}
							else
								Toast.makeText(ProfilUtilisateur.this, "Une erreur s'est produite lors de l'envoi!", Toast.LENGTH_SHORT).show();
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}

	private void enregistrer_demande_localement(int idGroupe,String date)
	{
		Participer entree= new Participer(idGroupe, MonUser.getIdUtilisateur(), "1900-01-01 00:00:00", "1900-01-01 00:00:00", date,0,1);
		ParticiperDS.createParticiper(entree);
		layout_button_suivre.setVisibility(View.GONE);
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
	private boolean isfollowed()
	{
		List<Participer> liste_entrees_participer= ParticiperDS.getAllEntrees();
		int listeSize=liste_entrees_participer.size();
		for (int i=0;i<listeSize;i++)
		{
			if (liste_entrees_participer.get(i).get_idGroupe()==CurrentUser.getIdGroupe() && liste_entrees_participer.get(i).get_idUtilisateur()==MonUser.getIdUtilisateur() && !liste_entrees_participer.get(i).get_dateDemande().equals("1900-01-01 00:00:00"))
			{
				layout_button_suivre.setVisibility(View.GONE);
				return true;
			}
		}
		return false;
	}
	private String get_departement_libelle(int idDepartement)
	{
		List<Departement> liste_des_departements=DepartementDS.getAllDepartements();
		int listeSize=liste_des_departements.size();
		for (int i=0;i<listeSize;i++)
		{
			if (idDepartement==liste_des_departements.get(i).get_idDepartement())
				return liste_des_departements.get(i).get_libelleDepartement();
		}
		return null;
	}


	private Utilisateur get_user_from_idUtilisateur(int identifiant)
	{
		List<Utilisateur> liste_des_utilisateurs=UtilisateurDS.getAllUtilisateurs();
		int listeSize=liste_des_utilisateurs.size();
		for(int i=0;i<listeSize;i++)
		{
			if (liste_des_utilisateurs.get(i).getIdUtilisateur()==identifiant)
				return liste_des_utilisateurs.get(i);
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profil_utilisateur, menu);
		return true;
	}
}
