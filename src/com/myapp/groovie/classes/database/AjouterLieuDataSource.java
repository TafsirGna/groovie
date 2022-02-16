package com.myapp.groovie.classes.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class AjouterLieuDataSource extends DataSource{

	private String[] allColumns = {MySQLiteHelper.ID_UTILISATEUR,MySQLiteHelper.ID_LIEU,
			MySQLiteHelper.DATEAJOUTLIEU};

	public AjouterLieuDataSource(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public long createEntree(AjouterLieu entree) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_UTILISATEUR, entree.get_idUtilisateur());
		values.put(MySQLiteHelper.ID_LIEU, entree.get_idLieu());
		values.put(MySQLiteHelper.DATEAJOUTLIEU, entree.get_dateAjout());
		long insertAjouterLieu = database.insert(MySQLiteHelper.TABLE_AJOUTER_LIEU, null,
				values);
		//Log.e("i", String.valueOf(insertUtilisateur));
		return insertAjouterLieu;
	}
	
	public void deleteEntree(AjouterLieu entree) {
		int idLieu=entree.get_idLieu();
		database.delete(MySQLiteHelper.TABLE_AJOUTER_LIEU, MySQLiteHelper.ID_LIEU
				+ " = " + idLieu, null);
	}

	public int getNombreEntrees(){
		final List<AjouterLieu> values= this.getAllEntrees();
		return values.size();
	}

	public List<AjouterLieu> getAllEntrees() {
		List<AjouterLieu> Entrees = new ArrayList<AjouterLieu>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_AJOUTER_LIEU,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			AjouterLieu entree = cursorToEntree(cursor);
			Entrees.add(entree);
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return Entrees;
	}

	private AjouterLieu cursorToEntree(Cursor cursor) {
		AjouterLieu entree = new AjouterLieu();
		entree.set_idUtilisateur(cursor.getInt(0));
		entree.set_idLieu(cursor.getInt(1));
		entree.set_dateAjout(cursor.getString(2));
		return entree;
	}

	public boolean hasAlreadySaved(AjouterLieu entree, List<AjouterLieu> liste)
	{
		int size=liste.size();
		for (int i=0;i<size;i++)
		{
			if (liste.get(i).matches(entree))
			{
				return true;
			}
		}
		return false;
	}

	public void deleteAllEntrees()
	{
		List<AjouterLieu> listeEntrees= getAllEntrees();
		for (int i=0;i<listeEntrees.size();i++)
		{
			AjouterLieu entree= listeEntrees.get(i);
			deleteEntree(entree);
		}
		Log.e("sup ajouter", "yes");
	}

	public int get_nombre_favoris(int id_utilisateur)
	{
		int resultat=0;
		String req="select count(*)"+
				" from "+MySQLiteHelper.TABLE_AJOUTER_LIEU+
				" where "+MySQLiteHelper.ID_UTILISATEUR+" = ?";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_utilisateur)});
		c.moveToFirst();
		resultat=c.getInt(0);
		return resultat;
	}
}
