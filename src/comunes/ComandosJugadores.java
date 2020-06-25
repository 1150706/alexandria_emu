package comunes;

import objetos.Accion;
import objetos.Personaje;

public class ComandosJugadores {
    public static int _id;
    public static String _nombre;
    public static int _accion;
    public static String _argumento;
    public static String _habilitado;
    public static Accion _accionJugador;

    public ComandosJugadores(int id, String nombre, int accion, String argumento, String habilitado) {
        _id = id;
        _nombre = nombre;
        _accion = accion;
        _argumento = argumento;
        _habilitado = habilitado;
        _accionJugador = new Accion(getAccion(),getArgumento(),"");
    }

    public int getID() { return _id; }

    public String getNombre() { return _nombre; }

    public int getAccion() { return _accion; }

    public String getArgumento() { return _argumento; }

    public String getHabilitado() { return _habilitado; }

    public void TipoAcciones(Personaje perso) {
        if(perso == null)return;
        if(perso.getCuenta().getJuegoThread() == null) return;
        _accionJugador.apply(perso,null,-1,-1);
    }
}
