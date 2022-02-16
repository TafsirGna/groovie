package com.myapp.groovie.classes.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class GroupeDataSource extends DataSource{

	private String[] allColumns = {MySQLiteHelper.ID_GROUPE,MySQLiteHelper.ID_UTILISATEUR	};

	public GroupeDataSource(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public long createGroupe(Groupe groupe) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_GROUPE, groupe.get_idGroupe());
		values.put(MySQLiteHelper.ID_UTILISATEUR, groupe.get_idUtilisateur());
		long insertGroupe = database.insert(MySQLiteHelper.TABLE_GROUPE, null,values);
		//Log.e("i", String.valueOf(insertUtilisateur));
		return insertGroupe;
	}
	public void deleteGroupe(Groupe groupe) {
		int idGroupe=groupe.get_idGroupe();
		database.delete(MySQLiteHelper.TABLE_GROUPE, MySQLiteHelper.ID_GROUPE
				+ " = " + idGroupe, null);
	}

	public int getNombreGroupes(){
		final List<Groupe> values= this.getAllGroupes();
		return values.size();
	}
	/*
	public void updateGroupe(Groupe groupe){

		ContentValues values = new ContentValues();
		database.update(MySQLiteHelper.TABLE_GROUPE, values, MySQLiteHelper.ID_GROUPE  + " = ?", new String[]
				{String.valueOf(groupe.get_idGroupe())});
	}
	 */
	public List<Groupe> getAllGroupes() {
		List<Groupe> groupes = new ArrayList<Groupe>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUPE,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Groupe groupe = cursorToGroupe(cursor);
			groupes.add(groupe);
			Log.e("groupe entries", groupe.get_idGroupe()+" ; "+ groupe.get_idUtilisateur());
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return groupes;
	}

	private Groupe cursorToGroupe(Cursor cursor) {
		Groupe groupe = new Groupe();
		groupe.set_idGroupe(cursor.getInt(0));
		groupe.set_idUtilisateur(cursor.getInt(1));
		return groupe;
	}

	public boolean hasAlreadySaved(Groupe groupe, List<Groupe> liste)
	{
		int size=liste.size();
		for (int i=0;i<size;i++)
		{
			if (liste.get(i).matches(groupe))
			{
				return true;
			}
		}
		return false;
	}

	public void deleteAllGroupes()
	{
		List<Groupe> listeGroupes= getAllGroupes();
		for (int i=0;i<listeGroupes.size();i++)
		{
			Groupe groupe= listeGroupes.get(i);
			deleteGroupe(groupe);
		}
		Log.e("sup group", "yes");
	}

	public Groupe get_groupe(int id_groupe)
	{
		List<Groupe> liste= getAllGroupes();
		int listeSize= liste.size();
		for (int i=0;i<listeSize;i++)
		{
			if (liste.get(i).get_idGroupe()==id_groupe)
				return liste.get(i);
		}
		return null;
	}

	public Cursor get_others_groups_of(int idUtilisateur)
	{
		String req="select "+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_DEPARTEMENT+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_GROUPE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PSEUDO+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.EMAIL+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.TELEPHONE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PHOTO+
				" from "+MySQLiteHelper.TABLE_UTILISATEUR+", "+MySQLiteHelper.TABLE_GROUPE+","+MySQLiteHelper.TABLE_PARTICIPER+
				" where "+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_GROUPE+"="+MySQLiteHelper.TABLE_GROUPE+"."+MySQLiteHelper.ID_GROUPE+" and "+MySQLiteHelper.TABLE_GROUPE+"."+MySQLiteHelper.ID_GROUPE+"="+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_GROUPE+" and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_UTILISATEUR+"= ? and "+MySQLiteHelper.DATEENTRE+"<> ?";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(idUtilisateur), "1900-01-01 00:00:00"});
		return c;
	}

	public int get_nombre_followers(int id_utilisateur)
	{
		int resultat=0;
		String req="select count(*)"+
				" from "+MySQLiteHelper.TABLE_GROUPE+","+MySQLiteHelper.TABLE_PARTICIPER+
				" where "+MySQLiteHelper.TABLE_GROUPE+"."+MySQLiteHelper.ID_GROUPE+" =  " + MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_GROUPE+
				" and "+MySQLiteHelper.TABLE_GROUPE+"."+MySQLiteHelper.ID_UTILISATEUR+" = ? and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEENTRE+" <> '1900-01-01 00:00:00'";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_utilisateur)});
		c.moveToFirst();
		while (!c.isAfterLast()) {
			resultat=c.getInt(0);
			c.moveToNext();
		}
		return resultat;
	}

	public Cursor get_notifs_groupe(int id_utilisateur)
	{
		String req="select "+MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.TITRE+","+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.PRIXMODIFIE+","+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.DATE_MODIFICATION+
				" from "+MySQLiteHelper.TABLE_INFOSDULIEU+","+MySQLiteHelper.TABLE_LIEU+
				" where "+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.ID_LIEU+" =  " + MySQLiteHelper.TABLE_LIEU+"."+MySQLiteHelper.ID_LIEU+
				" and "+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.ID_UTILISATEUR+" = ?";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_utilisateur)});
		return c;
	}
}
