package comunes

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import comunes.MainServidor.addToShopLog
import comunes.MainServidor.closeServers
import comunes.Mundo.*
import juego.JuegoServidor
import objetos.*
import objetos.Gremio.GuildMember
import objetos.Mapa.MountPark
import objetos.Mercadillo.HdvEntry
import objetos.NPCModelo.NPC_question
import objetos.NPCModelo.NPC_reponse
import objetos.Objeto.ObjTemplate
import objetos.casas.Casas
import objetos.casas.Cofres
import objetos.hechizos.Hechizos
import objetos.hechizos.Hechizos.SortStats
import realm.RealmServer.Companion.agregar_a_los_logs
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.system.exitProcess

object GestorSQL {
    private var _dinamicos: Connection? = null
    private var _estaticos: Connection? = null

    @JvmStatic
	@Synchronized
    @Throws(SQLException::class)
    fun ejecutar_consulta(query: String?, DBNAME: String?): ResultSet? {
        if (!MainServidor.isInit) return null
        val DB: Connection? = if (DBNAME == MainServidor.DB_DINAMICOS) _dinamicos else _estaticos
        val stat = DB!!.createStatement()
        val rs = stat.executeQuery(query)
        stat.queryTimeout = 300
        return rs
    }

    @Synchronized
    @Throws(SQLException::class)
    fun nueva_consulta(baseQuery: String?, dbCon: Connection?): PreparedStatement {
        return dbCon!!.prepareStatement(baseQuery)
    }

    @Synchronized
    fun cerrar_consulta() {
        try {
            _dinamicos!!.close()
            _estaticos!!.close()
        } catch (e: Exception) {
            println("Erreur a la fermeture des connexions SQL:" + e.message)
            e.printStackTrace()
        }
    }

    fun iniciar_conexion(): Boolean {
        return try {
            val configDinamica = HikariConfig()
            configDinamica.dataSourceClassName = "org.mariadb.jdbc.MySQLDataSource"
            configDinamica.addDataSourceProperty("serverName", MainServidor.DB_HOST)
            configDinamica.addDataSourceProperty("port", 3306)
            configDinamica.addDataSourceProperty("databaseName", MainServidor.DB_DINAMICOS)
            configDinamica.addDataSourceProperty("user", MainServidor.DB_USUARIO)
            configDinamica.addDataSourceProperty("password", MainServidor.DB_PASS)
            configDinamica.isAutoCommit = true
            configDinamica.maximumPoolSize = 50
            configDinamica.minimumIdle = 1
            configDinamica.poolName = "Dinamicos"
            val configEstatica = HikariConfig()
            configEstatica.dataSourceClassName = "org.mariadb.jdbc.MySQLDataSource"
            configEstatica.addDataSourceProperty("serverName", MainServidor.DB_HOST)
            configEstatica.addDataSourceProperty("port", 3306)
            configEstatica.addDataSourceProperty("databaseName", MainServidor.DB_ESTATICOS)
            configEstatica.addDataSourceProperty("user", MainServidor.DB_USUARIO)
            configEstatica.addDataSourceProperty("password", MainServidor.DB_PASS)
            configEstatica.isAutoCommit = true
            configEstatica.maximumPoolSize = 10
            configEstatica.minimumIdle = 1
            configEstatica.poolName = "Estaticos"
            val dinamicos = HikariDataSource(configDinamica)
            val estaticos = HikariDataSource(configEstatica)
            _dinamicos = dinamicos.connection
            _estaticos = estaticos.connection
            if (!_estaticos!!.isValid(1000) || !_dinamicos!!.isValid(1000)) {
                JuegoServidor.agregar_a_los_logs("SQLError : Connexion a la BD invalide!")
                return false
            }
            true
        } catch (e: SQLException) {
            println("SQL ERROR: " + e.message)
            e.printStackTrace()
            false
        }
    }

