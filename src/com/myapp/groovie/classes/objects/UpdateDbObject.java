package com.myapp.groovie.classes.objects;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.myapp.groovie.classes.database.Groupe;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.Lieu;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.ParamsUtilisateur;
import com.myapp.groovie.classes.database.ParamsUtilisateurDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;

public class UpdateDbObject {

	private Context context;
	private Utilisateur phone_user;
	private String [] liste_actions={"lieu","utilisateur","groupe","paramsutilisateur"};
	GroupeDataSource GroupeDS;
	ParamsUtilisateurDataSource ParamsUtilisateurDS;
	UtilisateurDataSource UtilisateurDS;
	LieuDataSource LieuDS;


	//Je définis le constructeur
	public UpdateDbObject(Context context,Utilisateur phone_user) {
		// TODO Auto-generated constructor stub
		this.context=context;
		this.phone_user=phone_user;
		GroupeDS= new GroupeDataSource(context);
		GroupeDS.open();

		ParamsUtilisateurDS= new ParamsUtilisateurDataSource(context);
		ParamsUtilisateurDS.open();

		UtilisateurDS= new UtilisateurDataSource(context);
		UtilisateurDS.open();

		LieuDS=new LieuDataSource(context);
		LieuDS.open();

	}


	private void load_data(String action,List<Object> liste)
	{
		if (action.equals("paramsutilisateur"))
		{
			int listeSize=liste.size();

			List<ParamsUtilisateur> liste_params=ParamsUtilisateurDS.getAllEntrees();

			int mode_update=((liste_params.size()<=1) ? 0 : 1);

			//Log.e("tag_ajouterlieu", liste_entrees.size()+" "+listeSize);

			//permet de remplir la base de données locale des données de participation aux groupes au premier démarrage (condition mode_update==0)
			if (mode_update==0)
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste.get(i);
					ParamsUtilisateur params=null;
					try {
						params = new ParamsUtilisateur(jObject.getInt("idParam"), jObject.getInt("idUtilisateur"), jObject.getInt("periode"), jObject.getInt("visibilitephoto"), jObject.getInt("visibilitecoordonnees"), jObject.getInt("visibilitestatistiques"));;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (!ParamsUtilisateurDS.hasAlreadySaved(params, liste_params))
						ParamsUtilisateurDS.createParamsUtilisateur(params);
				}
			}
			//permet de remplir la base de données locale des données de participation aux groupes lors de démarrages suivant le premier démarrage (condition mode_update==1)
			else
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste.get(i);
					int status=0;
					try {
						status=jObject.getInt("status");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//permet de supprimer les entrées non valables dans la base de données locale
					if(status==0)
					{
						int numeroEnregistrement=0;
						try {
							//récupération de l'identifiant de la ligne à supprimer
							numeroEnregistrement=jObject.getInt("numeroEnregistrement");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//suppression de la ligne concernée
						ParamsUtilisateur param_user= new ParamsUtilisateur();
						param_user.set_idParams(numeroEnregistrement);
						ParamsUtilisateurDS.deleteParamsUtilisateur(param_user);
					}
					else 
					{
						//récupération de l'entree Ajouterlieu mise à jour 
						ParamsUtilisateur params=null;
						try {
							params = new ParamsUtilisateur(jObject.getInt("idParam"), jObject.getInt("idUtilisateur"), jObject.getInt("periode"), jObject.getInt("visibilitephoto"), jObject.getInt("visibilitecoordonnees"), jObject.getInt("visibilitestatistiques"));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (status==1)
						{
							ParamsUtilisateurDS.updateParamsUtilisateur(params);
						}
						if (status==2)
						{
							ParamsUtilisateurDS.createParamsUtilisateur(params);
						}
					}
				}
			}
			//ParamsUtilisateurDS.close();
		}
		if (action.equals("groupe"))
		{
			int listeSize=liste.size();

			List<Groupe> liste_groupes=GroupeDS.getAllGroupes();

			int mode_update=((liste_groupes.size()<=1 ) ? 0 : 1);

			//Log.e("tag_ajouterlieu", liste_entrees.size()+" "+listeSize);

			//permet de remplir la base de données locale des données de participation aux groupes au premier démarrage (condition mode_update==0)
			if (mode_update==0)
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste.get(i);
					Groupe groupe=null;
					try {
						groupe = new Groupe(jObject.getInt("idGroupe"),jObject.getInt("idUtilisateur"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//if (phone_user.getIdGroupe()!=groupe.get_idGroupe())
					if (!GroupeDS.hasAlreadySaved(groupe, liste_groupes))
						GroupeDS.createGroupe(groupe);
				}
			}
			//permet de remplir la base de données locale des données de participation aux groupes lors de démarrages suivant le premier démarrage (condition mode_update==1)
			else
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste.get(i);
					int status=0;
					try {
						status=jObject.getInt("status");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//permet de supprimer les entrées non valables dans la base de données locale
					if(status==0)
					{
						int numeroEnregistrement=0;
						try {
							//récupération de l'identifiant de la ligne à supprimer
							numeroEnregistrement=jObject.getInt("numeroEnregistrement");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//suppression de la ligne concernée
						Groupe groupe= new Groupe();
						groupe.set_idGroupe(numeroEnregistrement);
						GroupeDS.deleteGroupe(groupe);
					}
					else 
					{
						//récupération de l'entree Ajouterlieu mise à jour 
						Groupe groupe= null;
						try {
							groupe = new Groupe(jObject.getInt("idGroupe"),jObject.getInt("idUtilisateur"));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (status==2)
						{
							GroupeDS.createGroupe(groupe);
						}
					}
				}
			}
			//GroupeDS.close();
		}
		if (action.equals("utilisateur"))
		{
			int listeSize=liste.size();

			int mode_update=((UtilisateurDS.getAllUtilisateurs().size()==1) ? 0 : 1);

			//Log.e("tag_utilisateur", liste_utilisateurs.size()+" "+listeSize);

			//permet de remplir la base de données locale des données de participation aux groupes au premier démarrage (condition mode_update==0)
			if (mode_update==0)
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste.get(i);
					Utilisateur user=null;
					try {
						if (jObject.getInt("idUtilisateur")!=phone_user.getIdUtilisateur())
						{
							user = new Utilisateur(jObject.getInt("idUtilisateur"), jObject.getInt("idDepartement"), jObject.getInt("idGroupe"), jObject.getString("pseudo"),jObject.getString("email"), jObject.getString("telephone"),"NULL", "NULL", "NULL", 1);
							user.setPhoto(Base64.decode(jObject.getString("photo"), Base64.DEFAULT));
							user.set_idParam(jObject.getInt("idParam"));
						}
						else
						{
							user = phone_user;
							user.setPhoto(Base64.decode(jObject.getString("photo"), Base64.DEFAULT));
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (user.getIdUtilisateur()!=phone_user.getIdUtilisateur())
						UtilisateurDS.createUtilisateur(user);
					else
						UtilisateurDS.updateUtilisateur(user);
				}
			}
			//permet de remplir la base de données locale des données de participation aux groupes lors de démarrages suivant le premier démarrage (condition mode_update==1)
			else
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste.get(i);
					int status=0;
					try {
						status=jObject.getInt("status");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//permet de supprimer les entrées non valables dans la base de données locale
					if(status==0)
					{
						int numeroEnregistrement=0;
						try {
							//récupération de l'identifiant de la ligne à supprimer
							numeroEnregistrement=jObject.getInt("numeroEnregistrement");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//suppression de la ligne concernée
						Utilisateur user= new Utilisateur();
						user.setIdUtilisateur(numeroEnregistrement);
						UtilisateurDS.deleteUtilisateur(user);
					}
					else 
					{
						//récupération de l'entree Ajouterlieu mise à jour 
						Utilisateur user= null;
						try {
							user = new Utilisateur(jObject.getInt("idUtilisateur"), jObject.getInt("idDepartement"), jObject.getInt("idGroupe"), jObject.getString("pseudo"),jObject.getString("email"), jObject.getString("telephone"),"NULL", "NULL", "NULL", 1);
							user.setPhoto(Base64.decode(jObject.getString("photo"), Base64.DEFAULT));
							user.set_idParam(jObject.getInt("idParam"));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (status==1)
						{
							UtilisateurDS.updateUtilisateur(user);
						}
						if (status==2)
						{
							UtilisateurDS.createUtilisateur(user);
						}
					}
				}
			}
			//UtilisateurDS.close();
		}
		if (action.equals("lieu"))
		{
			int listeSize=liste.size();

			int mode_update=((LieuDS.getAllLieux().size()==0) ? 0 : 1);

			//Log.e("tag_ajouterlieu", liste_entrees.size()+" "+listeSize);

			//permet de remplir la base de données locale des données de participation aux groupes au premier démarrage (condition mode_update==0)
			if (mode_update==0)
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste.get(i);
					Lieu lieu=null;
					try {
						lieu = new Lieu(jObject.getInt("idLieu"),jObject.getInt("idUtilisateur"),jObject.getInt("idDepartement"), jObject.getString("titre"), jObject.getDouble("longitude"), jObject.getDouble("latitude"), jObject.getString("dateCreation"),Base64.decode(jObject.getString("imageLieu"), Base64.DEFAULT));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					LieuDS.createLieu(lieu);
				}
			}
			//permet de remplir la base de données locale des données de participation aux groupes lors de démarrages suivant le premier démarrage (condition mode_update==1)
			else
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste.get(i);
					int status=0;
					try {
						status=jObject.getInt("status");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//permet de supprimer les entrées non valables dans la base de données locale
					if(status==0)
					{
						int numeroEnregistrement=0;
						try {
							//récupération de l'identifiant de la ligne à supprimer
							numeroEnregistrement=jObject.getInt("numeroEnregistrement");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//suppression de la ligne concernée
						Lieu lieu = new Lieu();
						lieu.set_idLieu(numeroEnregistrement);
						LieuDS.deleteLieu(lieu);
					}
					else 
					{
						//récupération de l'entree Ajouterlieu mise à jour 
						Lieu lieu= null;
						try {
							lieu = new Lieu(jObject.getInt("idLieu"),jObject.getInt("idUtilisateur"),jObject.getInt("idDepartement"), jObject.getString("titre"), jObject.getDouble("longitude"), jObject.getDouble("latitude"), jObject.getString("dateCreation"),Base64.decode(jObject.getString("imageLieu"), Base64.DEFAULT));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (status==1)
						{
							//Log.e("photo_size1", lieu.get_Picture().length+"");
							LieuDS.updateLieu(lieu);
						}
						if (status==2)
						{
							LieuDS.createLieu(lieu);
						}
					}
				}
			}
			//LieuDS.close();
		}
	}

	public void update_db()
	{
		update_places();
		update_users();
		update_params_user();
		update_groups();
	}

	public void update_users()
	{
		final String all_users_file= Groovieparams.DBurl+"lister_les_utilisateurs.php";

		final int mode_update=((UtilisateurDS.getAllUtilisateurs().size()==1) ? 0 : 1);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("mode_update", String.valueOf(mode_update)));
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(all_users_file);
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
				Log.e("log_error", result);
				try{
					JSONArray jArray = new JSONArray(result);
					final List<Object> l=new ArrayList<Object>();
					int i;
					for (i=0;i<jArray.length();i++)
					{
						l.add(jArray.getJSONObject(i));

					}
					load_data(liste_actions[1],l);

				}catch(JSONException e){
					Log.e("log_tag1", "Error parsing data " + e.toString());
				}


			}
		}).start();
	}

	public void update_places()
	{
		final String all_places_file=Groovieparams.DBurl+"lister_les_lieux.php";

		final int mode_update=((LieuDS.getAllLieux().size()==0) ? 0 : 1);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("mode_update", String.valueOf(mode_update)));
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(all_places_file);
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
				Log.e("result", result);
				try{
					JSONArray jArray = new JSONArray(result);
					final List<Object> l=new ArrayList<Object>();
					int i;
					for (i=0;i<jArray.length();i++)
					{
						l.add(jArray.getJSONObject(i));

					}
					load_data(liste_actions[0],l);
				}catch(JSONException e){
					Log.e("log_tag2", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}

	public void update_params_user()
	{
		final String all_params_users_file=Groovieparams.DBurl+"lister_paramsUtilisateur.php";

		final int mode_update=((ParamsUtilisateurDS.getAllEntrees().size()<=1) ? 0 : 1);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("mode_update", String.valueOf(mode_update)));
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(all_params_users_file);
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
				Log.e("get groupes", mode_update+" ; "+result);
				try{
					JSONArray jArray = new JSONArray(result);
					final List<Object> l=new ArrayList<Object>();
					int i;
					for (i=0;i<jArray.length();i++)
					{
						l.add(jArray.getJSONObject(i));
					}
					load_data(liste_actions[3],l);
				}catch(JSONException e){
					Log.e("log_tag3", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}

	public void update_groups()
	{
		final String all_groups_file=Groovieparams.DBurl+"lister_les_groupes.php";

		final int mode_update=((GroupeDS.getAllGroupes().size()<=1) ? 0 : 1);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("mode_update", String.valueOf(mode_update)));
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(all_groups_file);
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
				//Log.e("log_error", result);
				try{
					JSONArray jArray = new JSONArray(result);
					final List<Object> l=new ArrayList<Object>();
					int i;
					for (i=0;i<jArray.length();i++)
					{
						l.add(jArray.getJSONObject(i));
					}
					load_data(liste_actions[2],l);
				}catch(JSONException e){
					Log.e("log_tag1333", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}

			}
		}).start();
	}
}
