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
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groovie.R.drawable;
import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.AjouterLieuDataSource;
import com.myapp.groovie.classes.database.ConsommationDataSource;
import com.myapp.groovie.classes.database.DepartementDataSource;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.ParamsUtilisateurDataSource;
import com.myapp.groovie.classes.database.ParticiperDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;

@SuppressLint("NewApi")
public class SupprimerCompteActivity extends Activity {

	private TextView entete_textview;
	private EditText editText_telephone;
	private EditText editText_password;
	private String supprimer_utilisateur_file=Groovieparams.DBurl+"supprimer_compte_utilisateur.php";
	private int ID_NOTIFICATION_SUPPRESSION;
	private long[] pattern={200,200};
	private Vibrator myVibrator;
	private String string_editText_telephone;
	private String string_editText_password;
	private TestTask task;
	private Menu mMenu;
	private MenuItem menuItem;
	private UtilisateurDataSource UtilisateurDS;
	private AjouterLieuDataSource AjouterLieuDS;
	private DepartementDataSource DepartementDS;
	private GroupeDataSource GroupeDS;
	private LieuDataSource LieuDS;
	private ParticiperDataSource ParticiperDS;
	private ConsommationDataSource ConsommationDS;
	private InfosdulieuDataSource InfosDuLieuDS;
	private ParamsUtilisateurDataSource ParamsUtilisateurDS;
	private Utilisateur phone_user;
	private TelephonyManager phoneManager;
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_supprimer_compte);

		//Ceci me permet de faire vibrer le téléphone à chaque que l'utilisateur recoit une notification
		//je crée une instance de vibrator
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		//Récupération des éléments du layout dans les variables correspondantes
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		entete_textview=(TextView) findViewById(id.supprimer_compte_layout_entete_textview);
		editText_telephone=(EditText) findViewById(id.supprimer_compte_layout_editText_numeroTelephone);
		editText_password=(EditText) findViewById(id.supprimer_compte_layout_editText_motdepasse);

		//Cette portion du code rend rouge le libelle d'information à l'entète du layout
		entete_textview.setText(Html.fromHtml("<b><i><font color=#FF0000>"+entete_textview.getText()+"</font><i></b>"));

		//Initialisation de la data source
		ParamsUtilisateurDS= new ParamsUtilisateurDataSource(this);
		ParamsUtilisateurDS.open();
		UtilisateurDS= new UtilisateurDataSource(SupprimerCompteActivity.this);
		UtilisateurDS.open();
		InfosDuLieuDS= new InfosdulieuDataSource(this);
		InfosDuLieuDS.open();
		ConsommationDS= new ConsommationDataSource(this);
		ConsommationDS.open();
		AjouterLieuDS= new AjouterLieuDataSource(SupprimerCompteActivity.this);
		AjouterLieuDS.open();
		DepartementDS= new DepartementDataSource(SupprimerCompteActivity.this);
		DepartementDS.open();
		GroupeDS= new GroupeDataSource(SupprimerCompteActivity.this);
		GroupeDS.open();
		LieuDS= new LieuDataSource(SupprimerCompteActivity.this);
		LieuDS.open();
		ParticiperDS= new ParticiperDataSource(SupprimerCompteActivity.this);
		ParticiperDS.open();

		phone_user=get_user();

		//je remplis le champ telephone du téléphone du phone_user
		String string_phone_number= phone_user.getTelephone().substring(4);
		editText_telephone.setText(string_phone_number);
	}

	private boolean verifier_donnees()
	{
		if(editText_telephone.getText().toString().equals(""))
		{
			Toast.makeText(this, "Le champ Téléphone est vide. Veuillez le remplir avant de continuer!", Toast.LENGTH_SHORT).show();
			editText_telephone.requestFocus();
			return false;
		}
		if (editText_password.getText().toString().equals(""))
		{
			Toast.makeText(this, "Le champ Mot de passe est vide. Veuillez le remplr avant de continuer!", Toast.LENGTH_SHORT).show();
			editText_password.requestFocus();
			return false;
		}
		string_editText_telephone=editText_telephone.getText().toString();
		string_editText_password=editText_password.getText().toString();
		return true;
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

	private void supprimer_compte()
	{
		final ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("telephone", "+229"+string_editText_telephone));
		nameValuePair.add(new BasicNameValuePair("password", string_editText_password));

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
					Log.d("log_tag", supprimer_utilisateur_file);
					HttpPost httpPost=new HttpPost(supprimer_utilisateur_file);
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
				}

				// Parse  les données JSON
				Log.e("suppression result", result);
				try{
					final boolean resultat;
					JSONObject jObject= new JSONObject(result);
					resultat=((jObject.getString("res").equals("true")) ? true : false);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (resultat)
							{
								notifier_suppression();
								Intent i= new Intent(SupprimerCompteActivity.this, MainActivity.class);
								startActivity(i);
								SupprimerCompteActivity.this.finish();
							}
							else
							{
								Toast.makeText(SupprimerCompteActivity.this, "Le mot de passe est invalide. Veuillez réessayer!" ,Toast.LENGTH_SHORT).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();

	}

	private DialogInterface.OnClickListener dialog_confirmation_yes_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub

			//Ceci permet d'activer la barre de progression
			menuItem=mMenu.findItem(R.id.supprimer_compte_menu_action_refresh);
			menuItem.setActionView(R.layout.myactionprogressbar);
			menuItem.expandActionView();
			task = new TestTask();
			task.execute("test");

			//je supprime toutes les données de la base de données locales
			AjouterLieuDS.deleteAllEntrees();
			DepartementDS.deleteAllDepartements();
			GroupeDS.deleteAllGroupes();
			LieuDS.deleteAllLieux();
			ParticiperDS.deleteAllEntrees();
			UtilisateurDS.deleteAllUtilisateurs();
			ConsommationDS.deleteAllEntrees();
			InfosDuLieuDS.deleteAllEntrees();
			ParamsUtilisateurDS.deleteAllEntrees();

			//Ceci permet de supprimer le compte de l'utilisateur de la base de données distante
			supprimer_compte();
		}
	};
	private DialogInterface.OnClickListener dialog_confirmation_no_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			task.onPostExecute("test");
			editText_telephone.requestFocus();
		}
	};


	@SuppressLint("NewApi")
	private boolean demander_confirmation()
	{
		AlertDialog.Builder confirmation= new AlertDialog.Builder(SupprimerCompteActivity.this);
		confirmation.setCancelable(true);
		confirmation.setMessage("Toutes vos données seront supprimées! Etes-vous sûr de vouloir effectuer cette action?");
		confirmation.setTitle("Attention!");
		confirmation.setPositiveButton("OUI", dialog_confirmation_yes_listener);
		confirmation.setNegativeButton("NON", dialog_confirmation_no_listener);
		confirmation.setIcon(drawable.ic_action_help);
		confirmation.show();
		return true;
	}

	private void notifier_suppression()
	{
		int icon= R.drawable.groovie_logo;
		CharSequence tickerText="Suppression effectuée!";
		long when= System.currentTimeMillis();
		Notification notification_suppression= new Notification(icon, tickerText, when);
		notification_suppression.flags=Notification.FLAG_AUTO_CANCEL;

		Intent notificationIntent= new Intent();
		//notificationIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConservationListActivity");

		PendingIntent contentIntent= PendingIntent.getActivity(SupprimerCompteActivity.this, 0,notificationIntent, 0);

		notification_suppression.setLatestEventInfo(SupprimerCompteActivity.this,"Au revoir!", "Merci d'avoir été dans le monde Groovie!", contentIntent);

		//ceci devrait me permettre d'allumer le led de l'écran
		notification_suppression.defaults|=Notification.DEFAULT_SOUND;
		notification_suppression.defaults|=Notification.DEFAULT_VIBRATE;
		notification_suppression.defaults|=Notification.DEFAULT_LIGHTS;
		notification_suppression.ledARGB=0xff00ff00;
		notification_suppression.ledOnMS=300;
		notification_suppression.ledOffMS=1000;
		notification_suppression.flags|=Notification.FLAG_SHOW_LIGHTS;

		NotificationManager notification_manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification_manager.notify(ID_NOTIFICATION_SUPPRESSION, notification_suppression);

		// je fais vibrer le téléphone 
		myVibrator.vibrate(pattern,-1);

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.supprimer_compte, menu);
		mMenu=menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.supprimer_compte_menu_action_suivant:
			networkInfo= connectivityManager.getActiveNetworkInfo();
			if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
			{
				Toast.makeText(SupprimerCompteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
			}
			else
			{
				if (verifier_donnees())
					demander_confirmation();
			}
			break;
		case R.id.supprimer_compte_menu_action_refresh:
			editText_password.setText("");
			editText_telephone.setText("");
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
