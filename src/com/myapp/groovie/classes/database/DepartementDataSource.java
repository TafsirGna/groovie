package com.myapp.groovie.classes.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class DepartementDataSource extends DataSource{

	private String[] allColumns = {MySQLiteHelper.ID_DEPARTEMENT,MySQLiteHelper.LIBELLEDEPARTEMENT};
	
	public DepartementDataSource(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public long createDepartement(Departement departement) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_DEPARTEMENT, departement.get_idDepartement());
		values.put(MySQLiteHelper.LIBELLEDEPARTEMENT, departement.get_libelleDepartement());
		long insertDepartement = database.insert(MySQLiteHelper.TABLE_DEPARTEMENT, null,
				values);
		//Log.e("i", String.valueOf(insertUtilisateur));
		return insertDepartement;
	}
	
	public void deleteDepartement(Departement departement) {
		int idDepartement=departement.get_idDepartement();
		database.delete(MySQLiteHelper.TABLE_DEPARTEMENT, MySQLiteHelper.ID_DEPARTEMENT
				+ " = " + idDepartement, null);
	}

	public int getNombreDepartements(){
		final List<Departement> values= this.getAllDepartements();
		return values.size();
	}
	
	public void updateDepartement(Departement departement){

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.LIBELLEDEPARTEMENT, departement.get_libelleDepartement());
		database.update(MySQLiteHelper.TABLE_DEPARTEMENT, values, MySQLiteHelper.ID_DEPARTEMENT + " = ?", new String[]
				{String.valueOf(departement.get_idDepartement())});

	}
	
	public List<Departement> getAllDepartements() {
		List<Departement> departements = new ArrayList<Departement>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_DEPARTEMENT,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Departement departement = cursorToDepartement(cursor);
			departements.add(departement);
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return departements;
	}
	
	private Departement cursorToDepartement(Cursor cursor) {
		Departement departement = new Departement();
		departement.set_idDepartement(cursor.getInt(0));
		departement.set_libelleDepartement(cursor.getString(1));
		return departement;
	}
	public boolean hasAlreadySaved(Departement departement, List<Departement> liste)
	{
		int size=liste.size();
		for (int i=0;i<size;i++)
		{
			if (liste.get(i).matches(departement))
			{
				return true;
			}
		}
		return false;
	}
	
	public void deleteAllDepartements()
	{
		List<Departement> listeDepartements= getAllDepartements();
		for (int i=0;i<listeDepartements.size();i++)
		{
			Departement departement= listeDepartements.get(i);
			deleteDepartement(departement);
		}
		Log.e("sup depart", "yes");
	}
	public Departement get_departement(int id_departement, List<Departement> liste)
	{
		int listeSize=liste.size();
		for (int i=0;i<listeSize;i++)
		{
			if (liste.get(i).get_idDepartement()==id_departement)
				return liste.get(i);
		}
		return null;
	}
}
