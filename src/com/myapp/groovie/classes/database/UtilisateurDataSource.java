package com.myapp.groovie.classes.database;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;


public class UtilisateurDataSource extends DataSource{

	private String[] allColumns = {MySQLiteHelper.ID_UTILISATEUR,MySQLiteHelper.ID_DEPARTEMENT,MySQLiteHelper.ID_GROUPE,MySQLiteHelper.ID_PARAMS,
			MySQLiteHelper.PSEUDO,MySQLiteHelper.EMAIL,MySQLiteHelper.TELEPHONE,MySQLiteHelper.PHOTO,
			MySQLiteHelper.IDDEVICE,MySQLiteHelper.PASSWORD,MySQLiteHelper.CLE,MySQLiteHelper.ACTIF};
	public UtilisateurDataSource(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public long createUtilisateur(Utilisateur utilisateur) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_UTILISATEUR, utilisateur.getIdUtilisateur());
		values.put(MySQLiteHelper.ID_DEPARTEMENT, utilisateur.getIdDepartement());
		values.put(MySQLiteHelper.ID_GROUPE, utilisateur.getIdGroupe());
		values.put(MySQLiteHelper.ID_PARAMS, utilisateur.get_idParam());
		values.put(MySQLiteHelper.PSEUDO, utilisateur.getPseudo());
		values.put(MySQLiteHelper.EMAIL, utilisateur.getEmail());
		values.put(MySQLiteHelper.TELEPHONE, utilisateur.getTelephone());
		values.put(MySQLiteHelper.IDDEVICE, utilisateur.getIdDevice());
		values.put(MySQLiteHelper.PASSWORD, utilisateur.getPassword());
		values.put(MySQLiteHelper.PHOTO, utilisateur.getPhoto());
		values.put(MySQLiteHelper.CLE, utilisateur.getcleActivation());
		values.put(MySQLiteHelper.ACTIF, utilisateur.getactif());
		long insertUtilisateur = database.insert(MySQLiteHelper.TABLE_UTILISATEUR, null,
				values);
		//Log.e("i", String.valueOf(insertUtilisateur));
		return insertUtilisateur;
	}

	public void deleteUtilisateur(Utilisateur Utilisateur) {
		int idUtilisateur = Utilisateur.getIdUtilisateur();
		database.delete(MySQLiteHelper.TABLE_UTILISATEUR, MySQLiteHelper.ID_UTILISATEUR
				+ " = " + idUtilisateur, null);
	}

	public int getNombresUtilisateur(){
		final List<Utilisateur> values= this.getAllUtilisateurs();
		return values.size();
	}

	public void updateUtilisateur(Utilisateur utilisateur){

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_DEPARTEMENT, utilisateur.getIdDepartement());
		values.put(MySQLiteHelper.PSEUDO, utilisateur.getPseudo());
		values.put(MySQLiteHelper.EMAIL, utilisateur.getEmail());
		values.put(MySQLiteHelper.TELEPHONE, utilisateur.getTelephone());
		values.put(MySQLiteHelper.IDDEVICE, utilisateur.getIdDevice());
		values.put(MySQLiteHelper.PHOTO, utilisateur.getPhoto());
		values.put(MySQLiteHelper.ACTIF, utilisateur.getactif());
		database.update(MySQLiteHelper.TABLE_UTILISATEUR, values, MySQLiteHelper.ID_UTILISATEUR  + " = ?", new String[]
				{String.valueOf(utilisateur.getIdUtilisateur())});

	}

	public List<Utilisateur> getAllUtilisateurs() {
		List<Utilisateur> utilisateurs = new ArrayList<Utilisateur>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_UTILISATEUR,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Utilisateur utilisateur = cursorToUtilisateur(cursor);
			utilisateurs.add(utilisateur);
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return utilisateurs;
	}


	private Utilisateur cursorToUtilisateur(Cursor cursor) {
		Utilisateur utilisateur = new Utilisateur();
		utilisateur.setIdUtilisateur(cursor.getInt(0));
		utilisateur.setIdDepartement(cursor.getInt(1));
		utilisateur.setIdGroupe(cursor.getInt(2));
		utilisateur.set_idParam(cursor.getInt(3));
		utilisateur.setPseudo(cursor.getString(4));
		utilisateur.setEmail(cursor.getString(5));
		utilisateur.setTelephone(cursor.getString(6));
		utilisateur.setPhoto(cursor.getBlob(7));
		utilisateur.setIdDevice(cursor.getString(8));
		utilisateur.setPassword(cursor.getString(9));
		utilisateur.setcleActivation(cursor.getString(10));
		utilisateur.setActif(cursor.getInt(11));
		return utilisateur;
	}
	public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight)
	{
		return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
	}
	public void insertPicture(Utilisateur utilisateur,Bitmap bmp)
	{
		Bitmap rbmp=getResizedBitmap(bmp, 160,160);
		ContentValues values=new ContentValues();
		//database.insert(MySQLiteHelper.TABLE_UTILISATEUR, null,values);
		values.put(MySQLiteHelper.ID_DEPARTEMENT, utilisateur.getIdDepartement());
		values.put(MySQLiteHelper.PSEUDO, utilisateur.getPseudo());
		values.put(MySQLiteHelper.EMAIL, utilisateur.getEmail());
		values.put(MySQLiteHelper.TELEPHONE, utilisateur.getTelephone());
		if (bmp!=null)
		{
			ByteArrayOutputStream stream= new ByteArrayOutputStream();
			rbmp.compress(CompressFormat.PNG, 100, stream);
			values.put(MySQLiteHelper.PHOTO, stream.toByteArray());
		}
		values.put(MySQLiteHelper.IDDEVICE, utilisateur.getIdDevice());
		database.update(MySQLiteHelper.TABLE_UTILISATEUR, values, MySQLiteHelper.ID_UTILISATEUR  + " = ?", new String[]
				{String.valueOf(utilisateur.getIdUtilisateur())});
	}
	
	public boolean hasAlreadySaved(Utilisateur user, List<Utilisateur> liste)
	{
		int size=liste.size();
		for (int i=0;i<size;i++)
		{
			if (liste.get(i).matches(user))
			{
				return true;
			}
		}
		return false;
	}

	public void deleteAllUtilisateurs()
	{
		List<Utilisateur> listeUtilisateurs= getAllUtilisateurs();
		for (int i=0;i<listeUtilisateurs.size();i++)
		{
			Utilisateur utilisateur= listeUtilisateurs.get(i);
			deleteUtilisateur(utilisateur);
		}
		Log.e("sup user", "yes");
	}
	public Cursor get_group_members_of(int idGroupe)
	{
		String req="select "+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_DEPARTEMENT+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_GROUPE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PSEUDO+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.EMAIL+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.TELEPHONE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PHOTO+
				" from "+MySQLiteHelper.TABLE_UTILISATEUR+","+MySQLiteHelper.TABLE_PARTICIPER+
				" where "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_UTILISATEUR+"="+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_UTILISATEUR+
				" and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_GROUPE+"= ? and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEENTRE+" <> ?";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(idGroupe), "1900-01-01 00:00:00"});
		return c;
	}
	
	public Utilisateur get_utilisateur(int id_utilisateur)
	{
		List<Utilisateur> listeUtilisateurs= getAllUtilisateurs();
		for (int i=0;i<listeUtilisateurs.size();i++)
		{
			if (listeUtilisateurs.get(i).getIdUtilisateur()==id_utilisateur)
				return listeUtilisateurs.get(i);
		}
		return null;
	}
}
