<p align="center">
  <img src="src/main/resources/assets/zombie-infection/icon.png" width="128" alt="Icono de Zombie Infection">
</p>

<h1 align="center">Zombie Infection</h1>

<p align="center">
  Survival horror, infección progresiva e infectados especiales para Minecraft Fabric.
</p>

<p align="center">
  <img alt="Minecraft 1.21.8" src="https://img.shields.io/badge/Minecraft-1.21.8-62B47A">
  <img alt="Fabric" src="https://img.shields.io/badge/Loader-Fabric-DBD0B4">
  <img alt="Java 21" src="https://img.shields.io/badge/Java-21-E76F00">
  <img alt="Versión 0.4.0 Alpha" src="https://img.shields.io/badge/versión-0.4.0%20Alpha-D97706">
  <img alt="Estado no estable" src="https://img.shields.io/badge/estado-NO%20ESTABLE-B91C1C">
  <img alt="Licencia CC0 1.0" src="https://img.shields.io/badge/licencia-CC0--1.0-2563EB">
</p>

> [!WARNING]
> Zombie Infection se encuentra en **Alpha y NO ES ESTABLE**. La versión actual contiene errores conocidos que pueden volver el mod difícil de jugar o prácticamente injugable. No se recomienda utilizarla en mundos importantes ni en servidores de producción. Haz una copia de seguridad antes de probarla; su contenido, balance y datos pueden cambiar de forma incompatible en futuras versiones.

## Descripción

Zombie Infection transforma el Overworld en un brote zombi progresivo. El jugador puede contraer una infección persistente, sufrir síntomas cada vez más graves, extraer muestras biológicas de los infectados y producir tratamientos en un laboratorio médico.

La población hostil natural del Overworld está formada principalmente por los infectados del mod. Runner, Bloater y Spitter utilizan IA basada en SmartBrainLib, visión real, memoria temporal e investigación de ruidos; los monstruos hostiles vanilla se encuentran desactivados por defecto sin eliminar sus registros ni impedir su uso mediante comandos o contenido especial.

## Características actuales

### Sistema de infección

- Infección de **0 a 100 %**, persistente al salir y volver a entrar al mundo.
- Cinco estados visibles: sano, expuesto, infección temprana, moderada y grave.
- Síntomas progresivos desde el 25 %: hambre, debilidad, náuseas, lentitud y daño en fases avanzadas.
- Muerte al alcanzar el 100 % de infección.
- Lógica autoritativa en el servidor y datos sincronizados con el cliente.
- HUD compacto con porcentaje y avisos de cambio de etapa.
- Monitor epidemiológico compacto con estado, tratamiento y telemetría del brote.
- Acceso rápido al monitor con la tecla **I** o desde el inventario.

### Infectados especiales

| Infectado | Comportamiento | Amenaza especial |
| --- | --- | --- |
| **Runner** | Muy rápido, audición alta, persecución agresiva y memoria corta | Cierra distancias rápidamente y puede transmitir infección con sus golpes |
| **Bloater** | Lento, resistente, difícil de desplazar y persistente | Libera una nube infecciosa temporal al morir |
| **Spitter** | Combate a distancia, mantiene espacio y se reposiciona | Dispara un proyectil infeccioso que crea una pequeña zona contaminada al impactar |

Los tres infectados:

- detectan jugadores mediante visión con line of sight real;
- recuerdan temporalmente la última posición observada;
- pierden al objetivo cuando consigue escapar;
- investigan ruidos cercanos y regresan a patrullar si no encuentran nada;
- no se atacan entre sí como objetivos normales;
- cuentan con huevos generadores y loot propio.

El Spitter comprueba orientación frontal, distancia, visión y cooldown antes de cada disparo. Su rango máximo es de aproximadamente 14 bloques y genera un solo proyectil por ataque.

### Ruido y percepción

Los infectados reaccionan actualmente a:

- jugadores corriendo;
- bloques rotos;
- explosiones.

También existe una API preparada para futuros disparos normales y con silenciador. Las armas todavía no forman parte de esta versión.

### Outbreak Level

El nivel del brote se calcula a partir de los días transcurridos en el Overworld. Cada etapa modifica tanto la densidad total como la composición de infectados.

| Nivel | Desde el día | Runner | Bloater | Spitter | Densidad | Límite local |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 0 — Contenido | 0 | 48 | 18 | 14 | 22 % | 6 |
| 1 — Emergente | 3 | 48 | 22 | 18 | 32 % | 9 |
| 2 — En propagación | 7 | 46 | 25 | 22 | 46 % | 13 |
| 3 — Grave | 15 | 43 | 27 | 24 | 62 % | 18 |
| 4 — Crítico | 30 | 40 | 29 | 26 | 78 % | 24 |

