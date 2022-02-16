package com.myapp.groovie.classes.database;

public class ParamsUtilisateur {

	private int idParams;
	private int idUtilisateur;
	private int periode;
	private int visibilitePhoto;
	private int visibiliteCoordonnees;
	private int visibiliteStatistiques;
	
	public ParamsUtilisateur()
	{
		super();
	}
	public ParamsUtilisateur(int idParams, int idUtilisateur, int periode, int visibilitePhoto, int visibiliteCoordonnees, int visibiliteStatistiques)
	{
		this.idParams=idParams;
		this.idUtilisateur= idUtilisateur;
		this.periode=periode;
		this.visibiliteCoordonnees=visibiliteCoordonnees;
		this.visibilitePhoto=visibilitePhoto;
		this.visibiliteStatistiques= visibiliteStatistiques;
	}
	public void set_idParams(int idParams)
	{
		this.idParams=idParams;
	}
	public void set_idUtilisateur(int idUtilisateur)
	{
		this.idUtilisateur=idUtilisateur;
	}
	public void set_periode(int periode)
	{
		this.periode=periode;
	}
	public void set_visibilitePhoto(int visibilitePhoto)
	{
		this.visibilitePhoto=visibilitePhoto;
	}
	public void set_visibiliteCoordonnees(int visibiliteCoordonnees)
	{
		this.visibiliteCoordonnees=visibiliteCoordonnees;
	}
	public void set_visibiliteStatistiques(int visibiliteStatistiques)
	{
		this.visibiliteStatistiques=visibiliteStatistiques;
	}
	public int get_idParams()
	{
		return this.idParams;
	}
	public int get_idUtilisateur()
	{
		return this.idUtilisateur;
	}
	public int get_periode()
	{
		return this.periode;
	}
	public int get_visibilitePhoto()
	{
		return this.visibilitePhoto;
	}
	public int get_visibiliteStatistiques()
	{
		return this.visibiliteStatistiques;
	}
	public int get_visibiliteCoordonnees()
	{
		return this.visibiliteCoordonnees;
	}
	public boolean matches(ParamsUtilisateur params)
	{
		if (params.get_idParams()==this.get_idParams())
		{
			return true;
		}
		return false;
	}
}
