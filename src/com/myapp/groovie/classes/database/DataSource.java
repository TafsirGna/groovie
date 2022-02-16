package com.myapp.groovie.classes.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public abstract class DataSource {

	 // Champs de la base de données
	protected SQLiteDatabase database;
	protected MySQLiteHelper dbHelper;
	  
	  public DataSource(Context context) {
		  dbHelper = new MySQLiteHelper(context);
	  }
	  
	  public void open() throws SQLException {
		  database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
		  dbHelper.close();
	  }
	
}