Los valores de Runner, Bloater y Spitter son pesos efectivos relativos, no cantidades garantizadas. El límite local cuenta los infectados especiales vivos en una zona de 72 bloques alrededor de cada intento natural; evita que el monster cap termine llenando el área con Runners aunque la probabilidad individual sea baja. Los comandos y huevos generadores no están sujetos a este límite.

### Extracción, loot y materiales

- El **Kit de extracción** tiene 32 usos y permite obtener tejido infectado o muestras de sangre al eliminar criaturas compatibles.
- Runner puede soltar una **glándula suprarrenal**.
- Bloater puede soltar un **saco de toxinas**.
- Spitter puede soltar una **enzima reactiva**.
- Los materiales especiales utilizan loot tables normales y admiten Looting de forma limitada.
- La extracción con kit y el loot propio de cada infectado son sistemas separados.

### Laboratorio médico

El laboratorio es un bloque funcional con inventario persistente, slots filtrados, progreso de producción, salida protegida e integración opcional con REI.

| Producción | Material principal | Reactivo | Catalizador |
| --- | --- | --- | --- |
| Extracto viral | Tejido o sangre infectada | Redstone | Botella de vidrio |
| Compuesto antiviral | Extracto viral | Azúcar | Botella de miel |
| Antiviral básico | Compuesto antiviral | Zanahoria dorada | Botella de vidrio |
| Inyección antiviral | Antiviral básico | Lingote de hierro | Lágrima de ghast |
| Cura | Inyección antiviral | Manzana dorada | Lágrima de ghast |
| Inyector de adrenalina | Glándula de Runner | Azúcar | Botella de vidrio |
| Supresor de infección | Saco de Bloater | Enzima de Spitter | Botella de miel |

Tratamientos disponibles:

- **Antiviral básico:** reduce 15 puntos de infección.
- **Inyección antiviral:** reduce 35 puntos.
- **Cura:** elimina por completo la infección.
- **Inyector de adrenalina:** otorga Speed I durante 45 segundos y Strength I durante 15 segundos; no cura la infección.
- **Supresor de infección:** evita temporalmente nuevas aplicaciones de infección durante 3 minutos; no elimina la infección existente.

### Mundo

- Generación natural de Runner, Bloater y Spitter en biomas del Overworld.
- Hostiles vanilla naturales desactivados por defecto en el Overworld.
- Animales, aldeanos, peces, criaturas ambientales y mobs neutrales permanecen habilitados.
- Nether y End conservan su comportamiento normal.
- **Clínicas abandonadas** y **farmacias abandonadas** con loot médico y materiales útiles, distribuidas con una frecuencia apta para exploración normal.
- Soporte de idioma para inglés, español y español de México.

Los cambios de distribución de estructuras solo se aplican a chunks que todavía no se hayan generado. En un mundo existente, explora terreno nuevo o utiliza `/locate structure zombie-infection:abandoned_clinic` y `/locate structure zombie-infection:abandoned_pharmacy` para confirmar el registro y buscar candidatos.

## Compatibilidad y requisitos

| Componente | Requisito |
| --- | --- |
| Minecraft | **1.21.8** |
| Java | **21 o superior** |
| Fabric Loader | **0.19.3 o compatible** |
| Fabric API | **0.136.1+1.21.8 o compatible** |
| SmartBrainLib | **1.16.10 incluido dentro del JAR de Zombie Infection** |
| Roughly Enough Items | Opcional, recomendado para consultar las recetas del laboratorio |

SmartBrainLib no necesita descargarse por separado: la compilación compatible utilizada por el mod está anidada en el JAR. REI solo funciona como visor de recetas y no es necesario para que el laboratorio produzca objetos.

Zombie Infection debe instalarse tanto en el cliente como en el servidor dedicado. REI puede instalarse únicamente en los clientes que quieran utilizar su visor.

## Instalación

