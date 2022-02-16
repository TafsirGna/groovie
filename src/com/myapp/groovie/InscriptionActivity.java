package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.myapp.groovie.classes.database.Departement;
import com.myapp.groovie.classes.database.DepartementDataSource;
import com.myapp.groovie.classes.database.Groupe;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.ParamsUtilisateur;
import com.myapp.groovie.classes.database.ParamsUtilisateurDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;

@SuppressLint("NewApi")
public class InscriptionActivity extends Activity {

	private EditText editText_telephone;
	private Spinner spinner_departement;
	private EditText editText_pseudonyme;
	private EditText editText_password;
	private EditText editText_email;
	private String idDevice;
	private String ajout_utilisateur_file=Groovieparams.DBurl+"ajouter_utilisateur.php";
	private List<Departement> liste_departements = new ArrayList<Departement>();
	private List<String> liste_libelle_departement=new ArrayList<String>();
	private int idDepartement=0;
	private String StringPseudo;
	private String StringEmail;
	private String StringPhone;
	private String StringDepartement;
	private String StringPassword;
	private UtilisateurDataSource UtilisateurDS;
	private MenuItem menuItem;
	private  TelephonyManager phoneManager;
	private Menu mMenu;
	private TestTask task;
	private int ID_NOTIFICATION_INSCRIPTION;
	private long[] pattern={200,200};
	private Vibrator myVibrator;
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private DepartementDataSource DepartementDS;
	private GroupeDataSource GroupeDS;
	private ParamsUtilisateurDataSource ParamsUtilisateurDS;