    private fun cerrar_resultado(resultado: ResultSet?) {
        try {
            resultado!!.statement.close()
            resultado.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun cerrar_nueva_consulta(p: PreparedStatement?) {
        try {
            p!!.clearParameters()
            p.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun actualizar_datos_cuenta(cuenta: Cuenta) {
        try {
            val consulta = "UPDATE `datos_cuenta` SET `kamasbanco` = ?, `banco` = ?, `nivel` = ?, `baneado` = ?, `amigos` = ?, `enemigos` = ? WHERE `id` = ?;"
            val p = nueva_consulta(consulta, _dinamicos)
            p.setLong(1, cuenta.bankKamas)
            p.setString(2, cuenta.parseBankObjetsToDB())
            p.setInt(3, cuenta.gmlvl)
            p.setInt(4, if (cuenta.isBanned) 1 else 0)
            p.setString(5, cuenta.parseFriendListToDB())
            p.setString(6, cuenta.parseEnemyListToDB())
            p.setInt(7, cuenta.id)
            p.executeUpdate()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_recetas() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM `datos_recetas`;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val m = ArrayList<Couple<Int, Int>>()
                var cont = true
                for (str in RS.getString("recetas").split(";".toRegex()).toTypedArray()) {
                    try {
                        val tID = str.split("\\*".toRegex()).toTypedArray()[0].toInt()
                        val qua = str.split("\\*".toRegex()).toTypedArray()[1].toInt()
                        m.add(Couple(tID, qua))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cont = false
                    }
                }
                //s'il y a eu une erreur de parsing, on ignore cette recette
                if (!cont) continue
                addRecetas(RS.getInt("id"), m)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_publicidades_automaticas() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM `datos_publicidad`;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) Publicidad.add(RS.getString("texto"))
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_retos() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM `datos_retos`;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val chal = RS.getInt("id").toString() + "," +
                        RS.getInt("gananciaxp") + "," +
                        RS.getInt("gananciadrop") + "," +
                        RS.getInt("gananciapormob") + "," +
                        RS.getInt("condiciones")
                addChallenge(chal)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_gremios() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_gremio;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                addGuild(Gremio(
                        RS.getInt("id"),
                        RS.getString("nombre"),
                        RS.getString("emblema"),
                        RS.getInt("nivel"),
                        RS.getLong("experiencia"),
                        RS.getInt("capital"),
                        RS.getInt("recaudadoresmaximos"),
                        RS.getString("hechizos"),
                        RS.getString("caracteristicas")), false)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_miembros_gremio() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_miembros_gremio;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val G = getGuild(RS.getInt("gremio")) ?: continue
                G.addMember(RS.getInt("id"), RS.getInt("rango"), RS.getByte("xpdonada"), RS.getLong("porcentajexp"), RS.getInt("derechos"))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_montura() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_montura;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                addDragopavo(Dragopavo(
                        RS.getInt("id"),
                        RS.getInt("color"),
                        RS.getInt("sexo"),
                        RS.getInt("amor"),
                        RS.getInt("resistencia"),
                        RS.getInt("nivel"),
                        RS.getLong("experiencia"),
                        RS.getString("nombre"),
                        RS.getInt("fatiga"),
                        RS.getInt("energia"),
                        RS.getInt("reproducciones"),
                        RS.getInt("madurez"),
                        RS.getInt("serenidad"),
                        RS.getString("objetos"),
                        RS.getString("ancestros"),
                        RS.getString("habilidad")))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_drops() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_drops;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val MT = getMonstre(RS.getInt("monstruo"))
                MT.addDrop(Drop(
                        RS.getInt("objeto"),
                        RS.getInt("limite"),
                        RS.getFloat("maximo"),
                        RS.getInt("porcentaje")))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_sets() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_objetos_sets;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                addItemSet(ItemSet(
                        RS.getInt("id"),
                        RS.getString("objetos"),
                        RS.getString("bonus")
                ))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_objetos_interactivos() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_objetos_interactivos;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                addIOTemplate(IOTemplate(
                        RS.getInt("id"),
                        RS.getInt("actualizar"),
                        RS.getInt("duracion"),
                        RS.getInt("desconocido"),
                        RS.getInt("caminable") == 1))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_cercados(): Int {
        var nbr = 0
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_cercados;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val map = getCarte(RS.getShort("mapa")) ?: continue
                addMountPark(MountPark(
                        RS.getInt("dueño"), map,
                        RS.getInt("celda"),
                        RS.getInt("tamaño"),
                        RS.getString("monturas"),
                        RS.getInt("gremio"),
                        RS.getInt("precio")))
                nbr++
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
	fun cargar_oficios() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_oficios;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                addJob(Oficio(
                        RS.getInt("id"),
                        RS.getString("herramientas"),
                        RS.getString("recetas")
                ))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_area() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_area;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val A = Area(
                        RS.getInt("id"),
                        RS.getInt("superarea"),
                        RS.getString("nombre"))
                addArea(A)
                //on ajoute la zone au continent
                A._superArea.addArea(A)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_subareas() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_subareas;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val SA = SubArea(
                        RS.getInt("id"),
                        RS.getInt("area"),
                        RS.getInt("alineacion"),
                        RS.getString("nombre"))
                addSubArea(SA)
                //on ajoute la sous zone a la zone
                if (SA._area != null) SA._area.addSubArea(SA)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_npc(): Int {
        var nbr = 0
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_npc;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val map = getCarte(RS.getShort("mapa")) ?: continue
                map.addNpc(RS.getInt("npc"), RS.getInt("celda"), RS.getInt("orientacion"))
                nbr++
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
	fun cargar_recaudadores(): Int {
        var nbr = 0
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_recaudadores;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val map = getCarte(RS.getShort("mapid")) ?: continue
                addPerco(Recaudador(
                        RS.getInt("id"),
                        RS.getShort("mapa"),
                        RS.getInt("celda"),
                        RS.getByte("orientacion"),
                        RS.getInt("gremio"),
                        RS.getShort("N1"),
                        RS.getShort("N2"),
                        RS.getString("objetos"),
                        RS.getLong("kamas"),
                        RS.getLong("experiencia")
                ))
                nbr++
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
	fun cargar_casas(): Int {
        var nbr = 0
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_casas;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val map = getCarte(RS.getShort("mapa")) ?: continue
                addHouse(Casas(
                        RS.getInt("id"),
                        RS.getShort("mapa"),
                        RS.getInt("celda"),
                        RS.getInt("dueño"),
                        RS.getInt("venta"),
                        RS.getInt("gremio"),
                        RS.getInt("acceso"),
                        RS.getString("llave"),
                        RS.getInt("derechosgremio"),
                        RS.getInt("mapa_interior"),
                        RS.getInt("celda_interior")
                ))
                nbr++
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
	fun cargar_cuentas() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_cuenta;", MainServidor.DB_DINAMICOS)
            val baseQuery = "UPDATE datos_cuenta SET `actualizarnecesita` = 0 WHERE id = ?;"
            val p = nueva_consulta(baseQuery, _dinamicos)
            while (RS!!.next()) {
                val C = Cuenta(
                        RS.getInt("id"),
                        RS.getString("cuenta").toLowerCase(),
                        RS.getString("contraseña"),
                        RS.getString("apodo"),
                        RS.getString("pregunta"),
                        RS.getString("respuesta"),
                        RS.getInt("nivel"),
                        RS.getInt("vip"),
                        RS.getInt("baneado") == 1,
                        RS.getString("ultimaip"),
                        RS.getString("ultimafechaconexion"),
                        RS.getString("banco"),
                        RS.getInt("kamasbanco"),
                        RS.getString("amigos"),
                        RS.getString("enemigos"))
                addAccount(C)
                addAccountbyName(C)
                p.setInt(1, RS.getInt("id"))
                p.executeUpdate()
            }
            cerrar_nueva_consulta(p)
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	val siguienteIDPersonaje: Int
        get() {
            try {
                val RS = ejecutar_consulta("SELECT id FROM datos_personajes ORDER BY id DESC LIMIT 1;", MainServidor.DB_DINAMICOS)
                if (!RS!!.first()) return 1
                var guid = RS.getInt("id")
                guid++
                cerrar_resultado(RS)
                return guid
            } catch (e: SQLException) {
                agregar_a_los_logs("SQL ERROR: " + e.message)
                e.printStackTrace()
                closeServers()
            }
            return 0
        }

    @JvmStatic
	fun cargar_personaje_por_cuenta(accID: Int) {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_personajes WHERE cuenta = '$accID';", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val stats = TreeMap<Int, Int>()
                stats[Constantes.STATS_ADD_VITA] = RS.getInt("vitalidad")
                stats[Constantes.STATS_ADD_FORC] = RS.getInt("fuerza")
                stats[Constantes.STATS_ADD_SAGE] = RS.getInt("sabiduria")
                stats[Constantes.STATS_ADD_INTE] = RS.getInt("inteligencia")
                stats[Constantes.STATS_ADD_CHAN] = RS.getInt("suerte")
                stats[Constantes.STATS_ADD_AGIL] = RS.getInt("agilidad")
                val perso = Personaje(
                        RS.getInt("id"),
                        RS.getString("nombre"),
                        RS.getInt("sexo"),
                        RS.getInt("clase"),
                        RS.getInt("color1"),
                        RS.getInt("color2"),
                        RS.getInt("color3"),
                        RS.getLong("kamas"),
                        RS.getInt("puntoshechizo"),
                        RS.getInt("capital"),
                        RS.getInt("energia"),
                        RS.getInt("nivel"),
                        RS.getLong("experiencia"),
                        RS.getInt("tamaño"),
                        RS.getInt("gfx"),
                        RS.getByte("alineacion"),
                        RS.getInt("cuenta"), stats,
                        RS.getByte("veramigos"),
                        RS.getByte("veralineacion"),
                        RS.getByte("vervendedor"),
                        RS.getString("canales"),
                        RS.getShort("mapa"),
                        RS.getInt("celda"),
                        RS.getString("objetos"),
                        RS.getString("objetosmercante"),
                        RS.getInt("puntosdevida"),
                        RS.getString("hechizos"),
                        RS.getString("puntoguardado"),
                        RS.getString("oficios"),
                        RS.getInt("xpmontura"),
                        RS.getInt("montura"),
                        RS.getInt("honor"),
                        RS.getInt("deshonor"),
                        RS.getInt("nivelalineacion"),
                        RS.getString("zaaps"),
                        RS.getByte("titulo"),
                        RS.getInt("esposo"))
                //Vérifications pré-connexion
                perso.VerifAndChangeItemPlace()
                agregar_personaje(perso)
                val guildId = personaje_esta_en_gremio(RS.getInt("id"))
                if (guildId >= 0) {
                    perso.setGuildMember(getGuild(guildId).getMember(RS.getInt("id")))
                }
                if (getCompte(accID) != null) getCompte(accID).addPerso(perso)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
            closeServers()
        }
    }

    @JvmStatic
	fun cargar_personaje() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_personajes;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val stats = TreeMap<Int, Int>()
                stats[Constantes.STATS_ADD_VITA] = RS.getInt("vitalidad")
                stats[Constantes.STATS_ADD_FORC] = RS.getInt("fuerza")
                stats[Constantes.STATS_ADD_SAGE] = RS.getInt("sabiduria")
                stats[Constantes.STATS_ADD_INTE] = RS.getInt("inteligencia")
                stats[Constantes.STATS_ADD_CHAN] = RS.getInt("suerte")
                stats[Constantes.STATS_ADD_AGIL] = RS.getInt("agilidad")
                val perso = Personaje(
                        RS.getInt("id"),
                        RS.getString("nombre"),
                        RS.getInt("sexo"),
                        RS.getInt("clase"),
                        RS.getInt("color1"),
                        RS.getInt("color2"),
                        RS.getInt("color3"),
                        RS.getLong("kamas"),
                        RS.getInt("puntoshechizo"),
                        RS.getInt("capital"),
                        RS.getInt("energia"),
                        RS.getInt("nivel"),
                        RS.getLong("experiencia"),
                        RS.getInt("tamaño"),
                        RS.getInt("gfx"),
                        RS.getByte("alineacion"),
                        RS.getInt("cuenta"), stats,
                        RS.getByte("veramigos"),
                        RS.getByte("veralineacion"),
                        RS.getByte("vervendedor"),
                        RS.getString("canales"),
                        RS.getShort("mapa"),
                        RS.getInt("celda"),
                        RS.getString("objetos"),
                        RS.getString("objetosmercante"),
                        RS.getInt("puntosdevida"),
                        RS.getString("hechizos"),
                        RS.getString("puntoguardado"),
                        RS.getString("oficios"),
                        RS.getInt("xpmontura"),
                        RS.getInt("montura"),
                        RS.getInt("honor"),
                        RS.getInt("deshonor"),
                        RS.getInt("nivelalineacion"),
                        RS.getString("zaaps"),
                        RS.getByte("titulo"),
                        RS.getInt("esposo"))
                //Vérifications pré-connexion
                perso.VerifAndChangeItemPlace()
                agregar_personaje(perso)
                if (getCompte(RS.getInt("cuenta")) != null) getCompte(RS.getInt("cuenta")).addPerso(perso)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
            closeServers()
        }
    }

    @JvmStatic
	fun eliminar_personaje_db(perso: Personaje): Boolean {
        val guid = perso.id
        var baseQuery = "DELETE FROM datos_personajes WHERE id = ?;"
        return try {
            var p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, guid)
            p.execute()
            if (perso.getItemsIDSplitByChar(",") != "") {
                baseQuery = "DELETE FROM datos_objetos WHERE id IN (?);"
                p = nueva_consulta(baseQuery, _dinamicos)
                p.setString(1, perso.getItemsIDSplitByChar(","))
                p.execute()
            }
            if (perso.getStoreItemsIDSplitByChar(",") != "") {
                baseQuery = "DELETE FROM datos_objetos WHERE id IN (?);"
                p = nueva_consulta(baseQuery, _dinamicos)
                p.setString(1, perso.getStoreItemsIDSplitByChar(","))
                p.execute()
            }
            if (perso.mount != null) {
                baseQuery = "DELETE FROM datos_montura WHERE id = ?"
                p = nueva_consulta(baseQuery, _dinamicos)
                p.setInt(1, perso.mount.id)
                p.execute()
                delDragoByID(perso.mount.id)
            }
            cerrar_nueva_consulta(p)
            true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
            JuegoServidor.agregar_a_los_logs("Game: Supression du personnage echouee")
            false
        }
    }

    @JvmStatic
	fun agregar_personaje_db(perso: Personaje): Boolean {
        val baseQuery = "INSERT INTO datos_personajes( `id` , `nombre` , `sexo` , `clase` , `color1` , `color2` , `color3` , `kamas` , `puntoshechizo` , `capital` , `energia` , `nivel` , `experiencia`, `tamaño`, `gfx`, `cuenta`, `celda`,`mapa`,`hechizos`,`objetos`, `objetosmercante`)" +
                " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'', '');"
        return try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, perso.id)
            p.setString(2, perso.nombre)
            p.setInt(3, perso.sexo)
            p.setInt(4, perso.clase)
            p.setInt(5, perso._color1)
            p.setInt(6, perso._color2)
            p.setInt(7, perso._color3)
            p.setLong(8, perso.kamas)
            p.setInt(9, perso._spellPts)
            p.setInt(10, perso._capital)
            p.setInt(11, perso._energy)
            p.setInt(12, perso._lvl)
            p.setLong(13, perso._curExp)
            p.setInt(14, perso._size)
            p.setInt(15, perso._gfxID)
            p.setInt(16, perso.accID)
            p.setInt(17, perso.actualCelda.id)
            p.setInt(18, perso.actualMapa.id.toInt())
            p.setString(19, perso.parseSpellToDB())
            p.execute()
            cerrar_nueva_consulta(p)
            true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
            JuegoServidor.agregar_a_los_logs("Game: Creation du personnage echouee")
            false
        }
    }

    @JvmStatic
	fun cargar_experiencias() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_experiencia;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) addExpLevel(RS.getInt("nivel"), ExpLevel(RS.getLong("personaje"), RS.getInt("oficio"), RS.getInt("dragopavo"), RS.getInt("alineacion")))
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
	fun cargar_celdas(): Int {
        try {
            var nbr = 0
            val RS = ejecutar_consulta("SELECT * FROM `datos_celdas_accion`", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                if (getCarte(RS.getShort("mapa")) == null) continue
                if (getCarte(RS.getShort("mapa")).getMapa(RS.getInt("celda")) == null) continue
                if (RS.getInt("tipo") == 1) { //Stop sur la case(triggers)
                    getCarte(RS.getShort("mapa")).getMapa(RS.getInt("celda")).addOnCellStopAction(RS.getInt("accion"), RS.getString("argumento"), RS.getString("condicion"))
                } else {
                    JuegoServidor.agregar_a_los_logs("Action Event " + RS.getInt("tipo") + " non implante")
                }
                nbr++
            }
            cerrar_resultado(RS)
            return nbr
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
        return 0
    }

    @JvmStatic
	fun cargar_mapas() {
        try {
            var RS: ResultSet? = ejecutar_consulta("SELECT * FROM datos_mapas LIMIT " + MainServidor.LIMITE_DE_MAPAS + ";", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                addCarte(Mapa(
                        RS.getShort("id"),
                        RS.getString("datos"),
                        RS.getByte("ancho"),
                        RS.getByte("largo"),
                        RS.getString("llave"),
                        RS.getString("esquemapelea"),
                        RS.getString("datosmapa"),
                        RS.getString("celdas"),
                        RS.getString("monstruos"),
                        RS.getString("pos"),
                        RS.getByte("numerogrupos"),
                        RS.getByte("tamañogrupo")))
            }
            cerrar_resultado(RS)
            RS = ejecutar_consulta("SELECT * FROM datos_grupo_mobs;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val c = getCarte(RS.getShort("mapa")) ?: continue
                if (c.getMapa(RS.getInt("celda")) == null) continue
                c.addStaticGroup(RS.getInt("celda"), RS.getString("grupo"))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
	fun guardar_personaje(_perso: Personaje, saveItem: Boolean) {
        var baseQuery = "UPDATE `datos_personajes` SET " +
                "`kamas`= ?," +
                "`puntoshechizo`= ?," +
                "`capital`= ?," +
                "`energia`= ?," +
                "`nivel`= ?," +
                "`experiencia`= ?," +
                "`tamaño` = ?," +
                "`gfx`= ?," +
                "`alineacion`= ?," +
                "`honor`= ?," +
                "`deshonor`= ?," +
                "`nivelalineacion`= ?," +
                "`vitalidad`= ?," +
                "`fuerza`= ?," +
                "`sabiduria`= ?," +
                "`inteligencia`= ?," +
                "`suerte`= ?," +
                "`agilidad`= ?," +
                "`verhechizo`= ?," +
                "`veramigos`= ?," +
                "`veralineacion`= ?," +
                "`vervendedor`= ?," +
                "`canales`= ?," +
                "`mapa`= ?," +
                "`celda`= ?," +
                "`puntosdevida`= ?," +
                "`hechizos`= ?," +
                "`objetos`= ?," +
                "`objetosmercante`= ?," +
                "`puntoguardado`= ?," +
                "`zaaps`= ?," +
                "`oficios`= ?," +
                "`xpmontura`= ?," +
                "`montura`= ?," +
                "`titulo`= ?," +
                "`esposo`= ?" +
                " WHERE `datos_personajes`.`id` = ? LIMIT 1 ;"
        var p: PreparedStatement? = null
        try {
            p = nueva_consulta(baseQuery, _dinamicos)
            p.setLong(1, _perso.kamas)
            p.setInt(2, _perso._spellPts)
            p.setInt(3, _perso._capital)
            p.setInt(4, _perso._energy)
            p.setInt(5, _perso._lvl)
            p.setLong(6, _perso._curExp)
            p.setInt(7, _perso._size)
            p.setInt(8, _perso._gfxID)
            p.setInt(9, _perso._align.toInt())
            p.setInt(10, _perso._honor)
            p.setInt(11, _perso.deshonor)
            p.setInt(12, _perso.aLvl)
            p.setInt(13, _perso._baseStats.getEffect(Constantes.STATS_ADD_VITA))
            p.setInt(14, _perso._baseStats.getEffect(Constantes.STATS_ADD_FORC))
            p.setInt(15, _perso._baseStats.getEffect(Constantes.STATS_ADD_SAGE))
            p.setInt(16, _perso._baseStats.getEffect(Constantes.STATS_ADD_INTE))
            p.setInt(17, _perso._baseStats.getEffect(Constantes.STATS_ADD_CHAN))
            p.setInt(18, _perso._baseStats.getEffect(Constantes.STATS_ADD_AGIL))
            p.setInt(19, if (_perso.is_showSpells) 1 else 0)
            p.setInt(20, if (_perso.is_showFriendConnection) 1 else 0)
            p.setInt(21, if (_perso.is_showWings) 1 else 0)
            p.setInt(22, if (_perso.is_showSeller) 1 else 0)
            p.setString(23, _perso._canaux)
            p.setInt(24, _perso.actualMapa.id.toInt())
            p.setInt(25, _perso.actualCelda.id)
            p.setInt(26, _perso._pdvper)
            p.setString(27, _perso.parseSpellToDB())
            p.setString(28, _perso.parseObjetsToDB())
            p.setString(29, _perso.parseStoreItemstoBD())
            p.setString(30, _perso._savePos)
            p.setString(31, _perso.parseZaaps())
            p.setString(32, _perso.parseJobData())
            p.setInt(33, _perso.mountXpGive)
            p.setInt(34, if (_perso.mount != null) _perso.mount.id else -1)
            p.setByte(35, _perso._title)
            p.setInt(36, _perso.wife)
            p.setInt(37, _perso.id)
            p.executeUpdate()
            if (_perso.miembroGremio != null) actualizar_miembro_del_gremio(_perso.miembroGremio)
            if (_perso.mount != null) actualizar_informacion_monturas(_perso.mount)
            JuegoServidor.agregar_a_los_logs("Personaje " + _perso.nombre + " guardado")
        } catch (e: Exception) {
            println("Game: SQL ERROR: " + e.message)
            println("Requete: $baseQuery")
            println("Le personnage n'a pas ete sauvegarde")
            exitProcess(1)
        }
        if (saveItem) {
            baseQuery = "UPDATE `datos_objetos` SET cantidad = ?, ubicacion = ?, caracteristicas = ?, dueño = ? WHERE id = ?;"
            try {
                p = nueva_consulta(baseQuery, _dinamicos)
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            for (idStr in _perso.getItemsIDSplitByChar(":").split(":".toRegex()).toTypedArray()) {
                try {
                    val guid = idStr.toInt()
                    val obj = getObjet(guid) ?: continue
                    p!!.setInt(1, obj.quantity)
                    p.setInt(2, obj.position)
                    p.setString(3, obj.parseToSave())
                    p.setInt(4, _perso.id)
                    p.setInt(5, idStr.toInt())
                    p.execute()
                } catch (e: Exception) {
                    continue
                }
            }
            if (_perso.cuenta == null) return
            for (idStr in _perso.getBankItemsIDSplitByChar(":").split(":".toRegex()).toTypedArray()) {
                try {
                    val guid = idStr.toInt()
                    val obj = getObjet(guid) ?: continue
                    p!!.setInt(1, obj.quantity)
                    p.setInt(2, obj.position)
                    p.setString(3, obj.parseToSave())
                    p.setInt(4, idStr.toInt())
                    p.execute()
                } catch (e: Exception) {
                    continue
                }
            }
        }
        cerrar_nueva_consulta(p)
    }

    @JvmStatic
	fun cargar_hechizos() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_hechizos;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val id = RS.getInt("id")
                val sort = Hechizos(id, RS.getInt("sprite"), RS.getString("infosprite"), RS.getString("objetivoefecto"))
                val l1 = parseSortStats(id, 1, RS.getString("lvl1"))
                val l2 = parseSortStats(id, 2, RS.getString("lvl2"))
                val l3 = parseSortStats(id, 3, RS.getString("lvl3"))
                val l4 = parseSortStats(id, 4, RS.getString("lvl4"))
                var l5: SortStats? = null
                if (!RS.getString("lvl5").equals("-1", ignoreCase = true)) l5 = parseSortStats(id, 5, RS.getString("lvl5"))
                var l6: SortStats? = null
                if (!RS.getString("lvl6").equals("-1", ignoreCase = true)) l6 = parseSortStats(id, 6, RS.getString("lvl6"))
                sort.addSortStats(1, l1)
                sort.addSortStats(2, l2)
                sort.addSortStats(3, l3)
                sort.addSortStats(4, l4)
                sort.addSortStats(5, l5)
                sort.addSortStats(6, l6)
                addSort(sort)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
	fun cargar_objetos_modelo() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_objeto_modelo;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                addObjTemplate(ObjTemplate(
                        RS.getInt("id"),
                        RS.getString("caracteristicas"),
                        RS.getString("nombre"),
                        RS.getInt("tipo"),
                        RS.getInt("nivel"),
                        RS.getInt("pod"),
                        RS.getInt("precio"),
                        RS.getInt("set"),
                        RS.getString("condicion"),
                        RS.getString("infoarma"),
                        RS.getInt("vendido"),
                        RS.getInt("preciomedio")))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
    }

    private fun parseSortStats(id: Int, lvl: Int, str: String): SortStats? {
        return try {
            var stats: SortStats? = null
            val stat = str.split(",".toRegex()).toTypedArray()
            val effets = stat[0]
            val CCeffets = stat[1]
            var PACOST = 6
            try {
                PACOST = stat[2].trim { it <= ' ' }.toInt()
            } catch (ignored: NumberFormatException) {
            }
            val POm = stat[3].trim { it <= ' ' }.toInt()
            val POM = stat[4].trim { it <= ' ' }.toInt()
            val TCC = stat[5].trim { it <= ' ' }.toInt()
            val TEC = stat[6].trim { it <= ' ' }.toInt()
            val line = stat[7].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val LDV = stat[8].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val emptyCell = stat[9].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val MODPO = stat[10].trim { it <= ' ' }.equals("true", ignoreCase = true)
            //int unk = Integer.parseInt(stat[11]);//All 0
            val MaxByTurn = stat[12].trim { it <= ' ' }.toInt()
            val MaxByTarget = stat[13].trim { it <= ' ' }.toInt()
            val CoolDown = stat[14].trim { it <= ' ' }.toInt()
            val type = stat[15].trim { it <= ' ' }
            val level = stat[stat.size - 2].trim { it <= ' ' }.toInt()
            val endTurn = stat[19].trim { it <= ' ' }.equals("true", ignoreCase = true)
            stats = SortStats(id, lvl, PACOST, POm, POM, TCC, TEC, line, LDV, emptyCell, MODPO, MaxByTurn, MaxByTarget, CoolDown, level, endTurn, effets, CCeffets, type)
            stats
        } catch (e: Exception) {
            e.printStackTrace()
            println("[DEBUG]Sort $id lvl $lvl")
            for ((nbr, z) in str.split(",".toRegex()).toTypedArray().withIndex()) {
                println("[DEBUG]$nbr $z")
            }
            exitProcess(1)
            null
        }
    }

    @JvmStatic
	fun cargar_monstruo_modelo() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_monstruos;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val id = RS.getInt("id")
                val gfxID = RS.getInt("gfx")
                val align = RS.getInt("alineacion")
                val colors = RS.getString("color")
                val grades = RS.getString("grado")
                val spells = RS.getString("hechizo")
                val stats = RS.getString("caracteristicas")
                val pdvs = RS.getString("puntosdevida")
                val pts = RS.getString("puntos")
                val inits = RS.getString("iniciativa")
                val mK = RS.getInt("kamasminimas")
                val MK = RS.getInt("kamasmaximas")
                val IAType = RS.getInt("tipoia")
                val xp = RS.getString("experiencia")
                var capturable: Boolean = RS.getInt("capturable") == 1
                addMobTemplate(id, Monstruo(id, gfxID, align, colors, grades, spells, stats, pdvs, pts, inits, mK, MK, xp, IAType, capturable))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
	fun cargar_npc_modelo() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_npc_modelo;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val id = RS.getInt("id")
                val bonusValue = RS.getInt("bonificacion")
                val gfxID = RS.getInt("gfx")
                val scaleX = RS.getInt("escalax")
                val scaleY = RS.getInt("escalay")
                val sex = RS.getInt("sexo")
                val color1 = RS.getInt("color1")
                val color2 = RS.getInt("color2")
                val color3 = RS.getInt("color3")
                val access = RS.getString("accesorios")
                val extraClip = RS.getInt("clipextra")
                val customArtWork = RS.getInt("personalizacion")
                val initQId = RS.getInt("pregunta")
                val ventes = RS.getString("ventas")
                addNpcTemplate(NPCModelo(id, bonusValue, gfxID, scaleX, scaleY, sex, color1, color2, color3, access, extraClip, customArtWork, initQId, ventes))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
	fun guardar_nuevo_objeto(item: Objeto, idpersonaje: Int) {
        try {
            val baseQuery = "REPLACE INTO `datos_objetos` VALUES(?,?,?,?,?,?);"
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, item.id)
            p.setInt(2, item.template.id)
            p.setInt(3, item.quantity)
            p.setInt(4, item.position)
            p.setString(5, item.parseToSave())
            p.setInt(6, idpersonaje)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun guardar_nuevo_grupo_monstruos(mapID: Int, cellID: Int, groupData: String?): Boolean {
        try {
            val baseQuery = "REPLACE INTO `datos_grupo_mobs` VALUES(?,?,?)"
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, mapID)
            p.setInt(2, cellID)
            p.setString(3, groupData)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    @JvmStatic
	fun cargar_preguntas_npc() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_npc_pregunta;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                addNPCQuestion(NPC_question(
                        RS.getInt("id"),
                        RS.getString("respuesta"),
                        RS.getString("parametro"),
                        RS.getString("condicion"),
                        RS.getInt("esfalso")))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
	fun cargar_respuestas_npc() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_npc_respuesta;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val id = RS.getInt("id")
                val type = RS.getInt("tipo")
                val args = RS.getString("argumento")
                if (getNPCreponse(id) == null) addNPCreponse(NPC_reponse(id))
                getNPCreponse(id).addAction(Accion(type, args, ""))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
	fun cargar_acciones_fin_pelea(): Int {
        var nbr = 0
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_fin_pelea_accion;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val map = getCarte(RS.getShort("mapa")) ?: continue
                map.addEndFightAction(RS.getInt("tipopelea"),
                        Accion(RS.getInt("accion"), RS.getString("argumento"), RS.getString("condicion")))
                nbr++
            }
            cerrar_resultado(RS)
            return nbr
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
        return nbr
    }

    @JvmStatic
	fun cargar_accion_objetos(): Int {
        var nbr = 0
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_objetos_accion;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                val id = RS.getInt("modelo")
                val type = RS.getInt("tipo")
                val args = RS.getString("argumento")
                if (getObjTemplate(id) == null) continue
                getObjTemplate(id).addAction(Accion(type, args, ""))
                nbr++
            }
            cerrar_resultado(RS)
            return nbr
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
        return nbr
    }

    @JvmStatic
	fun cargando_objetos(ids: Int) {
        val req = "SELECT * FROM datos_objetos WHERE dueño = $ids;"
        try {
            val RS = ejecutar_consulta(req, MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val guid = RS.getInt("id")
                val tempID = RS.getInt("modelo")
                val qua = RS.getInt("cantidad")
                val pos = RS.getInt("ubicacion")
                val stats = RS.getString("caracteristicas")
                val dueño = RS.getInt("dueño")
                addObjet(newObjet(guid, tempID, qua, pos, stats), dueño, false)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            println("Requete: \n$req")
            exitProcess(1)
        }
    }

    @JvmStatic
	fun eliminar_objeto(guid: Int) {
        val baseQuery = "DELETE FROM datos_objetos WHERE id = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, guid)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun guardar_objeto(item: Objeto, personaje: Int) {
        val baseQuery = "REPLACE INTO `datos_objetos` VALUES (?,?,?,?,?,?);"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, item.id)
            p.setInt(2, item.template.id)
            p.setInt(3, item.quantity)
            p.setInt(4, item.position)
            p.setString(5, item.parseToSave())
            p.setInt(6, personaje)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun crear_montura(DD: Dragopavo) {
        val baseQuery = "REPLACE INTO `datos_montura`(`id`,`color`,`sexo`,`nombre`,`experiencia`,`nivel`," +
                "`resistencia`,`amor`,`madurez`,`serenidad`,`reproducciones`,`fatiga`,`objetos`," +
                "`ancestros`,`energia`, `habilidad`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, DD.id)
            p.setInt(2, DD.color)
            p.setInt(3, DD.sexo)
            p.setString(4, DD._nom)
            p.setLong(5, DD._exp)
            p.setInt(6, DD._level)
            p.setInt(7, DD._endurance)
            p.setInt(8, DD.amor)
            p.setInt(9, DD._maturite)
            p.setInt(10, DD._serenite)
            p.setInt(11, DD._reprod)
            p.setInt(12, DD._fatigue)
            p.setString(13, DD.itemsId)
            p.setString(14, DD.ancestros)
            p.setInt(15, DD._energie)
            p.setString(16, DD._ability)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun eliminar_montura(DID: Int) {
        val baseQuery = "DELETE FROM `datos_montura` WHERE `id` = ?;"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, DID)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun cargar_cuenta_por_id(user: Int) {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_cuenta WHERE `id` = '$user';", MainServidor.DB_DINAMICOS)
            val baseQuery = "UPDATE datos_cuenta SET `actualizarnecesita` = 0 WHERE id = ?;"
            val p = nueva_consulta(baseQuery, _dinamicos)
            while (RS!!.next()) {
                //Si le compte est déjà connecté, on zap
                if (getCompte(RS.getInt("id")) != null) if (getCompte(RS.getInt("id")).isConectado) continue
                val C = Cuenta(
                        RS.getInt("id"),
                        RS.getString("cuenta").toLowerCase(),
                        RS.getString("contraseña"),
                        RS.getString("apodo"),
                        RS.getString("pregunta"),
                        RS.getString("respuesta"),
                        RS.getInt("nivel"),
                        RS.getInt("vip"),
                        RS.getInt("baneado") == 1,
                        RS.getString("ultimaip"),
                        RS.getString("ultimafechaconexion"),
                        RS.getString("banco"),
                        RS.getInt("kamasbanco"),
                        RS.getString("amigos"),
                        RS.getString("enemigos"))
                addAccount(C)
                ReassignAccountToChar(C)
                p.setInt(1, RS.getInt("guid"))
                p.executeUpdate()
            }
            cerrar_nueva_consulta(p)
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun Cargar_cuenta_por_usuario(user: String) {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_cuenta WHERE `cuenta` LIKE '$user';", MainServidor.DB_DINAMICOS)
            val baseQuery = "UPDATE datos_cuenta SET `actualizarnecesita` = 0 WHERE id = ?;"
            val p = nueva_consulta(baseQuery, _dinamicos)
            while (RS!!.next()) {
                //Si le compte est déjà connecté, on zap
                if (getCompte(RS.getInt("id")) != null) if (getCompte(RS.getInt("id")).isConectado) continue
                val C = Cuenta(
                        RS.getInt("id"),
                        RS.getString("cuenta").toLowerCase(),
                        RS.getString("contraseña"),
                        RS.getString("apodo"),
                        RS.getString("pregunta"),
                        RS.getString("respuesta"),
                        RS.getInt("nivel"),
                        RS.getInt("vip"),
                        RS.getInt("baneado") == 1,
                        RS.getString("ultimaip"),
                        RS.getString("ultimafechaconexion"),
                        RS.getString("banco"),
                        RS.getInt("kamasbanco"),
                        RS.getString("amigos"),
                        RS.getString("enemigos"))
                addAccount(C)
                ReassignAccountToChar(C)
                p.setInt(1, RS.getInt("id"))
                p.executeUpdate()
            }
            cerrar_nueva_consulta(p)
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun actualizar_ultima_fecha_conexion(compte: Cuenta) {
        val baseQuery = "UPDATE datos_cuenta SET `ultimaip` = ?, `ultimafechaconexion` = ? WHERE `id` = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setString(1, compte.actualIP)
            p.setString(2, compte.lastConnectionDate)
            p.setInt(3, compte.id)
            p.executeUpdate()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            agregar_a_los_logs("Query: $baseQuery")
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun actualizar_informacion_monturas(DD: Dragopavo) {
        val baseQuery = "UPDATE datos_montura SET " +
                "`nombre` = ?," +
                "`experiencia` = ?," +
                "`nivel` = ?," +
                "`resistencia` = ?," +
                "`amor` = ?," +
                "`madurez` = ?," +
                "`serenidad` = ?," +
                "`reproducciones` = ?," +
                "`fatiga` = ?," +
                "`energia` = ?," +
                "`ancestros` = ?," +
                "`objetos` = ?," +
                "`habilidad` = ?" +
                " WHERE `id` = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setString(1, DD._nom)
            p.setLong(2, DD._exp)
            p.setInt(3, DD._level)
            p.setInt(4, DD._endurance)
            p.setInt(5, DD.amor)
            p.setInt(6, DD._maturite)
            p.setInt(7, DD._serenite)
            p.setInt(8, DD._reprod)
            p.setInt(9, DD._fatigue)
            p.setInt(10, DD._energie)
            p.setString(11, DD.ancestros)
            p.setString(12, DD.itemsId)
            p.setString(13, DD._ability)
            p.setInt(14, DD.id)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Query: $baseQuery")
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun guardar_cercados(MP: MountPark) {
        val baseQuery = "REPLACE INTO `datos_cercados`( `mapa` , `celda`, `tamaño` , `dueño` , `gremio` , `precio` , `monturas` ) VALUES (?,?,?,?,?,?,?);"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, MP._map.id.toInt())
            p.setInt(2, MP._cellid)
            p.setInt(3, MP._size)
            p.setInt(4, MP._owner)
            p.setInt(5, if (MP.gremio == null) -1 else MP.gremio._id)
            p.setInt(6, MP._price)
            p.setString(7, MP.parseDBData())
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun actualizar_cercado(MP: MountPark) {
        val baseQuery = "UPDATE `datos_cercados` SET `monturas` = ? WHERE mapa = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setString(1, MP.parseDBData())
            p.setShort(2, MP._map.id)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun guardar_celdas(mapID1: Int, cellID1: Int, action: Int, event: Int, args: String?, cond: String?): Boolean {
        val baseQuery = "REPLACE INTO `datos_celdas_accion` VALUES (?,?,?,?,?,?);"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, mapID1)
            p.setInt(2, cellID1)
            p.setInt(3, action)
            p.setInt(4, event)
            p.setString(5, args)
            p.setString(6, cond)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun eliminar_celdas(mapID: Int, cellID: Int): Boolean {
        val baseQuery = "DELETE FROM `datos_celdas_accion` WHERE `mapa` = ? AND `celda` = ?;"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, mapID)
            p.setInt(2, cellID)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun guardar_mapa(map: Mapa): Boolean {
        val baseQuery = "UPDATE `datos_mapas` SET `esquemapelea` = ?, `numerogrupos` = ? WHERE id = ?;"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setString(1, map.esquemaPelea)
            p.setInt(2, map.maxGroupNumb)
            p.setInt(3, map.id.toInt())
            p.executeUpdate()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun eliminar_npc_en_mapa(m: Int, c: Int): Boolean {
        val baseQuery = "DELETE FROM datos_npc WHERE mapa = ? AND celda = ?;"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, m)
            p.setInt(2, c)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun eliminar_recaudador(id: Int): Boolean {
        val baseQuery = "DELETE FROM datos_recaudadores WHERE id = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, id)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun agregar_publicidad(publicidad: String?): Boolean {
        val baseQuery = "INSERT INTO `datos_publicidad` VALUES (?, ?);"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, 0)
            p.setString(2, publicidad)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun agregar_npc_en_mapa(m: Int, id: Int, c: Int, o: Int): Boolean {
        val baseQuery = "INSERT INTO `datos_npc` VALUES (?,?,?,?);"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, m)
            p.setInt(2, id)
            p.setInt(3, c)
            p.setInt(4, o)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun agregar_recaudador_en_mapa(guid: Int, mapid: Int, guildID: Int, cellid: Int, o: Int, N1: Short, N2: Short): Boolean {
        val baseQuery = "INSERT INTO `datos_recaudadores` VALUES (?,?,?,?,?,?,?,?,?,?);"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, guid)
            p.setInt(2, mapid)
            p.setInt(3, cellid)
            p.setInt(4, o)
            p.setInt(5, guildID)
            p.setShort(6, N1)
            p.setShort(7, N2)
            p.setString(8, "")
            p.setLong(9, 0)
            p.setLong(10, 0)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun actualizar_recaudador(P: Recaudador) {
        val baseQuery = "UPDATE `datos_recaudadores` SET `objetos` = ?, `kamas` = ?, `experiencia` = ? WHERE id = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setString(1, P.parseItemPercepteur())
            p.setLong(2, P.kamas)
            p.setLong(3, P.xp)
            p.setInt(4, P.guid)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun agregar_fin_pelea_accion(mapID: Int, type: Int, Aid: Int, args: String?, cond: String?): Boolean {
        if (!eliminar_fin_pelea_accion(mapID, type, Aid)) return false
        val baseQuery = "INSERT INTO `datos_fin_pelea_accion` VALUES (?,?,?,?,?);"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, mapID)
            p.setInt(2, type)
            p.setInt(3, Aid)
            p.setString(4, args)
            p.setString(5, cond)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    private fun eliminar_fin_pelea_accion(mapID: Int, type: Int, aid: Int): Boolean {
        val baseQuery = "DELETE FROM `datos_fin_pelea_accion` WHERE mapa = ? AND tipopelea = ? AND accion = ?;"
        return try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, mapID)
            p.setInt(2, type)
            p.setInt(3, aid)
            p.execute()
            cerrar_nueva_consulta(p)
            true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
            false
        }
    }

    @JvmStatic
	fun guardar_nuevo_gremio(g: Gremio) {
        val baseQuery = "INSERT INTO `datos_gremio` VALUES (?,?,?,1,0,0,0,?,?);"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, g._id)
            p.setString(2, g._name)
            p.setString(3, g._emblem)
            p.setString(4, "462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0")
            p.setString(5, "176;100|158;1000|124;100")
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun eliminar_gremio(id: Int) {
        val baseQuery = "DELETE FROM `datos_gremio` WHERE `id` = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, id)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun eliminar_todos_los_miembros_del_gremio(guildid: Int) {
        val baseQuery = "DELETE FROM `datos_miembros_gremio` WHERE `gremio` = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, guildid)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun eliminar_miembro_del_gremio(id: Int) {
        val baseQuery = "DELETE FROM `datos_miembros_gremio` WHERE `id` = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, id)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun actualizar_gremio(g: Gremio) {
        val baseQuery = "UPDATE `datos_gremio` SET `nivel` = ?, `experiencia` = ?,`capital` = ?, `recaudadoresmaximos` = ?, `hechizos` = ?, `caracteristicas` = ? WHERE id = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, g._lvl)
            p.setLong(2, g._xp)
            p.setInt(3, g._Capital)
            p.setInt(4, g._nbrPerco)
            p.setString(5, g.compileSpell())
            p.setString(6, g.compileStats())
            p.setInt(7, g._id)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun actualizar_miembro_del_gremio(gm: GuildMember) {
        val baseQuery = "REPLACE INTO `datos_miembros_gremio` VALUES(?,?,?,?,?,?);"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, gm.guid)
            p.setInt(2, gm.guild._id)
            p.setInt(3, gm.rank)
            p.setLong(4, gm.xpGave)
            p.setInt(5, gm.pXpGive)
            p.setInt(6, gm.rights)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun personaje_esta_en_gremio(guid: Int): Int {
        var guildId = -1
        try {
            val GuildQuery = ejecutar_consulta("SELECT gremio FROM `datos_miembros_gremio` WHERE id =$guid;", MainServidor.DB_DINAMICOS)
            val found = GuildQuery!!.first()
            if (found) guildId = GuildQuery.getInt("gremio")
            cerrar_resultado(GuildQuery)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return guildId
    }

    @JvmStatic
	fun personaje_esta_en_gremio(name: String): IntArray {
        var guildId = -1
        var guid = -1
        try {
            val GuildQuery = ejecutar_consulta("SELECT gremio,id FROM `datos_miembros_gremio` WHERE nombre ='$name';", MainServidor.DB_DINAMICOS)
            val found = GuildQuery!!.first()
            if (found) {
                guildId = GuildQuery.getInt("gremio")
                guid = GuildQuery.getInt("id")
            }
            cerrar_resultado(GuildQuery)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return intArrayOf(guid, guildId)
    }

    @JvmStatic
	fun agregar_respuesta_npc(repID: Int, type: Int, args: String?): Boolean {
        var baseQuery = "DELETE FROM `datos_npc_respuesta` WHERE `id` = ? AND `tipo` = ?;"
        var p: PreparedStatement
        try {
            p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, repID)
            p.setInt(2, type)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        baseQuery = "INSERT INTO `datos_npc_respuesta` VALUES (?,?,?);"
        try {
            p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, repID)
            p.setInt(2, type)
            p.setString(3, args)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun actualizar_respuesta_de_npc(id: Int, q: Int): Boolean {
        val baseQuery = "UPDATE `datos_npc_modelo` SET `pregunta` = ? WHERE `id` = ?;"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setInt(1, q)
            p.setInt(2, id)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun actualizar_pregunta_npc(id: Int, reps: String?): Boolean {
        val baseQuery = "UPDATE `datos_npc_pregunta` SET `respuestas` = ? WHERE `id` = ?;"
        try {
            val p = nueva_consulta(baseQuery, _estaticos)
            p.setString(1, reps)
            p.setInt(2, id)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun cargar_acciones() {
        /*Variables représentant les champs de la base*/
        var perso: Personaje?
        var action: Int
        var nombre: Int
        var id: Int
        addToShopLog("Lancement de l'application des Lives Actions ...")
        var sortie: StringBuilder
        val couleur = "DF0101" //La couleur du message envoyer a l'utilisateur (couleur en code HTML)
        var t: ObjTemplate?
        var obj: Objeto?
        var p: PreparedStatement
        /*FIN*/try {
            val RS = ejecutar_consulta("SELECT * FROM datos_acciones_tiempo_real;", MainServidor.DB_DINAMICOS)
            loop@ while (RS!!.next()) {
                perso = getPersonnage(RS.getInt("personaje"))
                if (perso == null) {
                    addToShopLog("Personnage " + RS.getInt("personaje") + " non trouve, personnage non charge ?")
                    continue
                }
                if (!perso.isConectado) {
                    addToShopLog("Personnage " + RS.getInt("personaje") + " hors ligne")
                    continue
                }
                if (perso.cuenta == null) {
                    addToShopLog("Le Personnage " + RS.getInt("personaje") + " n'est attribue a aucun compte charge")
                    continue
                }
                if (perso.cuenta.juegoThread == null) {
                    addToShopLog("Le Personnage " + RS.getInt("personaje") + " n'a pas thread associe, le personnage est il hors ligne ?")
                    continue
                }
                if (perso.pelea != null) continue  // Perso en combat  @ Nami-Doc
                action = RS.getInt("accion")
                nombre = RS.getInt("nombre")
                id = RS.getInt("id")
                sortie = StringBuilder("+")
                when (action) {
                    1 -> {
                        if (perso._lvl == getExpLevelSize()) continue@loop
                        var n = nombre
                        while (n > 1) {
                            perso.levelUp(false, true)
                            n--
                        }
                        perso.levelUp(true, true)
                        sortie.append(nombre).append(" Niveau(x)")
                    }
                    2 -> {
                        if (perso._lvl == getExpLevelSize()) continue@loop
                        perso.addXp(nombre.toLong())
                        sortie.append(nombre).append(" Xp")
                    }
                    3 -> {
                        perso.addKamas(nombre.toLong())
                        sortie.append(nombre).append(" Kamas")
                    }
                    4 -> {
                        perso.addPuntosDeCapital(nombre)
                        sortie.append(nombre).append(" Point(s) de capital")
                    }
                    5 -> {
                        perso.addAgregarPuntosDeHechizo(nombre)
                        sortie.append(nombre).append(" Point(s) de sort")
                    }
                    20 -> {
                        t = getObjTemplate(nombre)
                        if (t == null) continue@loop
                        obj = t.createNewItem(1, false) //Si mis à "true" l'objet à des jets max. Sinon ce sont des jets aléatoire
                        if (obj == null) continue@loop
                        if (perso.addObjet(obj, true)) //Si le joueur n'avait pas d'item similaire
                            addObjet(obj, perso.id,true)
                        JuegoServidor.addToSockLog("Objet " + nombre + " ajouter a " + perso.nombre + " avec des stats aleatoire")
                        GestorSalida.GAME_SEND_MESSAGE(perso, "L'objet \"" + t.name + "\" viens d'etre ajouter a votre personnage", couleur)
                    }
                    21 -> {
                        t = getObjTemplate(nombre)
                        if (t == null) continue@loop
                        obj = t.createNewItem(1, true) //Si mis à "true" l'objet à des jets max. Sinon ce sont des jets aléatoire
                        if (obj == null) continue@loop
                        if (perso.addObjet(obj, true)) //Si le joueur n'avait pas d'item similaire
                            addObjet(obj, perso.id,true)
                        JuegoServidor.addToSockLog("Objet " + nombre + " ajoute a " + perso.nombre + " avec des stats MAX")
                        GestorSalida.GAME_SEND_MESSAGE(perso, "L'objet \"" + t.name + "\" avec des stats maximum, viens d'etre ajoute a votre personnage", couleur)
                    }
                    118 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso)
                        sortie.append(nombre).append(" force")
                    }
                    119 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso)
                        sortie.append(nombre).append(" agilite")
                    }
                    123 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso)
                        sortie.append(nombre).append(" chance")
                    }
                    124 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso)
                        sortie.append(nombre).append(" sagesse")
                    }
                    125 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso)
                        sortie.append(nombre).append(" vita")
                    }
                    126 -> {
                        val statID = action
                        perso._baseStats.addOneStat(statID, nombre)
                        GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso)
                        sortie.append(nombre).append(" intelligence")
                    }
                }
                GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso)
                if (action < 20 || action > 100) GestorSalida.GAME_SEND_MESSAGE(perso, "$sortie a votre personnage", couleur) //Si l'action n'est pas un ajout d'objet on envoye un message a l'utilisateur
                addToShopLog("(Commande " + id + ")Action " + action + " Nombre: " + nombre + " appliquee sur le personnage " + RS.getInt("jugador") + "(" + perso.nombre + ")")
                try {
                    val query = "DELETE FROM datos_acciones_tiempo_real WHERE id=$id;"
                    p = nueva_consulta(query, _dinamicos)
                    p.execute()
                    cerrar_nueva_consulta(p)
                    addToShopLog("Commande $id supprimee.")
                } catch (e: SQLException) {
                    JuegoServidor.agregar_a_los_logs("SQL ERROR: " + e.message)
                    addToShopLog("Error Delete From: " + e.message)
                    e.printStackTrace()
                }
                guardar_personaje(perso, true)
            }
            cerrar_resultado(RS)
        } catch (e: Exception) {
            JuegoServidor.agregar_a_los_logs("ERROR: " + e.message)
            addToShopLog("Error: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun salir_del_juego(accID: Int, logged: Int) {
        val p: PreparedStatement
        val query = "UPDATE `datos_cuenta` SET conectado = ? WHERE `id`=?;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setInt(1, logged)
            p.setInt(2, accID)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
    }

    @JvmStatic
	fun conectado_a_0() {
        val p: PreparedStatement
        val query = "UPDATE `datos_cuenta` SET conectado = 0;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
    }

    @JvmStatic
	fun cargar_maximo_de_objetos() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_objetos;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val guid = RS.getInt("id")
                val tempID = RS.getInt("modelo")
                val qua = RS.getInt("cantidad")
                val pos = RS.getInt("ubicacion")
                val stats = RS.getString("caracteristicas")
                val dueño = RS.getInt("dueño")
                addObjet(Objeto(guid, tempID, qua, pos, stats), dueño, false)
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
	fun persoExist(name: String?): Boolean {
        var exist = false
        val p: PreparedStatement
        val query = "SELECT COUNT(*) AS exist FROM datos_personajes WHERE nombre LIKE ?;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setString(1, name)
            val RS = p.executeQuery()
            val found = RS.first()
            if (found) {
                if (RS.getInt("exist") != 0) exist = true
            }
            cerrar_resultado(RS)
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return exist
    }

    @JvmStatic
	fun comprar_casa(P: Personaje, h: Casas) {
        var p: PreparedStatement
        var query = "UPDATE `datos_casas` SET `venta`='0', `dueño`=?, `gremio`='0', `acceso`='0', `llave`='-', `derechosgremio`='0' WHERE `id`=?;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setInt(1, P.accID)
            p.setInt(2, h._id)
            p.execute()
            cerrar_nueva_consulta(p)
            h._sale = 0
            h._owner_id = P.accID
            h._guild_id = 0
            h._access = 0
            h._key = "-"
            h._guild_rights = 0
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
        val trunks = Cofres.getTrunksByHouse(h)
        for (trunk in trunks) {
            trunk._owner_id = P.accID
            trunk._key = "-"
        }
        query = "UPDATE `datos_cofres` SET `dueño`=?, `llave`='-' WHERE `casa`=?;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setInt(1, P.accID)
            p.setInt(2, h._id)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
    }

    @JvmStatic
	fun vender_casa(h: Casas, price: Int) {
        h._sale = price
        val p: PreparedStatement
        val query = "UPDATE `datos_casas` SET `venta`=? WHERE `id`=?;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setInt(1, price)
            p.setInt(2, h._id)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
    }

    @JvmStatic
	fun codigo_casa(P: Personaje, h: Casas, packet: String?) {
        val p: PreparedStatement
        val query = "UPDATE `datos_casas` SET `llave`=? WHERE `id`=? AND dueño=?;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setString(1, packet)
            p.setInt(2, h._id)
            p.setInt(3, P.accID)
            p.execute()
            cerrar_nueva_consulta(p)
            h._key = packet
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
    }

    @JvmStatic
	fun casa_gremio(h: Casas, GuildID: Int, GuildRights: Int) {
        val p: PreparedStatement
        val query = "UPDATE `datos_casas` SET `gremio`=?, `derechosgremio`=? WHERE `id`=?;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setInt(1, GuildID)
            p.setInt(2, GuildRights)
            p.setInt(3, h._id)
            p.execute()
            cerrar_nueva_consulta(p)
            h._guild_id = GuildID
            h._guild_rights = GuildRights
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
    }

    @JvmStatic
	fun eliminar_casa_gremio(GuildID: Int) {
        val p: PreparedStatement
        val query = "UPDATE `datos_casas` SET `derechosgremio`='0', `gremio`='0' WHERE `gremio`=?;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setInt(1, GuildID)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
    }

    @JvmStatic
	fun actualizar_casa(h: Casas) {
        val baseQuery = "UPDATE `datos_casas` SET `dueño` = ?, `venta` = ?, `gremio` = ?, `acceso` = ?, `llave` = ?, `derechosgremio` = ? WHERE id = ?;"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setInt(1, h._owner_id)
            p.setInt(2, h._sale)
            p.setInt(3, h._guild_id)
            p.setInt(4, h._access)
            p.setString(5, h._key)
            p.setInt(6, h._guild_rights)
            p.setInt(7, h._id)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
	fun nueva_id_recaudador(): Int {
        var i = -50 //Pour éviter les conflits avec touts autre NPC
        try {
            val query = "SELECT `id` FROM `datos_recaudadores` ORDER BY `id` ASC LIMIT 0 , 1;"
            val RS = ejecutar_consulta(query, MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                i = RS.getInt("guid") - 1
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
	fun cargar_zaapis(): Int {
        var i = 0
        val bonta = StringBuilder()
        val brak = StringBuilder()
        val neutral = StringBuilder()
        try {
            val RS = ejecutar_consulta("SELECT mapa, alineacion FROM datos_zappis;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                if (RS.getInt("alineacion") == Constantes.ALIGNEMENT_BONTARIEN) {
                    bonta.append(RS.getString("mapa"))
                    if (!RS.isLast) bonta.append(",")
                } else if (RS.getInt("alineacion") == Constantes.ALIGNEMENT_BRAKMARIEN) {
                    brak.append(RS.getString("mapa"))
                    if (!RS.isLast) brak.append(",")
                } else {
                    neutral.append(RS.getString("mapa"))
                    if (!RS.isLast) neutral.append(",")
                }
                i++
            }
            Constantes.ZAAPI[Constantes.ALIGNEMENT_BONTARIEN] = bonta.toString()
            Constantes.ZAAPI[Constantes.ALIGNEMENT_BRAKMARIEN] = brak.toString()
            Constantes.ZAAPI[Constantes.ALIGNEMENT_NEUTRE] = neutral.toString()
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
	fun cargar_zaaps(): Int {
        var i = 0
        try {
            val RS = ejecutar_consulta("SELECT mapa, celda FROM datos_zaaps;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                Constantes.ZAAPS[RS.getInt("mapa")] = RS.getInt("celda")
                i++
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
	fun siguiente_id_objeto(): Int {
        try {
            val RS = ejecutar_consulta("SELECT MAX(id) AS max FROM datos_objetos;", MainServidor.DB_DINAMICOS)
            var guid = 0
            val found = RS!!.first()
            if (found) guid = RS.getInt("max")
            cerrar_resultado(RS)
            return guid
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
            closeServers()
        }
        return 0
    }

    @JvmStatic
	fun cargar_ip_baneadas(): Int {
        var i = 0
        try {
            val RS = ejecutar_consulta("SELECT ip FROM datos_ipbaneadas;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                Constantes.BAN_IP += RS.getString("ip")
                if (!RS.isLast) Constantes.BAN_IP += ","
                i++
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
	fun agregar_ip_baneada(ip: String?): Boolean {
        val baseQuery = "INSERT INTO `datos_ipbaneadas` VALUES (?);"
        try {
            val p = nueva_consulta(baseQuery, _dinamicos)
            p.setString(1, ip)
            p.execute()
            cerrar_nueva_consulta(p)
            return true
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
	fun cargar_mercadillos() {
        try {
            var RS = ejecutar_consulta("SELECT * FROM `datos_mercadillos` ORDER BY id ASC", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                addHdv(Mercadillo(
                        RS.getInt("mapa"),
                        RS.getFloat("tazaventa"),
                        RS.getShort("tiempoventa"),
                        RS.getShort("cuenta"),
                        RS.getShort("nivelmaximo"),
                        RS.getString("categoria")))
            }
            RS = ejecutar_consulta("SELECT id MAX FROM `datos_mercadillos`", MainServidor.DB_ESTATICOS)
            RS!!.first()
            setNextHdvID(RS.getInt("MAX"))
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_objetos_mercadillos() {
        try {
            val time1 = System.currentTimeMillis() //TIME
            var RS = ejecutar_consulta("SELECT i.* FROM `datos_objetos` AS i,`datos_objetos_mercadillo` AS h WHERE i.id = h.objeto", MainServidor.DB_DINAMICOS)
            //Load items
            while (RS!!.next()) {
                val guid = RS.getInt("id")
                val tempID = RS.getInt("modelo")
                val qua = RS.getInt("cantidad")
                val pos = RS.getInt("ubicacion")
                val stats = RS.getString("caracteristicas")
                val dueño = RS.getInt("dueño")
                addObjet(newObjet(guid, tempID, qua, pos, stats), dueño, false)
            }

            //Load HDV entry
            RS = ejecutar_consulta("SELECT * FROM `datos_objetos_mercadillo`", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                val tempHdv = getHdv(RS.getInt("mapa")) ?: continue
                tempHdv.addEntry(HdvEntry(
                        RS.getInt("precio"),
                        RS.getByte("cantidad"),
                        RS.getInt("dueño"),
                        getObjet(RS.getInt("objeto"))))
            }
            println((System.currentTimeMillis() - (time1)).toString() + "ms pour loader les HDVS items") //TIME
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun guardar_objetos_mercadillo(liste: ArrayList<HdvEntry>) {
        var queries: PreparedStatement? = null
        try {
            val emptyQuery = "TRUNCATE TABLE `datos_objetos_mercadillo`"
            val emptyTable = nueva_consulta(emptyQuery, _dinamicos)
            emptyTable.execute()
            cerrar_nueva_consulta(emptyTable)
            val baseQuery = "INSERT INTO `datos_objetos_mercadillo` (`mapa`,`dueño`,`precio`,`cantidad`,`objeto`) VALUES (?,?,?,?,?);"
            queries = nueva_consulta(baseQuery, _dinamicos)
            for (curEntry in liste) {
                if (curEntry.owner == -1) continue
                queries.setInt(1, curEntry.hdvID)
                queries.setInt(2, curEntry.owner)
                queries.setInt(3, curEntry.price)
                queries.setInt(4, curEntry.getAmount(false).toInt())
                queries.setInt(5, curEntry.objet.id)
                queries.execute()
            }
            cerrar_nueva_consulta(queries)
            guardar_mercadillo_precio_medio()
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    private fun guardar_mercadillo_precio_medio() {
        val baseQuery = "UPDATE `datos_objeto_modelo` SET vendido = ?, preciomedio = ? WHERE id = ?;"
        var queries: PreparedStatement? = null
        try {
            queries = nueva_consulta(baseQuery, _estaticos)
            for (curTemp in getObjTemplates()) {
                if (curTemp.sold == 0L) continue
                queries.setLong(1, curTemp.sold)
                queries.setInt(2, curTemp.avgPrice)
                queries.setInt(3, curTemp.id)
                queries.executeUpdate()
            }
            cerrar_nueva_consulta(queries)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_animaciones() {
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_animaciones;", MainServidor.DB_ESTATICOS)
            while (RS!!.next()) {
                addAnimation(Animaciones(
                        RS.getInt("id"),
                        RS.getInt("id2"),
                        RS.getString("nombre"),
                        RS.getInt("area"),
                        RS.getInt("accion"),
                        RS.getInt("tamaño")))
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun cargar_cofre(): Int {
        var nbr = 0
        try {
            val RS = ejecutar_consulta("SELECT * FROM datos_cofres;", MainServidor.DB_DINAMICOS)
            while (RS!!.next()) {
                addTrunk(Cofres(
                        RS.getInt("id"),
                        RS.getInt("casa"),
                        RS.getShort("mapa"),
                        RS.getInt("celda"),
                        RS.getString("objeto"),
                        RS.getInt("kamas").toLong(),
                        RS.getString("llave"),
                        RS.getInt("dueño")))
                nbr++
            }
            cerrar_resultado(RS)
        } catch (e: SQLException) {
            agregar_a_los_logs("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
	fun cofre_codigo(P: Personaje, t: Cofres, packet: String?) {
        val p: PreparedStatement
        val query = "UPDATE `datos_cofres` SET `llave`=? WHERE `id`=? AND dueño=?;"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setString(1, packet)
            p.setInt(2, t._id)
            p.setInt(3, P.accID)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
    }

    @JvmStatic
	fun actualizar_cofre(t: Cofres) {
        val p: PreparedStatement
        val query = "UPDATE `datos_cofres` SET `kamas`=?, `objeto`=? WHERE `id`=?"
        try {
            p = nueva_consulta(query, _dinamicos)
            p.setLong(1, t._kamas)
            p.setString(2, t.parseTrunkObjetsToDB())
            p.setInt(3, t._id)
            p.execute()
            cerrar_nueva_consulta(p)
        } catch (e: SQLException) {
            JuegoServidor.agregar_a_los_logs("Game: SQL ERROR: " + e.message)
            JuegoServidor.agregar_a_los_logs("Game: Query: $query")
        }
    }
}
