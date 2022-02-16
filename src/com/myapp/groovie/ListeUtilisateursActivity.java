package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import com.myapp.groovie.classes.database.Departement;
import com.myapp.groovie.classes.database.DepartementDataSource;
import com.myapp.groovie.classes.database.Groupe;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.ParamsUtilisateur;
import com.myapp.groovie.classes.database.ParamsUtilisateurDataSource;
import com.myapp.groovie.classes.database.Participer;
import com.myapp.groovie.classes.database.ParticiperDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.UpdateDbObject;
import com.myapp.groovie.classes.objects.UtilisateurAdapter;

public class ListeUtilisateursActivity extends Activity {

	//je déclare les variables qui me seront nécessaires tout au long du développement de cette activité 
	private ListView liste_utilisateurs_ListView;
	List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
	private Dialog dialog_box;
	private UtilisateurDataSource UtilisateurDS;
	private String integrer_groupe_file=Groovieparams.DBurl+"integrer_user_groupe.php";
	private Vibrator myVibrator;
	private TextView textView_no_reseau;
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private ProgressBar layout_progressbar;
	private EditText editText_rechercher ;
	private String utilisateur_a_rechercher;
	private List<Utilisateur> liste_resultats_recherche= new ArrayList<Utilisateur>();
	private Utilisateur phone_user;
	private TelephonyManager phoneManager;
	private ParticiperDataSource ParticiperDS;
	private DepartementDataSource DepartementDS;
	private GroupeDataSource GroupeDS;
	private int position_item_selected;
	private boolean inGroupe;
	private boolean isMember;
	private UpdateDbObject update_db_object;
	private int DUREE_RAFRAICHISSEMENT=30000;
	private ParamsUtilisateurDataSource ParamsUtilisateurDS;
	private List<Utilisateur> liste_des_utilisateurs= new ArrayList<Utilisateur>();
	private List<ParamsUtilisateur> liste_paramsUtilisateur;
	private SharedPreferences groovie_preferences;
	private String localisation_contenu_preference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_liste_utilisateurs);

		//j'initialise les variables 
		groovie_preferences= PreferenceManager.getDefaultSharedPreferences(this);
		localisation_contenu_preference= groovie_preferences.getString("localisation_contenue_listpreference_key", "");

		ParticiperDS=new ParticiperDataSource(this);
		ParticiperDS.open();
		GroupeDS= new GroupeDataSource(this);
		GroupeDS.open();
		DepartementDS= new DepartementDataSource(this);
		DepartementDS.open();
		ParamsUtilisateurDS= new ParamsUtilisateurDataSource(this);
		ParamsUtilisateurDS.open();
		phoneManager=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		liste_utilisateurs_ListView=(ListView) findViewById(R.id.liste_utilisateurs_layout_listView);
		textView_no_reseau=(TextView) findViewById(id.liste_utilisateurs_layout_textView_no_reseau);
		textView_no_reseau.setText(Html.fromHtml("<font color=#FF0000><i> Aucun réseau disponible!</i></font>"));
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();
		layout_progressbar= (ProgressBar) findViewById(id.liste_utilisateurs_layout_progressbar);
		editText_rechercher=(EditText) findViewById(id.liste_utilisateurs_layout_editText_rechercher);
		UtilisateurDS= new UtilisateurDataSource(ListeUtilisateursActivity.this);
		UtilisateurDS.open();
		phone_user=get_user();
		liste_des_utilisateurs= UtilisateurDS.getAllUtilisateurs();
		liste_paramsUtilisateur=ParamsUtilisateurDS.getAllEntrees();

		update_db_object= new UpdateDbObject(this, phone_user);

		if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
		{
			textView_no_reseau.setVisibility(View.VISIBLE);
		}
		textView_no_reseau.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				textView_no_reseau.setVisibility(View.GONE);
				layout_progressbar.setVisibility(View.VISIBLE);
				networkInfo= connectivityManager.getActiveNetworkInfo();
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

		editText_rechercher.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				utilisateur_a_rechercher=editText_rechercher.getText().toString();
				afficher_liste_utilisateurs(utilisateur_a_rechercher);
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

		//Ceci me permet de faire vibrer le téléphone à chaque que l'utilisateur fait un long click sur un item de ma listView
		//je crée une instance de vibrator
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		//je remplis la liste des membres du groupe 
		afficher_liste_utilisateurs(null);

		//je construis le dialog box qui apparait lorsque j'appuie long sur item de ma listeView
		dialog_box= new Dialog(ListeUtilisateursActivity.this);
		dialog_box.setTitle("Actions");
		final ListView dialog_box_vue=new ListView(ListeUtilisateursActivity.this);

		liste_utilisateurs_ListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				// je fais vibrer le téléphone 
				myVibrator.vibrate(100);
				position_item_selected=position;
				ArrayAdapter<String> adapter1= new ArrayAdapter<String>(ListeUtilisateursActivity.this, android.R.layout.simple_list_item_1);
				adapter1.add("Inviter");
				adapter1.add("Intégrer son groupe");
				adapter1.add("Voir Données Conso");
				dialog_box_vue.setAdapter(adapter1);
				dialog_box.setContentView(dialog_box_vue);
				dialog_box.show();

				return true;
			}
		});

		dialog_box_vue.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				networkInfo= connectivityManager.getActiveNetworkInfo();
				if (position==0)
				{
					if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
					{
						Toast.makeText(ListeUtilisateursActivity.this, "Connexion impossible!", Toast.LENGTH_LONG).show();
					}
					else
					{
						if (!ParticiperDS.isMyMember(phone_user.getIdGroupe(), liste_resultats_recherche.get(position_item_selected).getIdUtilisateur()))
							integrer_groupe("j'invite",liste_resultats_recherche.get(position_item_selected).getIdUtilisateur());
						else
							Toast.makeText(ListeUtilisateursActivity.this,"Vous aviez déjà envoyé une invitation à "+liste_resultats_recherche.get(position_item_selected).getPseudo()+"!" , Toast.LENGTH_LONG).show();
					}
				}
				if (position==1)
				{
					if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
					{
						Toast.makeText(ListeUtilisateursActivity.this, "Connexion impossible!", Toast.LENGTH_LONG).show();
					}
					else
					{
						if (!ParticiperDS.isMyMember(liste_resultats_recherche.get(position_item_selected).getIdGroupe(), phone_user.getIdUtilisateur()))
							integrer_groupe("j'integre",liste_resultats_recherche.get(position_item_selected).getIdGroupe());
						else
							Toast.makeText(ListeUtilisateursActivity.this,"Vous aviez déjà envoyeé une demande à "+liste_resultats_recherche.get(position_item_selected).getPseudo()+"!" , Toast.LENGTH_LONG).show();
					}
				}
				if (position==2)
				{
					// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
					ParamsUtilisateur paramsutilisateur= ParamsUtilisateurDS.get_paramsUtilisateur(liste_resultats_recherche.get(position_item_selected).get_idParam(), liste_paramsUtilisateur);
					Log.e("visibl stats", paramsutilisateur.get_visibiliteStatistiques()+"");
					boolean status_utilisateur= ParticiperDS.isMyMember(liste_resultats_recherche.get(position_item_selected).getIdGroupe(), phone_user.getIdUtilisateur());
					if(paramsutilisateur.get_visibiliteStatistiques()==2 || (paramsutilisateur.get_visibiliteStatistiques()==1 && status_utilisateur==true))
					{
						Intent donnees_conso_intent=new Intent(ListeUtilisateursActivity.this,MoisConsommationActivity.class);
						donnees_conso_intent.putExtra("idUtilisateur",liste_resultats_recherche.get(position_item_selected).getIdUtilisateur());
						donnees_conso_intent.putExtra("indice_mois",Integer.parseInt(new SimpleDateFormat("MM",Locale.UK).format(new Date())));
						//Toast.makeText(ListeUtilisateursActivity.this, Integer.parseInt(new SimpleDateFormat("MM",Locale.UK).format(new Date()))+"", Toast.LENGTH_LONG).show();
						startActivity(donnees_conso_intent);
					}
					else 
						Toast.makeText(ListeUtilisateursActivity.this, "Données de conso non accessibles!", Toast.LENGTH_LONG).show();

				}
				dialog_box.dismiss();
			}
		});
		//lorsaue l'utilisateur clique sur un item de la listview et clique donc sur un utilisateur, on a:
		liste_utilisateurs_ListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent profil_intent=new Intent(ListeUtilisateursActivity.this,ProfilUtilisateur.class);
				profil_intent.putExtra("idUtilisateur", liste_resultats_recherche.get(position).getIdUtilisateur());
				startActivity(profil_intent);
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
							afficher_liste_utilisateurs(null);
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
	private Bitmap getImageBitmap(byte[] image)
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}
	private void afficher_liste_utilisateurs(String mot_cle)
	{	
		List<Departement> liste_des_departements= DepartementDS.getAllDepartements();
		int listeSize=liste_des_utilisateurs.size();
		liste_resultats_recherche=new ArrayList<Utilisateur>();

		//List<Bitmap> liste_icone_utilisateurs= new ArrayList<Bitmap>();

		if (mot_cle==null)
		{
			liste_resultats_recherche=UtilisateurDS.getAllUtilisateurs();
			liste= new ArrayList<HashMap<String,Object>>();
			HashMap<String, Object> element;

			// ceci permet de retirer de la liste a afficher le profil du propriétaire du téléphone
			for (int i=0;i<liste_resultats_recherche.size();i++)
				if (liste_resultats_recherche.get(i).matches(phone_user))
					liste_resultats_recherche.remove(i);

			for (int i=0;i<liste_resultats_recherche.size();i++)
			{
				if (!localisation_contenu_preference.equals("Aucun") && !localisation_contenu_preference.equals(""))
				{
					if (localisation_contenu_preference.equals(DepartementDS.get_departement(liste_des_utilisateurs.get(i).getIdDepartement(), liste_des_departements)))
					{
						Groupe groupe=get_groupe(liste_resultats_recherche.get(i).getIdGroupe());
						element=new HashMap<String, Object>();
						element.put("Pseudo", liste_resultats_recherche.get(i).getPseudo());
						String value="<font color=#0000FF>"+DepartementDS.get_departement(liste_resultats_recherche.get(i).getIdDepartement(), liste_des_departements).get_libelleDepartement()+"</font>";
						element.put("Email", Html.fromHtml(value));

						// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
						ParamsUtilisateur paramsutilisateur= ParamsUtilisateurDS.get_paramsUtilisateur(liste_resultats_recherche.get(i).get_idParam(), liste_paramsUtilisateur);
						boolean status_utilisateur= ParticiperDS.isMyMember(liste_resultats_recherche.get(i).getIdGroupe(), phone_user.getIdUtilisateur());
						if(paramsutilisateur.get_visibilitePhoto()==2 || (paramsutilisateur.get_visibilitePhoto()==1 && status_utilisateur==true))
							element.put("photo", ((liste_resultats_recherche.get(i).getPhoto()==null) ? BitmapFactory.decodeResource(ListeUtilisateursActivity.this.getResources(), R.drawable.icon_user): getImageBitmap(liste_resultats_recherche.get(i).getPhoto())));
						else 
							element.put("photo", (BitmapFactory.decodeResource(ListeUtilisateursActivity.this.getResources(), R.drawable.icon_user)));

						element.put("nbUtilisateur",GroupeDS.get_nombre_followers(liste_resultats_recherche.get(i).getIdUtilisateur())+" personne(s) qui suivent");
						element.put("note", ParticiperDS.get_note_user(liste_resultats_recherche.get(i).getIdGroupe()));
						liste.add(element);
					}
				}
				else
				{
					Groupe groupe=get_groupe(liste_resultats_recherche.get(i).getIdGroupe());
					element=new HashMap<String, Object>();
					element.put("Pseudo", liste_resultats_recherche.get(i).getPseudo());
					String value="<font color=#0000FF>"+DepartementDS.get_departement(liste_resultats_recherche.get(i).getIdDepartement(), liste_des_departements).get_libelleDepartement()+"</font>";
					element.put("Email", Html.fromHtml(value));

					// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
					Log.e("idparam", liste_resultats_recherche.get(i).get_idParam()+" ");
					ParamsUtilisateur paramsutilisateur= ParamsUtilisateurDS.get_paramsUtilisateur(liste_resultats_recherche.get(i).get_idParam(), liste_paramsUtilisateur);
					boolean status_utilisateur= ParticiperDS.isMyMember(liste_resultats_recherche.get(i).getIdGroupe(), phone_user.getIdUtilisateur());
					if(paramsutilisateur.get_visibilitePhoto()==2 || (paramsutilisateur.get_visibilitePhoto()==1 && status_utilisateur==true))
						element.put("photo", ((liste_resultats_recherche.get(i).getPhoto()==null) ? BitmapFactory.decodeResource(ListeUtilisateursActivity.this.getResources(), R.drawable.icon_user): getImageBitmap(liste_resultats_recherche.get(i).getPhoto())));
					else 
						element.put("photo", (BitmapFactory.decodeResource(ListeUtilisateursActivity.this.getResources(), R.drawable.icon_user)));

					element.put("nbUtilisateur",GroupeDS.get_nombre_followers(liste_resultats_recherche.get(i).getIdUtilisateur())+" personne(s) qui suivent");
					element.put("note", ParticiperDS.get_note_user(liste_resultats_recherche.get(i).getIdGroupe()));
					liste.add(element);
				}
			}
		}
		else
		{
			Pattern model= Pattern.compile("^"+mot_cle+".*$");
			for (int i=0;i<listeSize;i++)
			{
				Matcher match= model.matcher(liste_des_utilisateurs.get(i).getEmail());
				if (match.find())
				{
					liste_resultats_recherche.add(liste_des_utilisateurs.get(i));
				}
			}
			if (liste_resultats_recherche.size()==0)
			{
				editText_rechercher.setTextColor(Color.RED);
			}
			else
			{
				editText_rechercher.setTextColor(Color.BLACK);
			}
			liste= new ArrayList<HashMap<String,Object>>();
			HashMap<String, Object> element;

			// ceci permet de retirer de la liste a afficher le profil du propriétaire du téléphone
			for (int i=0;i<liste_resultats_recherche.size();i++)
				if (liste_resultats_recherche.get(i).matches(phone_user))
					liste_resultats_recherche.remove(i);

			for (int i=0;i<liste_resultats_recherche.size();i++)
			{
				if (!localisation_contenu_preference.equals("Aucun") && !localisation_contenu_preference.equals(""))
				{
					if (localisation_contenu_preference.equals(DepartementDS.get_departement(liste_des_utilisateurs.get(i).getIdDepartement(), liste_des_departements)))
					{
						Groupe groupe=get_groupe(liste_resultats_recherche.get(i).getIdGroupe());
						element=new HashMap<String, Object>();
						element.put("Pseudo", liste_resultats_recherche.get(i).getPseudo());
						String value="<font color=#0000FF>"+DepartementDS.get_departement(liste_resultats_recherche.get(i).getIdDepartement(), liste_des_departements).get_libelleDepartement()+"</font>";
						element.put("Email", Html.fromHtml(value));

						// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
						ParamsUtilisateur paramsutilisateur= ParamsUtilisateurDS.get_paramsUtilisateur(liste_resultats_recherche.get(i).get_idParam(), liste_paramsUtilisateur);
						boolean status_utilisateur= ParticiperDS.isMyMember(liste_resultats_recherche.get(i).getIdGroupe(), phone_user.getIdUtilisateur());
						if(paramsutilisateur.get_visibilitePhoto()==2 || (paramsutilisateur.get_visibilitePhoto()==1 && status_utilisateur==true))
							element.put("photo", ((liste_resultats_recherche.get(i).getPhoto()==null) ? BitmapFactory.decodeResource(ListeUtilisateursActivity.this.getResources(), R.drawable.icon_user): getImageBitmap(liste_resultats_recherche.get(i).getPhoto())));
						else 
							element.put("photo", (BitmapFactory.decodeResource(ListeUtilisateursActivity.this.getResources(), R.drawable.icon_user)));

						element.put("nbUtilisateur",GroupeDS.get_nombre_followers(liste_resultats_recherche.get(i).getIdUtilisateur())+" personne(s) qui suivent");
						element.put("note", ParticiperDS.get_note_user(liste_resultats_recherche.get(i).getIdGroupe()));
						liste.add(element);	
					}
				}
				else
				{
					Groupe groupe=get_groupe(liste_resultats_recherche.get(i).getIdGroupe());
					element=new HashMap<String, Object>();
					element.put("Pseudo", liste_resultats_recherche.get(i).getPseudo());
					String value="<font color=#0000FF>"+DepartementDS.get_departement(liste_resultats_recherche.get(i).getIdDepartement(), liste_des_departements).get_libelleDepartement()+"</font>";
					element.put("Email", Html.fromHtml(value));

					// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
					ParamsUtilisateur paramsutilisateur= ParamsUtilisateurDS.get_paramsUtilisateur(liste_resultats_recherche.get(i).get_idParam(), liste_paramsUtilisateur);
					boolean status_utilisateur= ParticiperDS.isMyMember(liste_resultats_recherche.get(i).getIdGroupe(), phone_user.getIdUtilisateur());
					if(paramsutilisateur.get_visibilitePhoto()==2 || (paramsutilisateur.get_visibilitePhoto()==1 && status_utilisateur==true))
						element.put("photo", ((liste_resultats_recherche.get(i).getPhoto()==null) ? BitmapFactory.decodeResource(ListeUtilisateursActivity.this.getResources(), R.drawable.icon_user): getImageBitmap(liste_resultats_recherche.get(i).getPhoto())));
					else 
						element.put("photo", (BitmapFactory.decodeResource(ListeUtilisateursActivity.this.getResources(), R.drawable.icon_user)));

					element.put("nbUtilisateur",GroupeDS.get_nombre_followers(liste_resultats_recherche.get(i).getIdUtilisateur())+" personne(s) qui suivent");
					element.put("note", ParticiperDS.get_note_user(liste_resultats_recherche.get(i).getIdGroupe()));
					liste.add(element);	
				}
			}
		}
		UtilisateurAdapter adapter= new UtilisateurAdapter(ListeUtilisateursActivity.this, liste,R.drawable.icon_user);
		liste_utilisateurs_ListView.setAdapter(adapter);
	}

	private Groupe get_groupe(int idGroupe)
	{
		List<Groupe> liste_groupes=GroupeDS.getAllGroupes();
		int listeSize=liste_groupes.size();
		for (int i=0;i<listeSize;i++)
		{
			if (idGroupe==liste_groupes.get(i).get_idGroupe())
				return liste_groupes.get(i);
		}
		return null;
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
					if (cas.equals("j'integre"))
					{
						nameValuePair.add(new BasicNameValuePair("idGroupe", String.valueOf(user_selected)));
						nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));
						nameValuePair.add(new BasicNameValuePair("cas", cas));
					}
					else
					{
						nameValuePair.add(new BasicNameValuePair("idGroupe",String.valueOf(phone_user.getIdGroupe())));
						nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(user_selected)));
						nameValuePair.add(new BasicNameValuePair("cas", cas));
					}
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
				Log.e("tag_utilisateur", user_selected+" "+ result);
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
								if (cas.equals("j'integre"))
								{
									enregistrer_demande_localement(user_selected,date);
									Toast.makeText(ListeUtilisateursActivity.this, "Demande envoyée!", Toast.LENGTH_SHORT).show();
								}
								else
								{
									enregistrer_invitation_localement(user_selected,date);
									Toast.makeText(ListeUtilisateursActivity.this, "Invitation envoyée!", Toast.LENGTH_SHORT).show();
								}
							}
							else
								Toast.makeText(ListeUtilisateursActivity.this, "Une erreur s'est produite lors de l'envoi!", Toast.LENGTH_SHORT).show();
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}

	private void enregistrer_invitation_localement(int idUtilisateur,String date)
	{
		Participer entree= new Participer(phone_user.getIdGroupe(), idUtilisateur, "1900-01-01 00:00:00", date,  "1900-01-01 00:00:00",0,1);
		ParticiperDS.createParticiper(entree);
	}

	private void enregistrer_demande_localement(int idGroupe, String date)
	{
		Participer entree= new Participer(idGroupe, phone_user.getIdUtilisateur(), "1900-01-01 00:00:00", "1900-01-01 00:00:00", date,0,1);
		ParticiperDS.createParticiper(entree);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.liste_utilisateurs, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{

		case R.id.liste_utilisateurs_menu_action_settings:
			startActivity(new Intent(ListeUtilisateursActivity.this, ParametresActivity.class));
			return true;

		case R.id.liste_utilisateurs_menu_action_actualiser:
			layout_progressbar.setVisibility(View.VISIBLE);
			if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
			{
				layout_progressbar.setVisibility(View.GONE);
				Toast.makeText(ListeUtilisateursActivity.this, "Aucun réseau disponible!", Toast.LENGTH_LONG).show();
			}
			else
			{
				update_db_object.update_users();
				update_db_object.update_params_user();
				update_db_object.update_groups();
				new java.util.Timer().schedule(new java.util.TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								layout_progressbar.setVisibility(View.GONE);
								Toast.makeText(ListeUtilisateursActivity.this, "Mise à jour effectuée avec succès!", Toast.LENGTH_LONG).show();
								afficher_liste_utilisateurs(null);
							}
						});
					}
				}, 2000);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
