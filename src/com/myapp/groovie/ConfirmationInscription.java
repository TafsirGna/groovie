package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.UpdateDbObject;

@SuppressLint("NewApi")
public class ConfirmationInscription extends Activity {

	private EditText editText_email;
	private MenuItem menuItem;
	private Menu mMenu;
	private EditText editText_code;
	private UtilisateurDataSource UtilisateurDS;
	private String string_email;
	private String string_code;
	private String verification_code_file= Groovieparams.DBurl+"VerificationCode.php";
	private TestTask task;
	private TelephonyManager phoneManager;
	private Utilisateur MonUser;
	private UpdateDbObject update_db_object;

	final private Handler mhandler= new Handler(){

		// Gérer les communications avec le thread de connexion
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			if (msg.arg1==1)
			{
				//J'indique au niveau de la base de données que l'utilisateur est actif
				Utilisateur user= MonUser;
				user.setActif(1);
				UtilisateurDS.updateUtilisateur(user);

				//je mets à jour la base de données avant connexion
				update_db_object= new UpdateDbObject(ConfirmationInscription.this, user);
				update_db_object.update_db();
				
				//dès que la mise à jour est effectuée, je lance l'activité d'acceuil
				Intent i= new Intent(ConfirmationInscription.this, LesLieuxActivity.class);
				startActivity(i);
				ConfirmationInscription.this.finish();
			}
			else
			{
				Toast.makeText(ConfirmationInscription.this, "Le code ne correspond pas. Veuillez bien réessayer!", Toast.LENGTH_LONG).show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirmation_inscription);

		//je récupère l'intent à l'origine de l'appel de l'activité
		Intent i= getIntent();

		//j'initialie le data source 
		UtilisateurDS= new UtilisateurDataSource(ConfirmationInscription.this);
		UtilisateurDS.open();
		phoneManager=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		MonUser=get_user();
		
		//je récupère l'editText de l'email afin de le fournir 
		editText_email=(EditText) findViewById(id.confirmation_inscription_layout_editText_email);
		editText_email.setText(i.getStringExtra("email"));
		string_email=i.getStringExtra("email");

		//je récupère l'editText du code afin d'effectuer des vérifications
		editText_code=(EditText) findViewById(id.confirmation_inscription_layout_editText_code);

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
	private DialogInterface.OnClickListener dialog_confirmation_yes_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub

			string_code=editText_code.getText().toString();
			verifier_code();

		}
	};
	private DialogInterface.OnClickListener dialog_confirmation_no_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			task.onPostExecute("test");
			editText_code.requestFocus();
		}
	};

	public void verifier_code()
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
					nameValuePair.add(new BasicNameValuePair("email", string_email));
					nameValuePair.add(new BasicNameValuePair("code", string_code));

					HttpClient httpClient=new DefaultHttpClient();
					Log.d("log_tag", verification_code_file);
					HttpPost httpPost=new HttpPost(verification_code_file);
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
							if (resultat)
							{
								Message msg=mhandler.obtainMessage();
								msg.arg1=1;
								mhandler.sendMessage(msg);
							}
							else
							{
								Message msg=mhandler.obtainMessage();
								msg.arg1=0;
								mhandler.sendMessage(msg);
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
		getMenuInflater().inflate(R.menu.confirmation_inscription, menu);
		mMenu=menu;
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.confirmation_inscription_action_suivant:

			if (editText_code.getText().toString().equals(""))
			{
				Toast.makeText(ConfirmationInscription.this, "Veuillez entrer le code!", Toast.LENGTH_LONG).show();
				editText_code.requestFocus();
				break;
			}
			AlertDialog.Builder confirmation= new AlertDialog.Builder(ConfirmationInscription.this);
			confirmation.setCancelable(true);
			confirmation.setMessage("Validez-vous le code entré?");
			confirmation.setTitle("Confirmation");
			confirmation.setPositiveButton("OUI", dialog_confirmation_yes_listener);
			confirmation.setNegativeButton("NON", dialog_confirmation_no_listener);
			confirmation.show();

			menuItem=mMenu.findItem(R.id.confirmation_inscription_action_refresh);
			menuItem.setActionView(R.layout.myactionprogressbar);
			menuItem.expandActionView();
			task = new TestTask();
			task.execute("test");
			return true;
		case R.id.confirmation_inscription_action_refresh:
			//Remet le champ de code à zero 
			editText_code.setText("");
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
