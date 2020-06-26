package objetos

class Animaciones(val id: Int, val idanimacion: Int, val nombre: String, val area: Int, val accion: Int, val size: Int) {

    companion object {
        @kotlin.jvm.JvmStatic
		fun prepararparaGA(animacion: Animaciones): String {
            return animacion.idanimacion.toString() + "," + animacion.area + "," + animacion.accion + "," + animacion.size
        }
    }
}