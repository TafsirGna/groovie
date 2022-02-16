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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;

public class ChangerContactActivity extends Activity {

	private Utilisateur phone_user;
	private UtilisateurDataSource UtilisateurDS;
	private TelephonyManager phoneManager;
	private EditText editText_actuel_contact;
	private EditText editText_nouveau_contact;
	private String changer_contact_file=Groovieparams.DBurl+"changer_user_contact.php";
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private String nouveau_contact;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_changer_contact);

		//j'initialise les variables de l'activité
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		editText_actuel_contact= (EditText) findViewById(id.changer_contact_layout_editText_actuel_numero);
		editText_nouveau_contact= (EditText) findViewById(id.changer_contact_layout_editText_nouveau_numero);
		TextView textView_entete= (TextView) findViewById(id.changer_contact_layout_entete_textview);
		textView_entete.setText(Html.fromHtml("<b><i><font color=#0000FF>"+textView_entete.getText()+"</font></i></b>"));
		phone_user= get_user();

		//je remplis le champ telephone du téléphone du phone_user
		String string_phone_number= phone_user.getTelephone().substring(4);
		editText_actuel_contact.setText(string_phone_number);
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
	private void changer_numero(final String nouveau_numero)
	{
		final ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));
		nameValuePair.add(new BasicNameValuePair("nouveau_numero", nouveau_numero));
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(changer_contact_file);
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
					resultat=((jObject.getString("res").equals("true")) ? true : false);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (resultat)
							{
								changer_contact_localement();
								Toast.makeText(ChangerContactActivity.this, "Votre numéro vient d'être modifié avec succès!" ,Toast.LENGTH_LONG).show();
							}
							else
							{
								Toast.makeText(ChangerContactActivity.this, "Un problème est survenue lors de la modification! Veuillez réessayer plus tard!" ,Toast.LENGTH_LONG).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}
	private void changer_contact_localement()
	{
		Utilisateur user=phone_user;
		user.setTelephone("+229"+nouveau_contact);
		UtilisateurDS.updateUtilisateur(user);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.changer_contact, menu);
		return true;
	}
	private DialogInterface.OnClickListener dialog_confirmation_yes_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			//j'appelle la fonction d'enregistrement des utilisateurs dans la base de données 
			networkInfo= connectivityManager.getActiveNetworkInfo();
			if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
			{
				Toast.makeText(ChangerContactActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
			}
			else
			{
				editText_nouveau_contact.setText("");
				changer_numero("+229"+nouveau_contact);
			}
		}
	};
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.changer_contact_menu_action_termine:
			nouveau_contact= editText_nouveau_contact.getText().toString();
			if (nouveau_contact.equals(""))
			{
				Toast.makeText(ChangerContactActivity.this, "Le champ Nouveau contact est vide! Veuillez le remplir avant de continuer!", Toast.LENGTH_LONG).show();
				return false;
			}
			Pattern model= Pattern.compile("^[0-9]{8}$");
			Matcher match= model.matcher(nouveau_contact);
			if (!match.find())
			{
				Toast.makeText(ChangerContactActivity.this, "La valeur du numéro entré est invalide! Veuillez le corriger avant de continuer!", Toast.LENGTH_LONG).show();
				return false;
			}
			// demande de confirmation de la volonté de changer de numéro
			AlertDialog.Builder confirmation= new AlertDialog.Builder(ChangerContactActivity.this);
			confirmation.setCancelable(true);
			confirmation.setMessage("Confirmez-vous le nouveau contact?");
			confirmation.setTitle("Confirmation");
			confirmation.setPositiveButton("OUI", dialog_confirmation_yes_listener);
			confirmation.setNegativeButton("NON", null);
			confirmation.show();
			
		}
		return super.onOptionsItemSelected(item);
	}
}
