package com.myapp.groovie.classes.database;


public class Lieu {

	private int idLieu;
	private int idUtilisateur;
	private int idDepartement;
	private String titre;
	private double longitude;
	private double latitude;
	private String dateCreation;
	private byte[] picture;

	public Lieu()
	{
		super();
	}

	public Lieu(int idLieu,int idUtilisateur,int idDepartement,String titre, double longitude, double latitude, String dateCreation, byte[] picture)
	{
		this.idLieu=idLieu;
		this.idUtilisateur=idUtilisateur;
		this.idDepartement=idDepartement;
		this.titre=titre;
		this.longitude=longitude;
		this.latitude=latitude;
		this.dateCreation=dateCreation;
		this.picture=picture;
	}
	public void set_Picture(byte[] picture)
	{
		this.picture=picture;
	}
	public byte[] get_Picture()
	{
		return this.picture;
	}
	public int get_idLieu()
	{
		return this.idLieu;
	}
	public void set_idLieu(int idLieu)
	{
		this.idLieu=idLieu;
	}
	public int get_idUtilisateur()
	{
		return this.idUtilisateur;
	}
	public void set_idUtilisateur(int idUtilisateur)
	{
		this.idUtilisateur=idUtilisateur;
	}
	public int get_idDepartement()
	{
		return this.idDepartement;
	}
	public void set_idDepartement(int idDepartement)
	{
		this.idDepartement=idDepartement;
	}
	public String get_titre()
	{
		return this.titre;
	}
	public void set_titre(String titre)
	{
		this.titre=titre;
	}
	public double get_longitude()
	{
		return this.longitude;
	}
	public void set_longitude(double longitude)
	{
		this.longitude=longitude;
	}
	public double get_latitude()
	{
		return this.latitude;
	}
	public void set_latitude(double latitude)
	{
		this.latitude=latitude;
	}
	public void set_dateCreation(String dateCreation)
	{
		this.dateCreation=dateCreation;
	}
	public String get_dateCreation()
	{
		return this.dateCreation;
	}
	
	public boolean matches(Lieu lieu)
	{
		if (lieu.get_idLieu()==this.get_idLieu())
		{
			return true;
		}
		return false;
	}

}