	@SuppressWarnings("unused")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inscription);

		//Ceci permettra de faire vibrer le téléphone à chaque que l'utilisateur recoit une notification
		//Création d'une instance de vibrator
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		//je récupère l'actionbar de l'activité
		ActionBar actionBar=getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);		

		//Initialisation des variables et de la data source
		UtilisateurDS= new UtilisateurDataSource(InscriptionActivity.this);
		UtilisateurDS.open();
		DepartementDS= new DepartementDataSource(InscriptionActivity.this);
		DepartementDS.open();
		GroupeDS= new GroupeDataSource(InscriptionActivity.this);
		GroupeDS.open();
		ParamsUtilisateurDS= new ParamsUtilisateurDataSource(this);
		ParamsUtilisateurDS.open();

		editText_telephone=(EditText) findViewById(R.id.inscription_layout_editText_telephone);
		editText_pseudonyme=(EditText) findViewById(R.id.inscription_layout_editText_pseudonyme);
		editText_email= (EditText) findViewById(R.id.inscription_layout_editText_email);
		editText_password= (EditText) findViewById(R.id.inscription_layout_editText_password);
		spinner_departement = (Spinner) findViewById(R.id.inscription_layout_spinner_departement);
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();
		phoneManager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		idDevice=phoneManager.getDeviceId();

		//Affichage des départements dans le spinner_departement
		afficher_departements();

		// Tentative de récupération du numéro de téléphone du consommateur
		editText_telephone.setText(editText_telephone.getText().toString()+((phoneManager.getLine1Number()==null) ? "" : phoneManager.getLine1Number()));


		spinner_departement.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0,
					View arg1, int position, long arg3) {
				// TODO Auto-generated method stub

				if (spinner_departement.getSelectedItemPosition()!=0)
					idDepartement=liste_departements.get(position-1).get_idDepartement();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void afficher_departements()
	{
		liste_departements= new ArrayList<Departement>();
		liste_departements=DepartementDS.getAllDepartements();
		liste_libelle_departement=new ArrayList<String>();
		liste_libelle_departement.add("Mon departement");
		for (int i=0;i<liste_departements.size();i++)
		{
			liste_libelle_departement.add(liste_departements.get(i).get_libelleDepartement());			
		}
		ArrayAdapter<String> departement_adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, liste_libelle_departement);

		departement_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_departement.setAdapter(departement_adapter);
	}

	private DialogInterface.OnClickListener dialog_confirmation_yes_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			//j'appelle la fonction d'enregistrement des utilisateurs dans la base de données 
			networkInfo= connectivityManager.getActiveNetworkInfo();
			if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
			{
				Toast.makeText(InscriptionActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
				task.onPostExecute("test");
			}
			else
			{
				ajouter_utilisateur(StringPseudo, StringEmail, StringPhone, idDepartement, idDevice, StringPassword);

			}
		}
	};
	private DialogInterface.OnClickListener dialog_confirmation_no_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			task.onPostExecute("test");
			editText_pseudonyme.requestFocus();
		}
	};
	private void ajouter_utilisateur(final String pseudo, final String email, final String phone, final int departement, final String device, final String password )
	{
		final ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("pseudo", pseudo));
		nameValuePair.add(new BasicNameValuePair("email", email));
		nameValuePair.add(new BasicNameValuePair("telephone", "+229"+phone));
		nameValuePair.add(new BasicNameValuePair("departement", String.valueOf(departement)));
		nameValuePair.add(new BasicNameValuePair("device", device));
		nameValuePair.add(new BasicNameValuePair("password", password));
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
					Log.d("log_tag", ajout_utilisateur_file);
					HttpPost httpPost=new HttpPost(ajout_utilisateur_file);
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
					final boolean resultat;
					JSONObject jObject= new JSONObject(result);
					final int idUtilisateur=jObject.getInt("idUtilisateur");
					final int idGroupe=jObject.getInt("idGroupe");
					final int idParams=jObject.getInt("idParam");
					resultat=((jObject.getString("res").equals("true")) ? true : false);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (resultat)
							{
								Utilisateur user=new Utilisateur(idUtilisateur,idDepartement, idGroupe, StringPseudo, StringEmail, "+229"+StringPhone, idDevice,"NULL","NULL",0);
								user.set_idParam(idParams);
								ParamsUtilisateur Params= new ParamsUtilisateur(idParams, idUtilisateur, 0, 2, 2, 2);
								if (UtilisateurDS.createUtilisateur(user)>0 && GroupeDS.createGroupe(new Groupe(idGroupe, idUtilisateur))>0 && ParamsUtilisateurDS.createParamsUtilisateur(Params)>0)
								{
									afficher_notification_inscription();
									Intent i= new Intent(InscriptionActivity.this, ConfirmationInscription.class);
									i.putExtra("email", email);
									startActivity(i);
									InscriptionActivity.this.finish();
								}
							}
							else
							{
								Toast.makeText(InscriptionActivity.this, "Erreur lors de l'enregistrement!" ,Toast.LENGTH_SHORT).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}
	private void afficher_notification_inscription()
	{
		int icon= R.drawable.groovie_logo;
		CharSequence tickerText="Inscription effectuée!";
		long when= System.currentTimeMillis();
		Notification notification_inscription= new Notification(icon, tickerText, when);
		notification_inscription.flags=Notification.FLAG_AUTO_CANCEL;

		Intent notificationIntent= new Intent();
		//notificationIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConservationListActivity");

		PendingIntent contentIntent= PendingIntent.getActivity(InscriptionActivity.this, 0,notificationIntent, 0);

		notification_inscription.setLatestEventInfo(InscriptionActivity.this,"Inscription effectuée!", "Bienvenue à nouveau dans le monde Groovie!", contentIntent);

		//ceci devrait me permettre d'allumer le led de l'écran
		notification_inscription.defaults|=Notification.DEFAULT_SOUND;
		notification_inscription.defaults|=Notification.DEFAULT_VIBRATE;
		notification_inscription.defaults|=Notification.DEFAULT_LIGHTS;
		notification_inscription.ledARGB=0xff00ff00;
		notification_inscription.ledOnMS=300;
		notification_inscription.ledOffMS=1000;
		notification_inscription.flags|=Notification.FLAG_SHOW_LIGHTS;

		NotificationManager notification_manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification_manager.notify(ID_NOTIFICATION_INSCRIPTION, notification_inscription);

		// je fais vibrer le téléphone 
		myVibrator.vibrate(pattern,-1);

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.inscription, menu);
		mMenu=menu;
		return true;
	}

	private boolean verifier_donnees()
	{
		StringPseudo= editText_pseudonyme.getText().toString();
		StringEmail=editText_email.getText().toString();
		StringPhone=editText_telephone.getText().toString();
		StringDepartement=liste_libelle_departement.get(spinner_departement.getSelectedItemPosition());
		StringPassword=editText_password.getText().toString();

		if (StringPseudo.equals(""))
		{
			Toast.makeText(InscriptionActivity.this, "Le champ Pseudonyme est vide! Veuillez le remplir!", Toast.LENGTH_LONG).show();
			editText_pseudonyme.requestFocus();
			return false;
		}

		if (StringEmail.equals(""))
		{
			Toast.makeText(InscriptionActivity.this, "Le champ Email est vide! Vueillez le remplir! ", Toast.LENGTH_LONG).show();
			editText_email.requestFocus();
			return false;
		}

		//j'initialise l'expression régulière à vérifier sur la valeur de l'email
		Pattern model= Pattern.compile("^[a-z0-9._-]+@[a-z0-9._-]{2,}\\.[a-z]{2,4}$");
		Matcher match= model.matcher(StringEmail);
		if (!StringEmail.equals("") && !match.find())
		{
			Toast.makeText(InscriptionActivity.this, "L'adresse email entrée est invalide!", Toast.LENGTH_LONG).show();
			editText_email.requestFocus();
			return false;
		}
		if (StringPhone.equals("") || StringPhone.length()<8)
		{
			Toast.makeText(InscriptionActivity.this, "Le Téléphone est invalide! Veuillez le rectifier!", Toast.LENGTH_LONG).show();
			editText_telephone.requestFocus();
			return false;
		}
		if (StringDepartement.equals("Mon departement"))
		{
			Toast.makeText(InscriptionActivity.this, "Veuillez bien choisir un département!", Toast.LENGTH_LONG).show();
			spinner_departement.requestFocus();
			return false;
		}
		if (StringPassword.equals(""))
		{
			Toast.makeText(InscriptionActivity.this, "Veuillez bien choisir un département!", Toast.LENGTH_LONG).show();
			spinner_departement.requestFocus();
			return false;
		}
		return true;
	}
	private void demander_confirmation()
	{
		AlertDialog.Builder confirmation= new AlertDialog.Builder(InscriptionActivity.this);
		confirmation.setCancelable(true);
		confirmation.setMessage("Confirmez-vous les informations entrées?");
		confirmation.setTitle("Confirmation");
		confirmation.setPositiveButton("OUI", dialog_confirmation_yes_listener);
		confirmation.setNegativeButton("NON", dialog_confirmation_no_listener);
		confirmation.show();
	}
	private void vider_champs()
	{
		editText_pseudonyme.setText("");
		editText_email.setText("");
		editText_password.setText("");
		editText_telephone.setText("");
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.inscription_menu_action_suivant:

			if (verifier_donnees())
			{
				demander_confirmation();
				menuItem=mMenu.findItem(R.id.inscription_menu_action_refresh);
				menuItem.setActionView(R.layout.myactionprogressbar);
				menuItem.expandActionView();
				task = new TestTask();
				task.execute("test");
			}
			return true;
		case R.id.inscription_menu_action_refresh:
			vider_champs();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class TestTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try
			{
				Thread.sleep(1000000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result)
		{
			menuItem.collapseActionView();
			menuItem.setActionView(null);
		}
	}
}
