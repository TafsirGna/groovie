package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.AjouterLieu;
import com.myapp.groovie.classes.database.AjouterLieuDataSource;
import com.myapp.groovie.classes.database.Departement;
import com.myapp.groovie.classes.database.DepartementDataSource;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.Lieu;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.LieuAdapter;
import com.myapp.groovie.classes.objects.UpdateDbObject;


public class AccueilActivity extends Activity {

	private ListView les_lieux_listView;
	List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
	private String ajouter_mes_lieux_file=Groovieparams.DBurl+"ajouter_a_mes_lieux.php";
	private ListView dialog_listView;
	private ArrayAdapter<String> adapter1;
	private Vibrator myVibrator;
	private Dialog dialog_box;
	private TextView textView_no_reseau;
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private LieuDataSource LieuDS;
	private ProgressBar layout_progressbar;
	private EditText editText_rechercher;
	private String lieu_a_rechercher;
	private List<Lieu> liste_resultats_recherche= new ArrayList<Lieu>();
	private AjouterLieuDataSource AjouterLieuDS;
	private boolean ajoute;
	private Utilisateur phone_user;
	private TelephonyManager phoneManager;
	private UtilisateurDataSource UtilisateurDS;
	private InfosdulieuDataSource InfosduLieuDS;
	private int DUREE_RAFRAICHISSEMENT=30000;
	private int position_item_selected;
	private UpdateDbObject update_db_object;
	private SharedPreferences groovie_preferences;
	private String preference_localisation_contenu;
	private DepartementDataSource DepartementDS;
	private List<Departement> liste_departements;
	private TextView zero_lieu_TextView;

