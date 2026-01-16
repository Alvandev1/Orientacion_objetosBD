package actividad;

//Clase Jugador

public class Jugador 
{

//Variables
	
private final String nombre;
private final int dorsal;
private final String demarcacion;
private final String nacimiento;

//Constructor

	public Jugador(String nombre, int dorsal, String demarcacion, String nacimiento)
	{
	this.nombre = nombre;
	this.dorsal = dorsal;
	this.demarcacion = demarcacion;
	this.nacimiento = nacimiento;
	}
//Getters y Setters
	public String getNombre()
	{
	return nombre;
	}

	public int getDorsal()
	{
	return dorsal;
	}

	public String getDemarcacion()
	{
	return demarcacion;
	}

	public String getNacimiento()
	{
	return nacimiento;
	}
	
//ToString
	@Override
	public String toString()
	{
		return nombre + "(dorsal" + dorsal + ")\n" +
				"Demarcación: " + demarcacion + " | Nacimiento: " + nacimiento;
	}
}