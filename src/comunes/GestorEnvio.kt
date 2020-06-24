package comunes

import java.awt.event.ActionListener
import java.io.PrintWriter
import java.util.*
import javax.swing.Timer

object GestorEnvio {
    private val paquetebuffer: MutableMap<Int, MutableMap<Long, Map<PrintWriter, String>>> = TreeMap() //<hachID, <PacketID, <PacketID,String>>>
    private var paqueteid: Long = 1
    private var eliminarbuffer = ""
    private fun getEliminarBuffer(): String {
        return eliminarbuffer
    }

    private fun setEliminarBuffer(str: String) {
        eliminarbuffer += str
    }

    private fun del_BufferRemove() {
        eliminarbuffer = ""
    }

    private fun getPacketBuffer(): Map<Int, MutableMap<Long, Map<PrintWriter, String>>> {
        return paquetebuffer
    }

    fun flush_timer(): Timer {
        val action = ActionListener {
            for ((key, value) in getPacketBuffer()) {
                if ((getPacketBuffer()[key] ?: error("")).isEmpty()) continue
                val datostotales = StringBuilder()
                var pw: PrintWriter? = null
                for ((key1) in getPacketBuffer()[key] ?: error("")) {
                    for ((key2, value1) in (getPacketBuffer()[key] ?: error(""))[key1]!!) {
                        datostotales.append(value1).append(0x00.toChar())
                        if (pw != null && pw.hashCode() == key2.hashCode()) continue
                        pw = key2
                    }
                    setEliminarBuffer("$key1,")
                }
                if (datostotales.toString().isEmpty()) continue
                for (id in getEliminarBuffer().split(",".toRegex()).toTypedArray()) {
                    value.remove(id.toLong())
                }
                del_BufferRemove()
                pw!!.print(datostotales.toString())
                pw.flush()
                if (MainServidor.MOSTRAR_ENVIADOS) println("Multi: Send>>$datostotales")
            }
        }
        return Timer(MainServidor.CONFIG_SOCKET_TIME_COMPACT_DATA, action)
    }

    @JvmStatic
	fun enviar(out: PrintWriter, packet: String) {
        if (!getPacketBuffer().containsKey(out.hashCode())) {
            val firstData: MutableMap<PrintWriter, String> = TreeMap()
            firstData[out] = packet
            val secondData: MutableMap<Long, Map<PrintWriter, String>> = TreeMap()
            secondData[paqueteid++] = firstData
            paquetebuffer[out.hashCode()] = secondData
        } else {
            val data: MutableMap<PrintWriter, String> = TreeMap()
            data[out] = packet
            paquetebuffer[out.hashCode()]!![paqueteid++] = data
        }
    }
}