	private String[] drawerItemsList;
	private ListView myDrawer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accueil);

		/*
		drawerItemsList = getResources().getStringArray(R.array.items);
		myDrawer = (ListView) findViewById(R.id.my_drawer);
		myDrawer.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_item, drawerItemsList));
		 */

		//Ceci me permet de faire vibrer le téléphone à chaque que l'utilisateur fait un long click sur un item de ma listView

		groovie_preferences= PreferenceManager.getDefaultSharedPreferences(this);
		preference_localisation_contenu= groovie_preferences.getString("localisation_contenue_listpreference_key","");

		LieuDS= new LieuDataSource(AccueilActivity.this);
		LieuDS.open();
		DepartementDS= new DepartementDataSource(this);
		DepartementDS.open();
		UtilisateurDS= new UtilisateurDataSource(AccueilActivity.this);
		UtilisateurDS.open();
		InfosduLieuDS= new InfosdulieuDataSource(this);
		InfosduLieuDS.open();
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		textView_no_reseau=(TextView) findViewById(id.acceuil_layout_textView_no_reseau);
		textView_no_reseau.setText(Html.fromHtml("<font color=#FF0000><i> Aucun réseau disponible!</i></font>"));
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();
		textView_no_reseau.setVisibility(View.GONE);
		les_lieux_listView=(ListView) findViewById(R.id.acceuil_layout_listView);
		layout_progressbar=(ProgressBar) findViewById(id.acceuil_layout_progressbar);
		editText_rechercher=(EditText)findViewById(id.acceuil_layout_editText_rechercher);
		AjouterLieuDS= new AjouterLieuDataSource(AccueilActivity.this);
		liste_departements= DepartementDS.getAllDepartements();
		AjouterLieuDS.open();
		phone_user=get_user();
		update_db_object= new UpdateDbObject(this, phone_user);
		zero_lieu_TextView= (TextView) findViewById(R.id.acceuil_layout_indication_zero_lieux);

		dialog_box= new Dialog(AccueilActivity.this);
		dialog_listView=new ListView(this);
		dialog_box.setTitle("Actions");

		String stringAucunLieu="<font color=#0000FF><i> Aucun lieu disponible!</i></font>";
		zero_lieu_TextView.setText(Html.fromHtml(stringAucunLieu));	

		editText_rechercher.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				lieu_a_rechercher=editText_rechercher.getText().toString();
				afficher_les_lieux(lieu_a_rechercher);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}
		});
		if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
		{
			textView_no_reseau.setVisibility(View.VISIBLE);
		}

		// je récupère tous les lieux de la base de données
		afficher_les_lieux(null);

		les_lieux_listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				position_item_selected=position;
				// je fais vibrer le téléphone
				myVibrator.vibrate(100);

				position_item_selected=position;
				if (isMyPlace(liste_resultats_recherche.get(position).get_idLieu()))
				{
					ajoute=true;
					adapter1= new ArrayAdapter<String>(AccueilActivity.this, android.R.layout.simple_list_item_1);
					adapter1.add("Déjà ajouté à mes lieux");
					adapter1.add("Voir sur la carte");
					dialog_listView.setAdapter(adapter1);
				}
				else
				{
					ajoute=false;
					adapter1= new ArrayAdapter<String>(AccueilActivity.this, android.R.layout.simple_list_item_1);
					adapter1.add("Ajouter à mes lieux");
					adapter1.add("Voir sur la carte");
					dialog_listView.setAdapter(adapter1);
				}
				dialog_box.setContentView(dialog_listView);
				dialog_box.show();
				return true;
			}
		});
		//lorsqu'on clique sur un item de la dialogue box des actions, on a :
		dialog_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				if (position==0)
				{
					if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
					{
						Toast.makeText(AccueilActivity.this, "Aucun réseau disponible!", Toast.LENGTH_LONG).show();
					}
					else 
					{
						if (!ajoute)
						{
							if (AjouterLieuDS.get_nombre_favoris(phone_user.getIdUtilisateur())<=10)
								ajouter_a_mes_lieux(liste_resultats_recherche.get(position_item_selected).get_idLieu());
							else
								Toast.makeText(AccueilActivity.this, "Vous ne pouvez ajouter plus de 10 favoris!", Toast.LENGTH_LONG).show();
						}
						else
							Toast.makeText(AccueilActivity.this, "Ce lieu est déjà enregistré comme favori!", Toast.LENGTH_LONG).show();
					}
				}
				if (position==1)
				{
					Intent map_intent= new Intent(AccueilActivity.this, MapActivity.class);
					map_intent.putExtra("identifiant", 2);
					map_intent.putExtra("identifiant_lieu", liste_resultats_recherche.get(position_item_selected).get_idLieu());
					startActivity(map_intent);
				}
				dialog_box.dismiss();
			}
		});

		//lorsqu'on clique sur un des lieux de la listView, on a :
		les_lieux_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				position_item_selected=position;
				Intent details_lieu_intent= new Intent(AccueilActivity.this, LieuDetailsActivity.class);
				details_lieu_intent.putExtra("idLieu", liste_resultats_recherche.get(position).get_idLieu());
				details_lieu_intent.putExtra("activityparent", "accueilactivity");
				details_lieu_intent.putExtra("image_lieu", liste_resultats_recherche.get(position).get_Picture());
				details_lieu_intent.putExtra("status_lieu", isMyPlace(liste_resultats_recherche.get(position_item_selected).get_idLieu()));
				startActivity(details_lieu_intent);
			}
		});
		textView_no_reseau.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				textView_no_reseau.setVisibility(View.GONE);
				layout_progressbar.setVisibility(View.VISIBLE);

				new java.util.Timer().schedule(new java.util.TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
								{
									layout_progressbar.setVisibility(View.GONE);
									textView_no_reseau.setVisibility(View.VISIBLE);
								}
								else 
								{
									layout_progressbar.setVisibility(View.GONE);
								}
							}
						});
					}

				}, 2000);
			}
		});

		Timer minuteur= new Timer();
		TimerTask tache= new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				//afficher_mes_lieux();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (editText_rechercher.getText().toString().equals(""))
							afficher_les_lieux(null);
					}
				});
			}
		};
		minuteur.schedule(tache, DUREE_RAFRAICHISSEMENT, DUREE_RAFRAICHISSEMENT);
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

	private void afficher_les_lieux(String mot_cle)
	{
		try {
			List<Lieu> liste_des_lieux= new ArrayList<Lieu>();
			liste_des_lieux= LieuDS.getAllLieux();
			int listeSize=liste_des_lieux.size();
			liste_resultats_recherche= new ArrayList<Lieu>();

			if (mot_cle==null)
			{
				liste_resultats_recherche= LieuDS.getAllLieux();
				liste= new ArrayList<HashMap<String,Object>>();
				HashMap<String, Object> element;
				int nb_lieu_preference=0;
				if (!preference_localisation_contenu.equals("Aucun") && !preference_localisation_contenu.equals(""))
				{
					for (int i=0;i<liste_des_lieux.size();i++)
					{
						if (preference_localisation_contenu.equals(DepartementDS.get_departement(liste_des_lieux.get(i).get_idDepartement(), liste_departements).get_libelleDepartement()))
						{
							element=new HashMap<String, Object>();
							element.put("Titre", liste_des_lieux.get(i).get_titre());
							String value="<i>Au prix actuel de <font color=#FF0000>"+((InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_prixmodifie())+ 
									"</font> "+Groovieparams.monnaie+" modifié par <font color=#0000FF>"+((InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())==null || UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_modified_by())==null) ? "?" : UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_modified_by()).getPseudo())+
									"</font> le "+((InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())==null) ? "?" : FonctionsLibrary.formatDateTime(InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_dateModification()))+"</i>";
							element.put("SousTitre", Html.fromHtml(value));
							element.put("status", ((InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_status()));

							// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
							if (liste_des_lieux.get(i).get_Picture()!=null && liste_des_lieux.get(i).get_Picture().length!=3 && liste_des_lieux.get(i).get_Picture().length!=0){
								element.put("picture", (FonctionsLibrary.getImageBitmap(liste_des_lieux.get(i).get_Picture())));
							}
							else 
								element.put("picture", (BitmapFactory.decodeResource(AccueilActivity.this.getResources(), R.drawable.map2)));

							liste.add(element);		

							nb_lieu_preference++;
						}
					}
				}
				else
				{
					for (int i=0;i<liste_des_lieux.size();i++)
					{
						element=new HashMap<String, Object>();
						element.put("Titre", liste_des_lieux.get(i).get_titre());
						String value="<i>Au prix actuel de <font color=#FF0000>"+((InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_prixmodifie())+ 
								"</font> "+Groovieparams.monnaie+" modifié par <font color=#0000FF>"+((InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())==null || UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_modified_by())==null) ? "?" : UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_modified_by()).getPseudo())+
								//"</font> "+Groovieparams.monnaie+" modifié par <font color=#0000FF>"+InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())+                 )
								"</font> le "+((InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())==null) ? "?" : FonctionsLibrary.formatDateTime(InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_dateModification()))+"</i>";
						element.put("SousTitre", Html.fromHtml(value));
						element.put("status", ((InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(liste_des_lieux.get(i).get_idLieu()).get_status()));

						// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
						if (liste_des_lieux.get(i).get_Picture()!=null && liste_des_lieux.get(i).get_Picture().length!=3 && liste_des_lieux.get(i).get_Picture().length!=0)
							element.put("picture", (FonctionsLibrary.getImageBitmap(liste_des_lieux.get(i).get_Picture())));
						else 
							element.put("picture", (BitmapFactory.decodeResource(AccueilActivity.this.getResources(), R.drawable.map2)));
						liste.add(element);	

						nb_lieu_preference++;
					}	
				}
				LieuAdapter adapter= new LieuAdapter(AccueilActivity.this, liste);
				les_lieux_listView.setAdapter(adapter);

				if(nb_lieu_preference==0)
				{
					zero_lieu_TextView.setVisibility(View.VISIBLE);
					les_lieux_listView.setVisibility(View.GONE);
				}
				else
				{
					zero_lieu_TextView.setVisibility(View.GONE);
					les_lieux_listView.setVisibility(View.VISIBLE);
				}

			}
			else
			{
				Pattern model= Pattern.compile("^"+mot_cle+".*$");
				Log.e("motcle", mot_cle);
				for (int i=0;i<listeSize;i++)
				{
					Matcher match= model.matcher(liste_des_lieux.get(i).get_titre());
					if (match.find())
					{
						liste_resultats_recherche.add(liste_des_lieux.get(i));
					}
				}
				if (liste_resultats_recherche.size()==0)
				{
					editText_rechercher.setTextColor(Color.RED);
					zero_lieu_TextView.setVisibility(View.VISIBLE);
					les_lieux_listView.setVisibility(View.GONE);
				}
				else
				{
					editText_rechercher.setTextColor(Color.BLACK);
					zero_lieu_TextView.setVisibility(View.GONE);
					les_lieux_listView.setVisibility(View.VISIBLE);
				}
				liste= new ArrayList<HashMap<String,Object>>();
				HashMap<String, Object> element;
				for (int i=0;i<liste_resultats_recherche.size();i++)
				{
					element=new HashMap<String, Object>();
					element.put("Titre", liste_resultats_recherche.get(i).get_titre());
					String value="<i>Au prix actuel de <font color=#FF0000>"+((InfosduLieuDS.get_entree_reference(liste_resultats_recherche.get(i).get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(liste_resultats_recherche.get(i).get_idLieu()).get_prixmodifie())+" "+Groovieparams.monnaie+"</font> modifié par <font color=#0000FF>"+((InfosduLieuDS.get_entree_reference(liste_resultats_recherche.get(i).get_idLieu())==null) ? "?" : UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(liste_resultats_recherche.get(i).get_idLieu()).get_modified_by()).getPseudo())+"</font> le "
							+((InfosduLieuDS.get_entree_reference(liste_resultats_recherche.get(i).get_idLieu())==null) ? "?" : FonctionsLibrary.formatDateTime(InfosduLieuDS.get_entree_reference(liste_resultats_recherche.get(i).get_idLieu()).get_dateModification()))+"</i>";
					element.put("SousTitre", Html.fromHtml(value));
					element.put("status", ((InfosduLieuDS.get_entree_reference(liste_resultats_recherche.get(i).get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(liste_resultats_recherche.get(i).get_idLieu()).get_status()));

					// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
					if (liste_resultats_recherche.get(i).get_Picture()!=null && liste_resultats_recherche.get(i).get_Picture().length!=3)
						element.put("picture", (FonctionsLibrary.getImageBitmap(liste_resultats_recherche.get(i).get_Picture())));
					else 
						element.put("picture", (BitmapFactory.decodeResource(AccueilActivity.this.getResources(), R.drawable.map2)));

					liste.add(element);
				}
			}
			LieuAdapter adapter= new LieuAdapter(AccueilActivity.this, liste);
			les_lieux_listView.setAdapter(adapter);

		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(AccueilActivity.this, "Echec de la mise à jour des lieux!" , Toast.LENGTH_SHORT).show();
		}
	}

	public String BitMapToString(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		String temp = Base64.encodeToString(b, Base64.DEFAULT);
		return temp;
	}

	private boolean isMyPlace(int id_lieu)
	{
		AjouterLieu entree= new AjouterLieu();
		entree.set_idLieu(id_lieu);
		entree.set_idUtilisateur(phone_user.getIdUtilisateur());
		if (AjouterLieuDS.hasAlreadySaved(entree, AjouterLieuDS.getAllEntrees()))
			return true;
		return false;
	}

	//ByteArrayInputStream byteArrayImageStream = new ByteArrayInputStream(image);
	//result=BitmapFactory.decodeStream(byteArrayImageStream);

	private void ajouter_mon_lieu_localement(String date)
	{
		//String date= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.UK).format(new Date());
		AjouterLieu entree= new AjouterLieu();
		entree.set_dateAjout(date);
		entree.set_idLieu(liste_resultats_recherche.get(position_item_selected).get_idLieu());
		entree.set_idUtilisateur(phone_user.getIdUtilisateur());		
		AjouterLieuDS.createEntree(entree);
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
								Toast.makeText(AccueilActivity.this, "Erreur lors de l'enregistrement du lieu dans vos préférences!", Toast.LENGTH_SHORT).show();
							else
							{
								ajouter_mon_lieu_localement(date);
								Toast.makeText(AccueilActivity.this, "Le lieu a été ajouté à vos préférences avec succès!", Toast.LENGTH_SHORT).show();
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.accueil, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.acceuil_menu_action_ajouter_lieu:
			Intent o=new Intent(AccueilActivity.this, AjouterLieuActivity.class);
			startActivity(o);
			return true;

		case R.id.acceuil_menu_action_actualiser:
			layout_progressbar.setVisibility(View.VISIBLE);
			networkInfo= connectivityManager.getActiveNetworkInfo();
			if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
			{
				layout_progressbar.setVisibility(View.GONE);
				Toast.makeText(AccueilActivity.this, "Aucun réseau disponible!", Toast.LENGTH_LONG).show();
			}
			else
			{
				update_db_object.update_places();
				new java.util.Timer().schedule(new java.util.TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								layout_progressbar.setVisibility(View.GONE);
								Toast.makeText(AccueilActivity.this, "Mise à jour effectuée avec succès!", Toast.LENGTH_LONG).show();
								afficher_les_lieux(null);
							}
						});
					}
				}, 2000);
			}
			return true;


		case R.id.acceuil_menu_action_mesnotifications:
			Intent in=new Intent(AccueilActivity.this, NotificationsActivity.class);
			startActivity(in);
			return true;

		case R.id.acceuil_menu_action_mongroupe:
			Intent inte=new Intent(AccueilActivity.this, GroupeEtUtilisateursActivity.class);
			startActivity(inte);
			return true;

		case R.id.acceuil_menu_action_settings:
			Intent monI=new Intent(AccueilActivity.this, ParametresActivity.class);
			startActivity(monI);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
