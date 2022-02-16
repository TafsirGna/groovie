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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.annotation.SuppressLint;
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
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Consommation;
import com.myapp.groovie.classes.database.ConsommationDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.UpdateDbObject;

@SuppressLint("NewApi")
public class RecupererCompteActivity extends Activity {

	private MenuItem menuItem;
	private Menu mMenu;
	private EditText editText_Password;
	private EditText editText_Numero_Phone;
	private TestTask task;
	private String Recuperer_Compte_file=Groovieparams.DBurl+"recuperer_mon_compte.php";
	private String Recuperer_consommation_file=Groovieparams.DBurl+"recuperer_mes_consommations.php";
	private String password_forgotten_file=Groovieparams.DBurl+"password_forgotten.php";
	private UtilisateurDataSource UtilisateurDS;
	private String UserTelephone;
	private String UserPassword;
	private TelephonyManager phoneManager;
	private Vibrator myVibrator;
	private long[] pattern={200,200};
	private int ID_NOTIFICATION_RECUPERATION;
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private ConsommationDataSource ConsommationDS;
	private UpdateDbObject update_db_object;
	private CheckBox checkbox_password_forgotten;
	private LinearLayout layout_groupe_phone_number;
	private EditText editText_email;
	private TextView textview_indication;

	final private Handler mhandler= new Handler(){

		// Gérer les communications avec le thread de connexion
		@SuppressWarnings({ "unchecked", "deprecation" })
		public void handleMessage(Message msg){
			super.handleMessage(msg);

			if (msg.obj!=null)
			{

				List<Object> listeUser=(List<Object>)msg.obj;
				if (listeUser.size()==1)
				{
					JSONObject jObject=(JSONObject) listeUser.get(0);
					try {
						Utilisateur user=new Utilisateur(jObject.getInt("idUtilisateur"),jObject.getInt("idDepartement"), jObject.getInt("idGroupe"), jObject.getString("pseudo"), jObject.getString("email"), "+229"+UserTelephone, phoneManager.getDeviceId(), UserPassword, "NULL", 1);
						user.set_idParam(jObject.getInt("idParam"));
						if (UtilisateurDS.createUtilisateur(user)>0)
						{
							recuperer_mes_donnees_consommation(user.getIdUtilisateur());
							afficher_notification_recuperation();

							//je mets à jour la base de données 
							update_db_object= new UpdateDbObject(RecupererCompteActivity.this, user);
							update_db_object.update_db();

							//j'invoque l'activité de montrant les lieux
							Intent i= new Intent(RecupererCompteActivity.this, LesLieuxActivity.class);
							startActivity(i);
							RecupererCompteActivity.this.finish();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					Toast.makeText(RecupererCompteActivity.this, "Le mot de passe ou le numéro entré est invalide. Veuillez bien réessayer!", Toast.LENGTH_LONG).show();
					task.onPostExecute("test");
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recuperer_compte);

		//j'inialise des variables d'editText

		//j'initialie le data source 
		UtilisateurDS= new UtilisateurDataSource(RecupererCompteActivity.this);
		UtilisateurDS.open();
		ConsommationDS= new ConsommationDataSource(this);
		ConsommationDS.open();

		layout_groupe_phone_number= (LinearLayout) findViewById(id.recuperer_compte_layout_groupe_IndicatifetNumero);
		editText_Numero_Phone=(EditText) findViewById(id.recuperer_compte_layout_editText_telephone);
		editText_Password=(EditText) findViewById(id.recuperer_compte_layout_editText_password);
		editText_email= (EditText) findViewById(id.recuperer_compte_layout_editText_email);
		checkbox_password_forgotten=(CheckBox) findViewById(id.recuperer_compte_layout_checkbox_password_oublie);
		textview_indication= (TextView) findViewById(id.recuperer_compte_layout_entete_textview_indication);
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();


		// J'initialise les évenements sur la checkbox mot de passe oublié
		checkbox_password_forgotten.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub

				if (isChecked==true)
				{
					//Je fais disparaitre les anciens composantes de l'interface pour les remplacer par de nouveaux 
					layout_groupe_phone_number.setVisibility(View.GONE);
					editText_Password.setVisibility(View.GONE);

					editText_email.setVisibility(View.VISIBLE);

					textview_indication.setText(Html.fromHtml(getResources().getString(R.string.recuperer_compte_entete_indication)));
				}
				else
				{
					//Je fais réapparaitre les anciens composantes de l'interface pour les remplacer par de nouveaux 
					layout_groupe_phone_number.setVisibility(View.VISIBLE);
					editText_Password.setVisibility(View.VISIBLE);

					editText_email.setVisibility(View.GONE);

					textview_indication.setText(Html.fromHtml(getResources().getString(R.string.EnteteRecuperationcompte)));
				}
			}
		});

		//Ceci me permet de faire vibrer le téléphone à chaque que l'utilisateur recoit une notification
		//je crée une instance de vibrator
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recuperer_compte, menu);
		mMenu=menu;
		return true;
	}

