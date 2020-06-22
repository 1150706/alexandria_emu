# alexandria_emu
Emulador para Dofus v1.29 - Basado en AncestraR rev47

<b>v0.00.1</b>
- Actualizacion de las librerias e implementada la libreria de Hikari
- MainServer a Kotlin, ex Ancestra.java
- Eliminada columna ultima conexion, nivel, alineacion y sfx de la tabla miembros_gremio
- Nivel, ultima conexion, alineacion y sfx para miembros de gremios se toma desde personaje y cuenta
- Agregado Hikari para consultas a la BDD
- Division de la base de datos a dinamicos y estaticos
- Re organizacion de la base de datos
- Traduccion de las tablas y columnas reorganizadas

<b>v0.00.2</b>
- Arreglo error al guardar los objetos de los personajes
- Arreglo en base de datos, carga de personajes en el gremio
- Agregado de capital en caracteristicas reparado
- Agregada accion 104 Cliqueador automatico
- Agregada accion 105 teletransportar a todos los miembros del grupo

<b>v0.00.3</b>
- Agregado NPC guardianes de mazmorras con sus respectivos dialogos
- Movimiento de monstruos en los mapas solamente donde hay personajes online
- Reparado error en configuracion de la estructura en estaticos
- Parche con la totalidad de mapas en estaticos
- Pequeña modificacion en la organizacion de los logs
- Nuevo sistema de publicidad automatica
- Configurable desde la config.txt tiempo de impresion de publicidades
- Nuevo comando de administrador nivel 4 - agregar_publicidad

<b>v0.00.4</b>
- Timmer de commit para SQL anulado ya que Hikari lo hace automaticamente
- Gestor de SQL en kotlin
- Comandos admin.txt agregado  la carpeta recursos
- Ediciones en la config.txt (Limite de mapas, version del cliente e ignorar version del cliente)
- Agregada ID del personaje en la tabla datos_objetos, columna nueva dueño
- Cargar objetos de la columna objetos de personaje desde la ID del dueño

<b>v0.00.5</b>
- Reparado bug de yopuka al estar en estado portado y lanzar salto el estado portado no es eliminado
- Reparado bug de levantar a alguien en estado portador o portado
- Salir del estado portador cuando el panda lanza a un jugador lleva 0ms de espera, instantaneo
- Agregada la verificacion de trampas en la celda al lanzar un personaje
- Agregado break; faltante en efecto hechizo
- Recambio de accion de las trampas de 307 a 306, con delay correcto del cliente
