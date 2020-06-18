package comunes

import java.awt.event.ActionListener
import java.io.PrintWriter
import java.util.*
import javax.swing.Timer

object GestorEnvio {
    private val PacketBuffer: MutableMap<Int, MutableMap<Long, Map<PrintWriter, String>>> = TreeMap() //<hachID, <PacketID, <PacketID,String>>>
    private var packetid: Long = 1
    private var BufferRemove = ""
    private fun get_BufferRemove(): String {
        return BufferRemove
    }

    private fun set_BufferRemove(str: String) {
        BufferRemove += str
    }

    private fun del_BufferRemove() {
        BufferRemove = ""
    }

    private fun getPacketBuffer(): Map<Int, MutableMap<Long, Map<PrintWriter, String>>> {
        return PacketBuffer
    }

    fun FlushTimer(): Timer {
        val action = ActionListener {
            for ((key, value) in getPacketBuffer()) {
                if ((getPacketBuffer()[key] ?: error("")).isEmpty()) continue
                val Totaldata = StringBuilder()
                var pw: PrintWriter? = null
                for ((key1) in getPacketBuffer()[key] ?: error("")) {
                    for ((key2, value1) in (getPacketBuffer()[key] ?: error(""))[key1]!!) {
                        Totaldata.append(value1).append(0x00.toChar())
                        if (pw != null && pw.hashCode() == key2.hashCode()) continue
                        pw = key2
                    }
                    set_BufferRemove("$key1,")
                }
                if (Totaldata.toString().isEmpty()) continue
                for (id in get_BufferRemove().split(",".toRegex()).toTypedArray()) {
                    value.remove(id.toLong())
                }
                del_BufferRemove()
                pw!!.print(Totaldata.toString())
                pw.flush()
                if (MainServidor.CONFIG_DEBUG) println("Multi: Send>>$Totaldata")
            }
        }
        return Timer(MainServidor.CONFIG_SOCKET_TIME_COMPACT_DATA, action)
    }

    @JvmStatic
	fun send(out: PrintWriter, packet: String) {
        if (!getPacketBuffer().containsKey(out.hashCode())) {
            val firstData: MutableMap<PrintWriter, String> = TreeMap()
            firstData[out] = packet
            val secondData: MutableMap<Long, Map<PrintWriter, String>> = TreeMap()
            secondData[packetid++] = firstData
            PacketBuffer[out.hashCode()] = secondData
        } else {
            val data: MutableMap<PrintWriter, String> = TreeMap()
            data[out] = packet
            PacketBuffer[out.hashCode()]!![packetid++] = data
        }
    }
}