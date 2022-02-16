package com.myapp.groovie.classes.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class LieuDataSource extends DataSource{

	private String[] allColumns = {MySQLiteHelper.ID_LIEU,MySQLiteHelper.ID_UTILISATEUR,MySQLiteHelper.ID_DEPARTEMENT,
			MySQLiteHelper.TITRE,MySQLiteHelper.LONGITUDE,MySQLiteHelper.LATITUDE,
			MySQLiteHelper.DATECREATION,MySQLiteHelper.PICTURE};

	public LieuDataSource(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public long createLieu(Lieu lieu) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_LIEU, lieu.get_idLieu());
		values.put(MySQLiteHelper.ID_UTILISATEUR, lieu.get_idUtilisateur());
		values.put(MySQLiteHelper.ID_DEPARTEMENT, lieu.get_idDepartement());
		values.put(MySQLiteHelper.TITRE, lieu.get_titre());
		values.put(MySQLiteHelper.LONGITUDE, lieu.get_longitude());
		values.put(MySQLiteHelper.LATITUDE, lieu.get_latitude());
		values.put(MySQLiteHelper.DATECREATION, lieu.get_dateCreation().toString());
		//values.put(MySQLiteHelper.PHOTO, lieu.get_Picture());
		values.put(MySQLiteHelper.PICTURE, lieu.get_Picture());
		long insertLieu = database.insert(MySQLiteHelper.TABLE_LIEU, null,
				values);
		return insertLieu;
	}

	public void deleteLieu(Lieu lieu) {
		int idLieu=lieu.get_idLieu();
		database.delete(MySQLiteHelper.TABLE_LIEU, MySQLiteHelper.ID_LIEU
				+ " = " + idLieu, null);
	}

	public int getNombreLieux(){
		final List<Lieu> values= this.getAllLieux();
		return values.size();
	}

	public void updateLieu(Lieu lieu){

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.TITRE, lieu.get_titre());
		values.put(MySQLiteHelper.LONGITUDE, lieu.get_longitude());
		values.put(MySQLiteHelper.LATITUDE, lieu.get_latitude());
		values.put(MySQLiteHelper.DATECREATION, lieu.get_dateCreation().toString());
		//values.put(MySQLiteHelper.PHOTO, lieu.get_Picture());
		values.put(MySQLiteHelper.PICTURE, lieu.get_Picture());
		database.update(MySQLiteHelper.TABLE_LIEU, values, MySQLiteHelper.ID_LIEU  + " = ?", new String[]
				{String.valueOf(lieu.get_idLieu())});

	}

	public List<Lieu> getAllLieux() {
		List<Lieu> lieux = new ArrayList<Lieu>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_LIEU,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Lieu lieu = cursorToLieu(cursor);
			lieux.add(lieu);
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return lieux;
	}

	private Lieu cursorToLieu(Cursor cursor) {
		Lieu lieu = new Lieu();
		lieu.set_idLieu(cursor.getInt(0));
		lieu.set_idUtilisateur(cursor.getInt(1));
		lieu.set_idDepartement(cursor.getInt(2));
		lieu.set_titre(cursor.getString(3));
		lieu.set_longitude(cursor.getDouble(4));
		lieu.set_latitude(cursor.getDouble(5));
		lieu.set_dateCreation(cursor.getString(6));
		lieu.set_Picture(cursor.getBlob(7));
		return lieu;
	}

	public boolean hasAlreadySaved(Lieu lieu,List<Lieu> liste)
	{
		int listeSize=liste.size();
		for (int i=0;i<listeSize;i++)
		{
			if (liste.get(i).matches(lieu))
			{
				return true;
			}
		}
		return false;
	}
	public Lieu get_Lieu(int idLieu)
	{
		Cursor cursor = database.query(MySQLiteHelper.TABLE_LIEU,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) 
		{
			Lieu lieu = cursorToLieu(cursor);
			if (lieu.get_idLieu()==idLieu)
			{
				return lieu;
			}
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return null;
	}
	public void deleteAllLieux()
	{
		List<Lieu> listeLieux= getAllLieux();
		for (int i=0;i<listeLieux.size();i++)
		{
			Lieu lieu= listeLieux.get(i);
			deleteLieu(lieu);
		}
		Log.e("sup lieu", "yes");
	}

	public Cursor get_places_of(int id_utilisateur)
	{
		String req="select "+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.ID_LIEU+","+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.ID_DEPARTEMENT+","+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.TITRE+","+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.LATITUDE+","+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.LONGITUDE+","+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.DATECREATION+","+MySQLiteHelper.TABLE_AJOUTER_LIEU+"."+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.TABLE_AJOUTER_LIEU+"."+MySQLiteHelper.DATEAJOUTLIEU+","+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.PICTURE+
				" from "+MySQLiteHelper.TABLE_AJOUTER_LIEU+","+MySQLiteHelper.TABLE_LIEU+
				" where "+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.ID_LIEU+"="+MySQLiteHelper.TABLE_AJOUTER_LIEU+"."+MySQLiteHelper.ID_LIEU+" and "+MySQLiteHelper.TABLE_AJOUTER_LIEU+"."+MySQLiteHelper.ID_UTILISATEUR+" = ?";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_utilisateur)});
		return c;
	}
	
	public int get_nombre_Abonnees(int id_lieu)
	{
		int resultat=0;
		String req="select count(*)"+
				" from "+MySQLiteHelper.TABLE_LIEU+","+MySQLiteHelper.TABLE_AJOUTER_LIEU+
				" where "+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.ID_LIEU+" = " +MySQLiteHelper.TABLE_AJOUTER_LIEU+"."+MySQLiteHelper.ID_LIEU+
				" and "+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.ID_LIEU+" = ?";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_lieu)});
		c.moveToFirst();
		while (!c.isAfterLast()) {
			resultat=c.getInt(0);
			c.moveToNext();
		}
		return resultat;
	}
}