1. Instala [Fabric Loader](https://fabricmc.net/use/installer/) para Minecraft 1.21.8.
2. Descarga e instala [Fabric API](https://modrinth.com/mod/fabric-api).
3. Descarga Zombie Infection desde la sección de [Releases](https://github.com/worddevs/Zombie-Infection/releases) o compílalo desde el código fuente.
4. Coloca los JAR de Zombie Infection y Fabric API en `.minecraft/mods`.
5. Opcionalmente, instala [Roughly Enough Items](https://modrinth.com/mod/rei) y sus dependencias para consultar las recetas.
6. Inicia Minecraft con el perfil de Fabric.

En un servidor dedicado, repite la instalación dentro de la carpeta `mods` del servidor y utiliza Java 21.

## Controles

| Acción | Tecla predeterminada |
| --- | --- |
| Abrir el monitor de infección | **I** |

La tecla puede cambiarse desde el menú normal de controles de Minecraft. El monitor también dispone de un botón en las pantallas de inventario y creativo.

## Comandos

| Comando | Permiso | Función |
| --- | ---: | --- |
| `/infection` | Jugador | Consulta el porcentaje de infección propio |
| `/infection set <player> <0-100>` | Nivel 2 | Establece la infección de un jugador |
| `/infection add <player> <amount>` | Nivel 2 | Añade infección a un jugador |
| `/infection clear <player>` | Nivel 2 | Elimina la infección de un jugador |

## Configuración del servidor

En el primer inicio se crea `config/zombie-infection-spawns.properties`:

```properties
allowVanillaHostileSpawns=false
spawnVanillaZombies=false
spawnSkeletons=false
spawnCreepers=false
```

Con `allowVanillaHostileSpawns=false`, los monstruos vanilla no aparecen naturalmente en el Overworld. Al establecerlo en `true`, los otros tres valores permiten controlar por separado las familias de zombies, skeletons y creepers. Reinicia el servidor después de editar el archivo.

La configuración no elimina EntityTypes: `/summon`, spawners, estructuras y otros mods pueden seguir utilizando entidades vanilla.

## Compilar desde el código fuente

Clona el repositorio:

```bash
git clone https://github.com/worddevs/Zombie-Infection.git
cd Zombie-Infection
```

En Windows:

```powershell
.\gradlew.bat clean build
```

En Linux o macOS:

```bash
./gradlew clean build
```

El JAR remapeado se genera en `build/libs/`. El proyecto ya contiene la dependencia compatible de SmartBrainLib utilizada para construir y empaquetar el mod.

## Reportar errores

Utiliza [GitHub Issues](https://github.com/worddevs/Zombie-Infection/issues) e incluye:

1. versión de Zombie Infection, Minecraft, Fabric Loader y Fabric API;
2. lista de mods instalados;
3. pasos exactos para reproducir el problema;
4. `latest.log` o crash report completo;
5. capturas o video cuando ayuden a entender el fallo.

## Contribuciones

Las contribuciones son bienvenidas. Para cambios de código:

1. crea un fork del repositorio;
2. abre una rama descriptiva;
3. mantén el cambio limitado a un objetivo;
4. ejecuta `gradlew clean build`;
5. abre un Pull Request explicando el comportamiento anterior, el nuevo y cómo se validó.

## Cambios de la versión 0.4.0

- Monitor de infección oscuro y adaptable, separado del inventario vanilla y compatible con interfaces de otros mods.
- Menú principal tematizado como red de cuarentena, conservando los botones y acciones de Minecraft y Mod Menu.
- Regreso correcto al inventario de supervivencia o creativo después de cerrar el monitor.
- Eliminación del listener de ratón duplicado del botón de acceso médico.
- Límite local progresivo para impedir acumulaciones masivas de infectados especiales.
- Grupos naturales más pequeños y composición inicial con menos predominio de Runners.
- Clínicas y farmacias abandonadas considerablemente más fáciles de encontrar durante exploración normal.
- Estado Outbreak ampliado con información sobre el protocolo de contención local.
- Recursos y traducciones revisados en inglés, español y español de México.

## Créditos

- **Creación, dirección y diseño:** [Carlos Hernández (@Carloshdz22)](https://github.com/Carloshdz22).
- **Desarrollo y asistencia técnica:** Codex IA de OpenAI.
- **IA de entidades:** [SmartBrainLib](https://github.com/Tslat/SmartBrainLib), creada por Tslat.
- **Plataforma de modding:** [Fabric](https://fabricmc.net/).
- **Visor opcional de recetas:** [Roughly Enough Items](https://github.com/shedaniel/RoughlyEnoughItems).

## Licencia

Este proyecto se distribuye bajo [CC0 1.0 Universal](LICENSE). Puedes usarlo, modificarlo y distribuirlo, incluso comercialmente, conforme a los términos de la licencia.

---

<p align="center">
  Mantén la calma, prepara el laboratorio y no hagas ruido.
</p>
