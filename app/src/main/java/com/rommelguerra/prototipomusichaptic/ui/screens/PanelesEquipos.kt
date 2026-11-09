package com.rommelguerra.prototipomusichaptic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rommelguerra.prototipomusichaptic.integracion.*
import com.tesis.audiotohaptics.config.Actuator
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PanelCotidianoEquipo(vm: IntegracionViewModel, onEscuchar: () -> Unit, onConectar: () -> Unit, onProbar: (String) -> Unit) {
    val conectado by vm.bluetooth.conectado.collectAsState()
    val ocupado by vm.bluetooth.ocupado.collectAsState()
    Text("Sonidos cotidianos", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF18283A))
    Text("Elige un contexto y explora las señales que tu háptica puede reconocer.", color = Color(0xFF526477))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("CONTEXTO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF096B60), letterSpacing = 1.2.sp)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            vm.catalogo.contextos.forEach { contexto ->
                val (icon, accent) = contextoStyle(contexto)
                FilterChip(
                    selected = vm.contexto == contexto,
                    onClick = { vm.cambiarContexto(contexto) },
                    label = { Text(contexto.replaceFirstChar(Char::uppercaseChar), fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("contexto_$contexto"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accent,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18283A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF096B60)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text("Micrófono del entorno", fontWeight = FontWeight.Bold, color = Color.White)
                    Text(when {
                        vm.nivelMicrofono != null -> "Activo · recibiendo audio"
                        vm.escuchando -> "Esperando las primeras muestras…"
                        else -> "Apagado · sin escucha"
                    }, color = Color(0xFFB9C7D5), fontSize = 13.sp)
                }
                Switch(
                    checked = vm.escuchando,
                    onCheckedChange = { activar -> if (activar != vm.escuchando) onEscuchar() },
                    enabled = vm.escuchando || !vm.analizando,
                    modifier = Modifier.testTag("microfono_entorno").semantics { contentDescription = "Escucha del entorno con micrófono" },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00D166))
                )
            }
            val nivel = vm.nivelMicrofono
            Text(when {
                nivel == null -> "Nivel del micrófono · —"
                nivel <= -70f -> "Nivel del micrófono · señal muy baja"
                else -> "Nivel del micrófono · ${nivel.roundToInt()} dBFS"
            }, color = Color(0xFFD9E3EC), style = MaterialTheme.typography.labelLarge)
            LinearProgressIndicator(
                progress = { ((nivel ?: -70f).coerceIn(-70f, 0f) + 70f) / 70f },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)).testTag("nivel_microfono"),
                color = Color(0xFF00D166),
                trackColor = Color(0xFF304253)
            )
        }
    }
    if (vm.escuchando) {
        MicrophoneWaveform(vm.nivelMicrofono)
    }
    if (vm.mensajeCotidiano.isNotBlank()) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFE7F5EF)) {
            Text(vm.mensajeCotidiano, modifier = Modifier.padding(14.dp), color = Color(0xFF096B60), fontSize = 13.sp)
        }
    }
    if (vm.escuchando && !vm.notificacionesActivas) {
        Text("Las notificaciones están desactivadas. Puedes activarlas en los ajustes de Android.", fontSize = 13.sp)
    }
    ResumenHaptica(conectado, ocupado, onConectar)
    Text("Configuración de avisos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF18283A))
    Text("Elige el firmware instalado en tu háptica.", fontSize = 13.sp, color = Color(0xFF526477))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PerfilEntorno.entries.forEach { perfil ->
            FilterChip(selected = vm.perfil == perfil, onClick = { vm.cambiarPerfil(perfil) }, label = { Text(perfil.etiqueta) })
        }
    }
    OutlinedButton(onClick = { vm.detenerAviso() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Icon(Icons.Default.Stop, null)
        Spacer(Modifier.width(8.dp))
        Text("Detener señal")
    }
    Text("Intensidad del teléfono · ${(vm.intensidad * 100).toInt()} %", fontWeight = FontWeight.Bold, color = Color(0xFF18283A))
    Slider(value = vm.intensidad, onValueChange = { vm.intensidad = it }, valueRange = 0.1f..1f)
    Text("La intensidad de la háptica depende de su firmware.", fontSize = 13.sp, color = Color(0xFF526477))
    if (vm.mensajeSalida.isNotBlank()) Text(vm.mensajeSalida, color = Color(0xFF096B60))
    Text("Sonidos en ${vm.contexto.replaceFirstChar(Char::uppercaseChar)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF18283A))
    Text("Toca una tarjeta para probar el aviso disponible.", fontSize = 13.sp, color = Color(0xFF526477))
    vm.casos.chunked(2).forEachIndexed { index, fila ->
        var visible by remember(vm.contexto, index) { mutableStateOf(false) }
        LaunchedEffect(vm.contexto, index) {
            delay(index * 45L)
            visible = true
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                )
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { caso ->
                    SoundAlertCard(caso, vm.pruebaVisual == caso.clase, onProbar, Modifier.weight(1f))
                }
                if (fila.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
    Text("Las demostraciones manuales quedan fuera del historial de detecciones.", fontSize = 13.sp)
    if (vm.historial.isNotEmpty()) {
        Text("Detecciones de esta sesión", fontWeight = FontWeight.Bold)
        vm.historial.take(8).forEach { Text(it) }
    }
}

@Composable
private fun MicrophoneWaveform(nivelMicrofono: Float?) {
    val nivel = nivelMicrofono ?: -70f
    // La onda no cambia entre dos estados. El nivel pasa gradualmente desde
    // la línea de reposo hasta la amplitud del audio recibido.
    val actividadObjetivo = ((nivel + 58f) / 40f).coerceIn(0f, 1f)
    val actividad by animateFloatAsState(
        targetValue = actividadObjetivo,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "wave_activity"
    )
    val barras = listOf(0.32f, 0.52f, 0.78f, 0.46f, 0.68f, 0.92f, 0.58f, 0.82f, 0.43f, 0.72f, 0.95f, 0.55f, 0.76f, 0.40f, 0.64f, 0.88f, 0.50f, 0.70f, 0.36f, 0.58f, 0.80f, 0.45f, 0.68f, 0.34f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Escuchando en tiempo real", color = Color(0xFF18283A), fontWeight = FontWeight.Bold)
                Text("MIC", color = Color(0xFF096B60), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Row(Modifier.fillMaxWidth().height(58.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                barras.forEachIndexed { index, base ->
                    val alturaObjetivo = (5 + base * 3 + 48 * base * actividad).coerceAtMost(56f).dp
                    val altura by animateDpAsState(
                        targetValue = alturaObjetivo,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "wave_height_$index"
                    )
                    val reposo = Color(0xFFB8C5D2)
                    val activo = if (index % 3 == 0) Color(0xFF5B6EF5) else Color(0xFF00B978)
                    val intensidad = (actividad * 1.35f).coerceIn(0f, 1f)
                    val color = Color(
                        red = reposo.red + (activo.red - reposo.red) * intensidad,
                        green = reposo.green + (activo.green - reposo.green) * intensidad,
                        blue = reposo.blue + (activo.blue - reposo.blue) * intensidad,
                        alpha = 1f
                    )
                    Box(
                        Modifier.weight(1f).height(altura)
                            .clip(RoundedCornerShape(4.dp)).background(color)
                    )
                }
            }
            Text("La forma de onda representa la actividad del micrófono.", color = Color(0xFF526477), fontSize = 12.sp)
        }
    }
}

private fun contextoStyle(contexto: String) = when (contexto) {
    "hogar" -> Icons.Default.Home to Color(0xFF6C63D9)
    "urbano" -> Icons.Default.LocationCity to Color(0xFF3E72B8)
    "social" -> Icons.Default.Groups to Color(0xFFD56A5C)
    "industrial" -> Icons.Default.PrecisionManufacturing to Color(0xFFB27A32)
    else -> Icons.Default.Mic to Color(0xFF096B60)
}

@Composable
private fun SpringAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "feedforward_press"
    )
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun SoundAlertCard(
    caso: CasoCotidiano,
    muestraPrueba: Boolean,
    onProbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (icono, color) = sonidoStyle(caso.clase)
    Card(
        modifier = modifier.fillMaxWidth().testTag("caso_${caso.clase}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = .20f)), contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Text(CatalogoCotidianoEquipo.etiqueta(caso.clase), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            if (caso.entrenado) {
                Text("Prioridad ${caso.prioridad} · ${(caso.umbral * 100).toInt()} %", color = Color.White.copy(alpha = .82f), fontSize = 11.sp)
                if (CatalogoCotidianoEquipo.sonido(caso.clase) == null) {
                    Text("Aviso visual en el teléfono", color = Color.White.copy(alpha = .84f), fontSize = 11.sp)
                }
                SpringAction(
                    text = if (CatalogoCotidianoEquipo.sonido(caso.clase) == null) "Probar aviso" else "Probar señal",
                    onClick = { onProbar(caso.clase) },
                    modifier = Modifier.testTag("probar_${caso.clase}")
                )
                if (muestraPrueba) {
                    AvisoSonidoCompacto(caso.clase)
                }
            } else {
                Text("Pendiente de entrenamiento", color = Color.White.copy(alpha = .78f), fontSize = 12.sp)
            }
        }
    }
}

private fun sonidoStyle(clase: String) = when (clase) {
    "ladrido" -> Icons.Default.Pets to Color(0xFF9A5B24)
    "llanto_bebe" -> Icons.Default.ChildCare to Color(0xFF8051A8)
    "vidrio_roto" -> Icons.Default.Warning to Color(0xFF3E72B8)
    "sirena" -> Icons.Default.Campaign to Color(0xFFC4514D)
    "claxon" -> Icons.Default.DirectionsCar to Color(0xFF26866D)
    "maquinaria" -> Icons.Default.PrecisionManufacturing to Color(0xFF667085)
    else -> Icons.Default.ReportProblem to Color(0xFF59616B)
}

@Composable
private fun AvisoSonidoCompacto(clase: String) {
    val (icono, _) = sonidoStyle(clase)
    val pulse = remember { Animatable(1f) }
    val movimiento = remember { Animatable(0f) }
    LaunchedEffect(clase) {
        while (isActive) {
            pulse.animateTo(1.16f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            pulse.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            val destino = when (clase) {
                "llanto_bebe" -> -6f
                "ladrido", "claxon" -> 5f
                else -> 0f
            }
            if (destino != 0f) {
                movimiento.animateTo(destino, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
                movimiento.animateTo(0f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("aviso_$clase").semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = .16f)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icono,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp).graphicsLayer {
                    scaleX = pulse.value
                    scaleY = pulse.value
                    translationY = movimiento.value
                    translationX = if (clase == "sirena") movimiento.value else 0f
                }
            )
            Column(Modifier.weight(1f)) {
                Text("Aviso activo", color = Color.White.copy(alpha = .78f), fontSize = 11.sp)
                Text(CatalogoCotidianoEquipo.etiqueta(clase), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AvisoSonido(clase: String, encabezado: String, detalle: String, animar: Boolean = true) {
    val (icono, color) = when (clase) {
        "ladrido" -> Icons.Default.Pets to Color(0xFF7A4700)
        "llanto_bebe" -> Icons.Default.ChildCare to Color(0xFF6A348C)
        "vidrio_roto" -> Icons.Default.Warning to Color(0xFF18588B)
        "sirena" -> Icons.Default.Campaign to Color(0xFFA5252B)
        "claxon" -> Icons.Default.DirectionsCar to Color(0xFF17614B)
        "maquinaria" -> Icons.Default.PrecisionManufacturing to Color(0xFF454D63)
        else -> Icons.Default.ReportProblem to Color(0xFF3F4752)
    }
    val titulo = when (clase) {
        "ladrido" -> "Ladrido de perro"
        "sirena" -> "Sirena de emergencia"
        else -> CatalogoCotidianoEquipo.etiqueta(clase)
    }
    val pulse = remember { Animatable(1f) }
    val bounce = remember { Animatable(0f) }
    LaunchedEffect(animar, clase) {
        if (!animar) {
            pulse.snapTo(1f)
            bounce.snapTo(0f)
        } else {
            while (isActive) {
                pulse.animateTo(1.12f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                pulse.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                if (clase == "llanto_bebe") {
                    bounce.animateTo(-7f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
                    bounce.animateTo(0f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
                }
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("aviso_$clase").semantics { liveRegion = LiveRegionMode.Polite },
        shape = MaterialTheme.shapes.medium,
        color = color,
        contentColor = Color.White
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(36.dp).graphicsLayer { scaleX = pulse.value; scaleY = pulse.value; translationY = bounce.value })
            Column(Modifier.weight(1f)) {
                Text(encabezado, style = MaterialTheme.typography.labelLarge)
                Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(detalle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun PanelMusicalEquipo(vm: IntegracionViewModel, listo: Boolean, onAnalizar: () -> Unit, onEscucharResultado: (File) -> Unit) {
    HorizontalDivider()
    Text("Audio a señales hápticas", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Text("La biblioteca separa voz, guitarra y percusión, y genera un WAV sincronizado para cada canal.")
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Estado de los modelos", fontWeight = FontWeight.Bold)
            Text("Basic Pitch: ${if (vm.basicPitchInstalado) "incluido" else "no incluido"}")
            Text("Demucs: ${if (vm.demucsInstalado) "incluido" else "no incluido"}")
            if (!vm.demucsInstalado) Text("El análisis requiere el modelo Demucs que falta. No se descarga ni se genera desde la app.")
        }
    }
    Row {
        Checkbox(checked = vm.usarValoresPrueba, enabled = !vm.analizando, onCheckedChange = { vm.usarValoresPrueba = it })
        Text("Usar configuración de prueba: 150/200/250 Hz y 44,1 kHz. Son valores del equipo sin confirmar para los actuadores.", Modifier.padding(top = 8.dp))
    }
    Row {
        Checkbox(checked = vm.exportarFuentes, enabled = !vm.analizando, onCheckedChange = { vm.exportarFuentes = it })
        Text("Guardar también voz, guitarra y batería separadas", Modifier.padding(top = 8.dp))
    }
    if (!vm.usarValoresPrueba) Text("La configuración oficial de los actuadores está pendiente.", fontSize = 13.sp)
    Button(enabled = listo && vm.musicaDisponible && vm.usarValoresPrueba && !vm.analizando,
        onClick = onAnalizar, modifier = Modifier.fillMaxWidth()) { Text("Analizar música") }
    if (vm.analizando) {
        LinearProgressIndicator(progress = vm.progresoMusica, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = { vm.cancelarAnalisis() }) { Text("Cancelar análisis") }
    }
    Text(vm.mensajeMusica)
    Text("Los WAV representan ritmo y duración con una portadora fija por canal. Esta integración no transmite los tres canales musicales al ESP32.", fontSize = 13.sp)
    vm.resultadoMusica?.let { result ->
        Text("Resultados del análisis", fontWeight = FontWeight.Bold)
        Text("Duración: ${"%.1f".format(result.durationS)} s · configuración ${if (result.configConfirmed) "confirmada" else "de prueba"}")
        result.outputs.forEach { (actuador, output) ->
            val nombre = when (actuador) { Actuator.VOICE -> "Voz"; Actuator.GUITAR -> "Guitarra"; Actuator.PERCUSSION -> "Percusión" }
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(nombre, fontWeight = FontWeight.Bold)
                    Text("${output.eventCount} eventos · ${output.carrierHz} Hz · ${output.status}")
                    Text("WAV: ${output.wavFile.name}")
                    output.midiFile?.let { Text("MIDI: ${it.name}") }
                    Text("Eventos: ${output.eventsFile.name}")
                    TextButton(onClick = { onEscucharResultado(output.wavFile) }) { Text("Escuchar señal de $nombre") }
                }
            }
        }
        result.stemFiles.forEach { (nombre, file) -> TextButton(onClick = { onEscucharResultado(file) }) { Text("Escuchar fuente: $nombre") } }
        Text("Resultados guardados en: ${result.outputDirectory}", fontSize = 12.sp)
        result.warnings.forEach { Text(it, fontSize = 13.sp) }
    }
    if (vm.mensajeVistaPrevia.isNotBlank()) {
        Text(vm.mensajeVistaPrevia)
        OutlinedButton(onClick = { vm.detenerVistaPrevia() }) { Text("Detener vista previa") }
    }
}
