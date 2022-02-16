package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Departement;
import com.myapp.groovie.classes.database.DepartementDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.simple_item_adapter;

public class ProfilActivity extends Activity {

	private final int CHOOSE_PICTURE_REQUETE=1;
	private ImageView photoView;
	private Bitmap bitmap;
	private UtilisateurDataSource UtilisateurDS;
	private DepartementDataSource DepartementDS;
	private TelephonyManager phoneManager;
	private Utilisateur MonUser;
	private ListView listView_details_utilisateur;
	private EditText editText_pseudo;
	private EditText editText_email;
	private EditText editText_password1;
	private EditText editText_password2;
	private Spinner spinner_departement;
	private String modifier_profil_file=Groovieparams.DBurl+"modifier_mon_profil.php";
	private List<Departement> liste_departements= new ArrayList<Departement>();
	private List<String> liste_libelle_departement= new ArrayList<String>();
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private ProgressDialog progressdialog;
	private boolean resultat_modification;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profil);

		//je récupère les éléments du layout 
		progressdialog= new ProgressDialog(this);
		progressdialog.setMessage("Please wait...");
		progressdialog.setIndeterminate(true);
		progressdialog.setCancelable(false);

		DepartementDS= new DepartementDataSource(this);
		DepartementDS.open();
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();
		photoView= (ImageView) findViewById(id.profil_layout_ImageView_utilisateur);
		phoneManager=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listView_details_utilisateur= (ListView) findViewById(id.profil_layout_listView_details_utilisateur);
		UtilisateurDS= new UtilisateurDataSource(ProfilActivity.this);
		UtilisateurDS.open();
		MonUser=get_user();

		afficher_details_profil();

		if (MonUser.getPhoto()!=null && MonUser.getPhoto().length!=3)
			photoView.setImageBitmap(Bitmap.createScaledBitmap(FonctionsLibrary.getImageBitmap(MonUser.getPhoto()), 640, 640, true));
		//je réagis aux évenements sur les éléments du layout
		photoView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				pickImage();
			}
		});

		//lorsque l'utilisateur clique sur un des items de la listView alors, on a: 
		listView_details_utilisateur.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				switch(position)
				{
				case 0:
				{
					RelativeLayout layout= (RelativeLayout) RelativeLayout.inflate(ProfilActivity.this, R.layout.modifier_profil_layout_dialogbox, null);
					TextView textView_pseudo=(TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_pseudo);
					TextView textView_email= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_email);
					TextView textView_departement= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_departement);
					TextView textView_password1= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_password1);
					TextView textView_password2= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_password2);

					editText_pseudo=(EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_pseudo);
					editText_email= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_email);
					spinner_departement= (Spinner) layout.findViewById(id.modifier_profil_layout_dialogbox_spinner_departement);
					editText_password1= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_password1);
					editText_password2= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_password2);

					AlertDialog.Builder confirmation;
					textView_pseudo.setVisibility(View.VISIBLE);
					editText_pseudo.setVisibility(View.VISIBLE);
					confirmation= new AlertDialog.Builder(ProfilActivity.this);
					confirmation.setCancelable(true);
					confirmation.setView(layout);
					confirmation.setPositiveButton("valider", dialog_confirmation_yes_pseudo_listener);
					confirmation.show();
					break;
				}
				case 1:
				{
					RelativeLayout layout= (RelativeLayout) RelativeLayout.inflate(ProfilActivity.this, R.layout.modifier_profil_layout_dialogbox, null);
					TextView textView_pseudo=(TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_pseudo);
					TextView textView_email= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_email);
					TextView textView_departement= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_departement);
					TextView textView_password1= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_password1);
					TextView textView_password2= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_password2);

					editText_pseudo=(EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_pseudo);
					editText_email= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_email);
					spinner_departement= (Spinner) layout.findViewById(id.modifier_profil_layout_dialogbox_spinner_departement);
					editText_password1= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_password1);
					editText_password2= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_password2);

					AlertDialog.Builder confirmation;
					textView_email.setVisibility(View.VISIBLE);
					editText_email.setVisibility(View.VISIBLE);
					confirmation= new AlertDialog.Builder(ProfilActivity.this);
					confirmation.setCancelable(true);
					confirmation.setView(layout);
					confirmation.setPositiveButton("valider", dialog_confirmation_yes_email_listener);
					confirmation.show();
					break;
				}
				case 2:
				{
					RelativeLayout layout= (RelativeLayout) RelativeLayout.inflate(ProfilActivity.this, R.layout.modifier_profil_layout_dialogbox, null);
					TextView textView_pseudo=(TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_pseudo);
					TextView textView_email= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_email);
					TextView textView_departement= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_departement);
					TextView textView_password1= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_password1);
					TextView textView_password2= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_password2);

					editText_pseudo=(EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_pseudo);
					editText_email= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_email);
					spinner_departement= (Spinner) layout.findViewById(id.modifier_profil_layout_dialogbox_spinner_departement);
					editText_password1= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_password1);
					editText_password2= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_password2);

					afficher_departements();
					AlertDialog.Builder confirmation;
					textView_departement.setVisibility(View.VISIBLE);
					spinner_departement.setVisibility(View.VISIBLE);
					confirmation= new AlertDialog.Builder(ProfilActivity.this);
					confirmation.setCancelable(true);
					confirmation.setView(layout);
					confirmation.setPositiveButton("valider", dialog_confirmation_yes_departement_listener);
					confirmation.show();
					break;
				}
				case 3:
				{
					RelativeLayout layout= (RelativeLayout) RelativeLayout.inflate(ProfilActivity.this, R.layout.modifier_profil_layout_dialogbox, null);
					TextView textView_pseudo=(TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_pseudo);
					TextView textView_email= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_email);
					TextView textView_departement= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_departement);
					TextView textView_password1= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_password1);
					TextView textView_password2= (TextView) layout.findViewById(id.modifier_profil_layout_dialogbox_textView_password2);

					editText_pseudo=(EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_pseudo);
					editText_email= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_email);
					spinner_departement= (Spinner) layout.findViewById(id.modifier_profil_layout_dialogbox_spinner_departement);
					editText_password1= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_password1);
					editText_password2= (EditText) layout.findViewById(id.modifier_profil_layout_dialogbox_editText_password2);

					AlertDialog.Builder confirmation;
					textView_password1.setVisibility(View.VISIBLE);
					editText_password1.setVisibility(View.VISIBLE);
					textView_password2.setVisibility(View.VISIBLE);
					editText_password2.setVisibility(View.VISIBLE);
					confirmation= new AlertDialog.Builder(ProfilActivity.this);
					confirmation.setCancelable(true);
					confirmation.setView(layout);
					confirmation.setPositiveButton("valider", dialog_confirmation_yes_password_listener);
					confirmation.show();
					break;
				}
				case 4:
				{
					Toast.makeText(ProfilActivity.this, "Cette information est en lecture seule!", Toast.LENGTH_LONG).show();
					break;
				}
				}
			}
		});
	}

	private byte[] bitmap_to_byte(Bitmap bmp)
	{
		ByteArrayOutputStream stream= new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 90, stream);
		byte[] bt= stream.toByteArray();
		return bt;
	}
	private void afficher_details_profil()
	{
		//je remplis la liste des caractéristiques de l'utilisateur 
		MonUser=get_user();
		String [][] caracteristiques= new String[][]{{"Pseudonyme actuel",MonUser.getPseudo()},
				{"Email",MonUser.getEmail()},
				{"Departement",DepartementDS.get_departement(MonUser.getIdDepartement(),DepartementDS.getAllDepartements()).get_libelleDepartement()},
				{"Mot de passe","*******"},
				{"Téléphone",MonUser.getTelephone()}};
		List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> element;
		for (int i=0;i<caracteristiques.length;i++)
		{
			element=new HashMap<String, Object>();
			element.put("text1", caracteristiques[i][0]);
			element.put("text2", caracteristiques[i][1]);
			liste.add(element);
		}
		simple_item_adapter adapter= new simple_item_adapter(this, liste);
		listView_details_utilisateur.setAdapter(adapter);
	}

	private DialogInterface.OnClickListener dialog_confirmation_yes_pseudo_listener= new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			String pseudo= editText_pseudo.getText().toString();
			if (pseudo.equals(""))
			{
				Toast.makeText(ProfilActivity.this, "La valeur du pseudonyme entré n'est pas valide!", Toast.LENGTH_LONG).show();
			}
			else
			{
				networkInfo= connectivityManager.getActiveNetworkInfo();
				if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
				{
					Toast.makeText(ProfilActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
				}
				else
				{
					progressdialog.show();
					Utilisateur user=get_user();
					user.setPseudo(pseudo);
					//je modifie le pseudonyme de l'utilisateur à distance
					UtilisateurDS.updateUtilisateur(user);
					modifier_utilisateur("pseudo",pseudo);
				}
			}
		}
	};

	private DialogInterface.OnClickListener dialog_confirmation_yes_email_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			String email= editText_email.getText().toString();
			if (email.equals(""))
			{
				Toast.makeText(ProfilActivity.this, "La valeur du email entré n'est pas valide!", Toast.LENGTH_LONG).show();
			}
			else
			{
				networkInfo= connectivityManager.getActiveNetworkInfo();
				if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
				{
					Toast.makeText(ProfilActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
				}
				else
				{
					progressdialog.show();
					Utilisateur user=get_user();
					user.setEmail(email);
					//je modifie le pseudonyme de l'utilisateur à distance
					UtilisateurDS.updateUtilisateur(user);
					modifier_utilisateur("email",email);
				}
			}
		}
	};
	private DialogInterface.OnClickListener dialog_confirmation_yes_departement_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			int position= spinner_departement.getSelectedItemPosition();
			if (position==0)
			{
				Toast.makeText(ProfilActivity.this, "La valeur du département entré n'est pas valide!", Toast.LENGTH_LONG).show();
			}
			else
			{
				networkInfo= connectivityManager.getActiveNetworkInfo();
				if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
				{
					Toast.makeText(ProfilActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
				}
				else
				{
					int idDepartement= liste_departements.get(position-1).get_idDepartement();
					progressdialog.show();
					Utilisateur user=get_user();
					user.setIdDepartement(idDepartement);
					//je modifie le pseudonyme de l'utilisateur à distance
					UtilisateurDS.updateUtilisateur(user);
					modifier_utilisateur("departement",String.valueOf(idDepartement));
				}
			}
		}
	};

	private DialogInterface.OnClickListener dialog_confirmation_yes_password_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			String password1=editText_password1.getText().toString();
			String password2=editText_password2.getText().toString();
			if (password1.equals("") || password2.equals(""))
			{
				Toast.makeText(ProfilActivity.this, "La valeur du mot de passe entré n'est pas valide!", Toast.LENGTH_LONG).show();
			}
			else
			{
				networkInfo= connectivityManager.getActiveNetworkInfo();
				if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
				{
					Toast.makeText(ProfilActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
				}
				else
				{
					if (password1.equals(password2))
					{
						progressdialog.show();
						//Utilisateur user=get_user();
						//user.setPassword(Password);
						//je modifie le pseudonyme de l'utilisateur à distance
						//UtilisateurDS.updateUtilisateur(user);
						modifier_utilisateur("password",password1);
					}
					else
					{
						Toast.makeText(ProfilActivity.this, "Les valeurs entrées ne sont pas identiques!", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	};

	private void modifier_utilisateur(String detail, String value)
	{
		final ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(MonUser.getIdUtilisateur())));
		nameValuePair.add(new BasicNameValuePair("information", detail));
		nameValuePair.add(new BasicNameValuePair("value", value));

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
					Log.d("log_tag", modifier_profil_file);
					HttpPost httpPost=new HttpPost(modifier_profil_file);
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
					//final boolean resultat;//int j=0;
					JSONObject jObject= new JSONObject(result);
					resultat_modification=((jObject.getString("res").equals("true")) ? true : false);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (resultat_modification)
							{
								afficher_details_profil();
								progressdialog.dismiss();
								Toast.makeText(ProfilActivity.this, "Information modifiée avec succès!" ,Toast.LENGTH_SHORT).show();
							}
							else
							{
								progressdialog.dismiss();
								Toast.makeText(ProfilActivity.this, "Erreur lors de l'enregistrement!" ,Toast.LENGTH_SHORT).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
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

	/*
	//cette fonction permet de récupere un outputPhotofile
	private File getOuputPhotoFile()
	{
		File directory= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getPackageName());
		if (!directory.exists() && !directory.mkdirs())
		{
			Log.e("tag", "Failed to create storage directory.");
			Toast.makeText(ProfilActivity.this, "Failed to create storage directory.",Toast.LENGTH_LONG).show();
			return null;
		}
		String timeStamp= new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.UK).format(new Date());
		return new File(directory.getPath()+ File.separator+ "IMG_"+timeStamp+".jpg");
	}
	 */

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode)
		{
		case CHOOSE_PICTURE_REQUETE:
			InputStream stream= null;
			if (resultCode==Activity.RESULT_OK)
			{
				try
				{
					if (bitmap!=null)
						bitmap.recycle();
					stream=getContentResolver().openInputStream(data.getData());
					bitmap=BitmapFactory.decodeStream(stream);
					photoView.setImageBitmap(bitmap);

					progressdialog.show();

					//Le code qui suit affiche à l'utilisateur un message de connexion impossible, lorsque la connexion n'est pas possible
					new java.util.Timer().schedule(new java.util.TimerTask(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (resultat_modification==false)
									{
										progressdialog.dismiss();
										//Toast.makeText(ProfilActivity.this, "Connexion impossible!", Toast.LENGTH_SHORT).show();
									}
								}
							});
						}
					}, 12000);

					UtilisateurDS.insertPicture(MonUser, bitmap);

					//j'enregistre l'image dans la base de données distante
					modifier_utilisateur("photo", FonctionsLibrary.BitMapToString(FonctionsLibrary.getResizedBitmap(bitmap, 160,160)));
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
				finally 
				{
					if (stream!=null)
						try{
							stream.close();
						}
					catch (IOException e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			}
			break;
		}
	}
	 
	public void pickImage()
	{
		Intent intent=new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		/*
		imageUri=Uri.fromFile(getOuputPhotoFile());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		 */
		startActivityForResult(intent, CHOOSE_PICTURE_REQUETE);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profil, menu);
		return true;
	}
}