	private void afficher_notification_recuperation()
	{
		int icon= R.drawable.groovie_logo;
		CharSequence tickerText="Récupération effectuée!";
		long when= System.currentTimeMillis();
		Notification notification_recuperation= new Notification(icon, tickerText, when);
		notification_recuperation.flags=Notification.FLAG_AUTO_CANCEL;

		Intent notificationIntent= new Intent(RecupererCompteActivity.this, ProfilActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent contentIntent= PendingIntent.getActivity(RecupererCompteActivity.this, 0,notificationIntent, 0);

		notification_recuperation.setLatestEventInfo(RecupererCompteActivity.this,"Récupération effectuée!", "Bienvenue à nouveau dans le monde Groovie!", contentIntent);

		//ceci devrait me permettre d'allumer le led de l'écran
		notification_recuperation.defaults|=Notification.DEFAULT_SOUND;
		notification_recuperation.defaults|=Notification.DEFAULT_VIBRATE;
		notification_recuperation.defaults|=Notification.DEFAULT_LIGHTS;
		notification_recuperation.ledARGB=0xff00ff00;
		notification_recuperation.ledOnMS=300;
		notification_recuperation.ledOffMS=1000;
		notification_recuperation.flags|=Notification.FLAG_SHOW_LIGHTS;

		NotificationManager notification_manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification_manager.notify(ID_NOTIFICATION_RECUPERATION, notification_recuperation);

		// je fais vibrer le téléphone 
		myVibrator.vibrate(pattern,-1);

	}
	private DialogInterface.OnClickListener dialog_confirmation_yes_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
			{
				Toast.makeText(RecupererCompteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
				task.onPostExecute("test");
			}
			else
			{
				if (checkbox_password_forgotten.isChecked()){
					change_password_forgotten(editText_email.getText().toString());
				}
				else{
					UserTelephone=editText_Numero_Phone.getText().toString();
					UserPassword=editText_Password.getText().toString();
					recupererCompte();
				}
			}
		}
	};
	private DialogInterface.OnClickListener dialog_confirmation_no_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			task.onPostExecute("test");
			editText_Numero_Phone.requestFocus();
		}
	};

	private void recupererCompte()
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
					nameValuePair.add(new BasicNameValuePair("telephone", "+229"+UserTelephone));
					nameValuePair.add(new BasicNameValuePair("password", UserPassword));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(Recuperer_Compte_file);
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
					HttpResponse response=httpClient.execute(httpPost);
					HttpEntity entity= response.getEntity();
					is=entity.getContent();
				}
				catch(Exception e){
					Log.e("log_tag", "Error in http connection " + e.toString());
					//e.printStackTrace();
				}

				// Conversion de la requete en string
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
					JSONArray jArray = new JSONArray(result);
					final List<Object> l=new ArrayList<Object>();
					int i;
					for (i=0;i<jArray.length();i++)
					{
						l.add(jArray.getJSONObject(i));

					}
					Log.e("tag", String.valueOf(i));
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Message msg=mhandler.obtainMessage();
							msg.obj=l;
							mhandler.sendMessage(msg);
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}

	private void recuperer_mes_donnees_consommation(final int idUtilisateur)
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
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(idUtilisateur)));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(Recuperer_consommation_file);
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
					HttpResponse response=httpClient.execute(httpPost);
					HttpEntity entity= response.getEntity();
					is=entity.getContent();
				}
				catch(Exception e){
					Log.e("log_tag", "Error in http connection " + e.toString());
					//e.printStackTrace();
				}

				// Conversion de la requete en string
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
					JSONArray jArray = new JSONArray(result);
					final List<Object> l=new ArrayList<Object>();
					int i;
					for (i=0;i<jArray.length();i++)
					{
						l.add(jArray.getJSONObject(i));

					}
					Log.e("tag", String.valueOf(i));
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							enregistrer_donnees_consommation(l);
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}

	//Requete de modificaion du mot de passe oublié
	private void change_password_forgotten(final String phone_user_email)
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
					nameValuePair.add(new BasicNameValuePair("email_sent", phone_user_email));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(password_forgotten_file);
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
					HttpResponse response=httpClient.execute(httpPost);
					HttpEntity entity= response.getEntity();
					is=entity.getContent();
				}
				catch(Exception e){
					Log.e("log_tag", "Error in http connection " + e.toString());
					//e.printStackTrace();
				}

				// Conversion de la requete en string
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
					final boolean resultat;
					JSONObject jObject= new JSONObject(result);

					resultat=((jObject.getString("res").equals("true")) ? true : false);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onResultRequestChangePassword(resultat);
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}

	//Traitement des résultats de la requete de changement de password oublié
	private void onResultRequestChangePassword(boolean isChanged)
	{
		if (isChanged==true){
			
			Toast.makeText(this, "Nouveau mot de passe envoyé par mail! Merci de le récupérer!", Toast.LENGTH_LONG).show();
			
			layout_groupe_phone_number.setVisibility(View.VISIBLE);
			editText_Password.setVisibility(View.VISIBLE);
			editText_email.setVisibility(View.GONE);
			checkbox_password_forgotten.setChecked(false);
			checkbox_password_forgotten.setVisibility(View.GONE);
			textview_indication.setText(Html.fromHtml(getResources().getString(R.string.EnteteRecuperationcompte)));
			
			/*
			// J'appel l'activité de confirmation du code envoyé
			startActivity(new Intent(RecupererCompteActivity.this, ConfirmationInscription.class));
			
			// Je finis l'acivité qui est en cours afin d libérer les ressources
			RecupererCompteActivity.this.finish();
			*/
		}
		else{
			Toast.makeText(RecupererCompteActivity.this, "Une erreur est survenue! Veuillez réessayer!", Toast.LENGTH_LONG).show();
		}
	}

	private void enregistrer_donnees_consommation(List<Object> liste)
	{
		int listeSize=liste.size();
		for (int i=0;i<listeSize;i++)
		{
			JSONObject jObject=(JSONObject) liste.get(i);
			Consommation conso=null;
			try {
				conso=new Consommation(jObject.getInt("idConso"), jObject.getInt("idLieu"), jObject.getInt("idUtilisateur"), jObject.getString("dateConso"), jObject.getInt("quantiteConso"), jObject.getInt("cout"), 0);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ConsommationDS.createConsommation(conso);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.recuperation_menu_action_suivant:
			if (!checkbox_password_forgotten.isChecked())
			{
				if (editText_Numero_Phone.getText().toString().equals("") || editText_Numero_Phone.getText().toString().length()<8)
				{
					Toast.makeText(RecupererCompteActivity.this, "Veuillez entrer le numéro de téléphone!", Toast.LENGTH_LONG).show();
					editText_Numero_Phone.requestFocus();
					break;
				}
				if (editText_Password.getText().toString().equals(""))
				{
					Toast.makeText(RecupererCompteActivity.this, "Veuillez entrer le mot de passe!", Toast.LENGTH_LONG).show();
					editText_Password.requestFocus();
					break;
				}
			}
			else
			{
				String email_address=editText_email.getText().toString();

				//j'initialise l'expression régulière à vérifier sur la valeur de l'email
				Pattern model= Pattern.compile("^[a-z0-9._-]+@[a-z0-9._-]{2,}\\.[a-z]{2,4}$");
				Matcher match= model.matcher(email_address);
				if (email_address.equals("") || !match.find())
				{
					Toast.makeText(RecupererCompteActivity.this, "L'adresse email entrée est invalide!", Toast.LENGTH_LONG).show();
					editText_email.requestFocus();
					break;
				}
			}

			AlertDialog.Builder confirmation= new AlertDialog.Builder(RecupererCompteActivity.this);
			confirmation.setCancelable(true);
			confirmation.setMessage("Validez-vous les informations entrées?");
			confirmation.setPositiveButton("OUI", dialog_confirmation_yes_listener);
			confirmation.setNegativeButton("NON", dialog_confirmation_no_listener);
			confirmation.show();

			menuItem=mMenu.findItem(R.id.recuperation_menu_action_refresh);
			menuItem.setActionView(R.layout.myactionprogressbar);
			menuItem.expandActionView();
			task = new TestTask();
			task.execute("test");
			return true;
		case R.id.recuperation_menu_action_refresh:
			// Remet le champ mot de passe a zeéro et le vide 
			editText_Password.setText("");
			return true;
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
