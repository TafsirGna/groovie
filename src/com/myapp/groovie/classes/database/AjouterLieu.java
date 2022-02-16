package com.myapp.groovie.classes.database;

public class AjouterLieu {

	private int idUtilisateur;
	private int idLieu;
	private String dateAjout;
	
	public AjouterLieu()
	{ super();}
	
	public AjouterLieu(int idUtilisateur, int idLieu, String dateAjout)
	{
		this.idUtilisateur=idUtilisateur;
		this.idLieu=idLieu;
		this.dateAjout=dateAjout;
	}
	
	public int get_idUtilisateur()
	{
		return this.idUtilisateur;
	}
	public void set_idUtilisateur(int idUtilisateur)
	{
		this.idUtilisateur=idUtilisateur;
	}
	
	public int get_idLieu()
	{
		return this.idLieu;
	}
	
	public void set_idLieu(int idLieu)
	{
		this.idLieu=idLieu;
	}
	
	public String get_dateAjout()
	{
		return this.dateAjout;
	}
	public void set_dateAjout(String dateAjout)
	{
		this.dateAjout=dateAjout;
	}
	public boolean matches(AjouterLieu entree)
	{
		if (entree.get_idUtilisateur()==this.get_idUtilisateur() && entree.get_idLieu()==this.get_idLieu())
		{
			return true;
		}
		return false;
	}
}
