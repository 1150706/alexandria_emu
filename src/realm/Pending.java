package realm;

import comunes.GestorSalida;

import objetos.Cuenta;

public class Pending {

    /*
     TODO : Gestion du paquet Af + position dans la file d'attente.
    */
    public static void PendingSystem(Cuenta C)
    {
        if(C == null) return;
        if(C._position <= 1)
        {
            try {
                	Thread.sleep(750);
                	if(C == null || C.getRealmThread()._out == null) return;
                	GestorSalida.MULTI_SEND_Af_PACKET(C.getRealmThread()._out,1,RealmServer._totalAbo,RealmServer._totalNonAbo,""+1,RealmServer._queueID);
                	C._position = -1;
                	RealmServer._totalAbo--;
            	} catch (InterruptedException e) {
            		GestorSalida.REALM_SEND_ALREADY_CONNECTED(C.getRealmThread()._out);
            		RealmServer.addToLog("Erreur : " + e.getMessage());
            }
        }else
        {
            try {
            		Thread.sleep(750*C._position);
            		if(C == null ||  C.getRealmThread()._out == null) return;
            		GestorSalida.MULTI_SEND_Af_PACKET(C.getRealmThread()._out,1,RealmServer._totalAbo,RealmServer._totalNonAbo,""+1,RealmServer._queueID);
            		C._position = -1;
            		RealmServer._totalAbo--;
            	} catch (InterruptedException e) {
            			GestorSalida.REALM_SEND_ALREADY_CONNECTED(C.getRealmThread()._out);
            			RealmServer.addToLog("Erreur : " + e.getMessage());
            	}
        }
    }
}