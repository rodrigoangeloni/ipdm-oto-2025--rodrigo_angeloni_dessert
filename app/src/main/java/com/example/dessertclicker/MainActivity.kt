package com.example.dessertclicker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.dessertclicker.data.Datasource
import com.example.dessertclicker.model.Dessert
import com.example.dessertclicker.ui.theme.DessertClickerTheme

// Etiqueta para registro de logs
private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate Llamado")
        setContent {
            DessertClickerTheme {
                // Un contenedor de superficie que usa el color de 'fondo' del tema
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                ) {
                    DessertClickerApp(postres = Datasource.dessertList)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart Llamado")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume Llamado")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart Llamado")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause Llamado")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop Llamado")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy Llamado")
    }
}

/**
 * Determina qué postre mostrar.
 */
fun determinarPostreAMostrar(
    postres: List<Dessert>,
    postresVendidos: Int
): Dessert {
    var postreAMostrar = postres.first()
    for (postre in postres) {
        if (postresVendidos >= postre.startProductionAmount) {
            postreAMostrar = postre
        } else {
            // La lista de postres está ordenada por startProductionAmount. A medida que vendes más postres,
            // comenzarás a producir postres más caros según lo determinado por startProductionAmount.
            // Sabemos que hay que romper tan pronto como veamos un postre cuyo "startProductionAmount" es mayor
            // que la cantidad vendida.
            break
        }
    }

    return postreAMostrar
}

/**
 * Comparte la información de postres vendidos usando el intent ACTION_SEND
 */
private fun compartirInformacionPostresVendidos(contextoIntent: Context, postresVendidos: Int, ingresos: Int) {
    val enviarIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            contextoIntent.getString(R.string.share_text, postresVendidos, ingresos)
        )
        type = "text/plain"
    }

    val intentCompartir = Intent.createChooser(enviarIntent, null)

    try {
        ContextCompat.startActivity(contextoIntent, intentCompartir, null)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            contextoIntent,
            contextoIntent.getString(R.string.sharing_not_available),
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
private fun DessertClickerApp(
    postres: List<Dessert>
) {

    var ingresos by rememberSaveable { mutableStateOf(0) }
    var postresVendidos by rememberSaveable { mutableStateOf(0) }

    val indicePostreActual by rememberSaveable { mutableStateOf(0) }

    var precioPostreActual by rememberSaveable {
        mutableStateOf(postres[indicePostreActual].price)
    }
    var idImagenPostreActual by rememberSaveable {
        mutableStateOf(postres[indicePostreActual].imageId)
    }

    Scaffold(
        topBar = {
            val contextoIntent = LocalContext.current
            val direccionLayout = LocalLayoutDirection.current
            BarraAppDessertClicker(
                onBotonCompartirClick = {
                    compartirInformacionPostresVendidos(
                        contextoIntent = contextoIntent,
                        postresVendidos = postresVendidos,
                        ingresos = ingresos
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateStartPadding(direccionLayout),
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(direccionLayout),
                    )
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingContenido ->
        PantallaDessertClicker(
            ingresos = ingresos,
            postresVendidos = postresVendidos,
            idImagenPostre = idImagenPostreActual,
            onPostreClick = {

                // Actualizar los ingresos
                ingresos += precioPostreActual
                postresVendidos++

                // Mostrar el siguiente postre
                val postreAMostrar = determinarPostreAMostrar(postres, postresVendidos)
                idImagenPostreActual = postreAMostrar.imageId
                precioPostreActual = postreAMostrar.price
            },
            modifier = Modifier.padding(paddingContenido)
        )
    }
}

@Composable
private fun BarraAppDessertClicker(
    onBotonCompartirClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(
            onClick = onBotonCompartirClick,
            modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_medium)),
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.share),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun PantallaDessertClicker(
    ingresos: Int,
    postresVendidos: Int,
    @DrawableRes idImagenPostre: Int,
    onPostreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.bakery_back),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(idImagenPostre),
                    contentDescription = null,
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.image_size))
                        .height(dimensionResource(R.dimen.image_size))
                        .align(Alignment.Center)
                        .clickable { onPostreClick() },
                    contentScale = ContentScale.Crop,
                )
            }
            InfoTransaccion(
                ingresos = ingresos,
                postresVendidos = postresVendidos,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    }
}

@Composable
private fun InfoTransaccion(
    ingresos: Int,
    postresVendidos: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        InfoPostresVendidos(
            postresVendidos = postresVendidos,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        )
        InfoIngresos(
            ingresos = ingresos,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        )
    }
}

@Composable
private fun InfoIngresos(ingresos: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.total_revenue),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "$${ingresos}",
            textAlign = TextAlign.Right,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun InfoPostresVendidos(postresVendidos: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.dessert_sold),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = postresVendidos.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Preview
@Composable
fun VistaPreviewDessertClickerApp() {
    DessertClickerTheme {
        DessertClickerApp(listOf(Dessert(R.drawable.cupcake, 5, 0)))
    }
}
