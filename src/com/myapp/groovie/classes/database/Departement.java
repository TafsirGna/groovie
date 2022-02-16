package com.myapp.groovie.classes.database;

public class Departement {

	private int idDepartement;
	private String libelleDepartement;
	
	public Departement()
	{super();}
	
	public Departement(int idDepartement, String libelleDepartement)
	{
		//super();
		this.idDepartement=idDepartement;
		this.libelleDepartement=libelleDepartement;
	}
	public int get_idDepartement()
	{
		return this.idDepartement;
	}
	public void set_idDepartement(int idDepartement)
	{
		this.idDepartement=idDepartement;
	}
	
	public void set_libelleDepartement(String libelleDepartement)
	{
		this.libelleDepartement=libelleDepartement;
	}
	public String get_libelleDepartement()
	{
		return this.libelleDepartement;
	}
	public boolean matches(Departement departement)
	{
		if (departement.get_idDepartement()==this.get_idDepartement())
		{
			return true;
		}
		return false;
	}
}
