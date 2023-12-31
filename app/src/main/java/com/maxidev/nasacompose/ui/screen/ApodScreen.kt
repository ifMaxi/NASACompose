package com.maxidev.nasacompose.ui.screen

/* Created by Pelizzoni Maximiliano on 23/11/2023 */

import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Copyright
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.VideoFrameDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.maxidev.nasacompose.R
import com.maxidev.nasacompose.data.download.AndroidDownloader
import com.maxidev.nasacompose.data.model.apodmodel.ApodModel
import com.maxidev.nasacompose.data.network.ApiResponse
import com.maxidev.nasacompose.ui.components.AppTopBar
import com.maxidev.nasacompose.ui.theme.NASAComposeTheme
import com.maxidev.nasacompose.ui.viewmodel.NasaViewModel

// Shows the app bar and on-screen content.
@Composable
fun ApodScreen(
    modifier: Modifier = Modifier,
    viewmodel: NasaViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { AppTopBar(title = R.string.top_bar_apod_title) }
    ) { paddingValues ->
        LaunchedEffect(true) {
            viewmodel.apod
        }

        ApodNetStatus(
            apiResponse = viewmodel.apod,
            modifier = modifier.padding(paddingValues)
        )
    }
}

// Simulate the check Network status.
@Composable
private fun ApodNetStatus(
    apiResponse: ApiResponse,
    modifier: Modifier = Modifier
) {
    when (apiResponse) {
        is ApiResponse.Loading -> Loading()
        is ApiResponse.Success -> ApodScreenModel(
            apodModel = apiResponse.data as ApodModel,
            modifier = modifier
        )
        is ApiResponse.Error -> ConnectionError(errorText = R.string.connection_problems)
    }
}

// It will use the parameters of the data model to obtain the data from the API.
@Composable
private fun ApodScreenModel(
    apodModel: ApodModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloader = AndroidDownloader(context)

    ApodScreenContent(
        image = apodModel.url,
        title = apodModel.title,
        description = apodModel.explanation,
        date = apodModel.date,
        copyright = apodModel.copyright.toString(),
        modifier = modifier,
        onDownload = {
            downloader.downloadFile(apodModel.url)
            Toast.makeText(context, R.string.downloading_toast, Toast.LENGTH_SHORT).show()
        }
    )
}

// It will be the main screen of the app.
// It will render the Title, Image, Description and Date composables.
@Composable
private fun ApodScreenContent(
    image: String,
    title: String,
    description: String,
    date: String,
    copyright: String,
    modifier: Modifier = Modifier,
    onDownload: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = 25.dp,
                start = 6.dp,
                bottom = 6.dp,
                end = 6.dp
            ),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ApodTitleOfTheDay(title = title)
        ApodCard(
            image = image,
            description = description,
            contentDescription = date,
            copyright = copyright,
            onDownload = onDownload
        )
        ApodDateOfTheDay(date = date)
    }
}

// Card component.
// It will show the image and description of the day.
// It will have the "expand" animation that will show and hide the information.
@Composable
private fun ApodCard(
    image: String,
    description: String,
    contentDescription: String,
    copyright: String,
    onDownload: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val elevation = CardDefaults.cardElevation(6.dp)
    val cardColor = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .padding(10.dp),
        elevation = elevation,
        colors = cardColor,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState),
            content = {
                ApodImageOfTheDay(
                    image = image,
                    contentDescription = contentDescription,
                    onDownload = onDownload
                )
                ApodExpandableButton(
                    onClick = { expanded = !expanded },
                    expanded = expanded,
                    info = R.string.info
                )
                if (expanded) {
                    ApodDescriptionOfTheDay(description = description)
                    ApodPhotoAndCopyright(copyright = copyright)
                }
            }
        )
    }
}

// The title of the image. It will change every day.
@Composable
private fun ApodTitleOfTheDay(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

// It will display an image daily by calling the Api.
// Use the Coil library to get the photo from the api.
@Composable
private fun ApodImageOfTheDay(
    image: String,
    contentDescription: String,
    onDownload: () -> Unit
) {
    val context = LocalContext.current
    val cachePolicy = CachePolicy.ENABLED

    val imageRequest = ImageRequest.Builder(context)
        .data(image)
        .crossfade(true)
        .decoderFactory(GifDecoder.Factory())
        .decoderFactory(VideoFrameDecoder.Factory())
        .memoryCachePolicy(cachePolicy)
        .diskCachePolicy(cachePolicy)
        .build()

    Box(
        modifier = Modifier
            .padding(10.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(5))
        )
        Icon(
            imageVector = Icons.Outlined.FileDownload,
            contentDescription = "Download Image",
            tint = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .padding(10.dp)
                .clickable { onDownload() }
        )
    }
}

// Photo Description.
// Useful information explained by a NASA professional.
@Composable
private fun ApodDescriptionOfTheDay(description: String) {
    Column(
        modifier = Modifier
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = description,
            textAlign = TextAlign.Justify,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// It will show the date of the day.
// It will change automatically every day through the API.
@Composable
private fun ApodDateOfTheDay(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Date: $date"
        )
        Spacer(modifier = Modifier.width(5.dp))
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null // Only illustrative
        )
    }
}

// Copyright info
@Composable
private fun ApodPhotoAndCopyright(copyright: String) {
    val copyContains = copyright.replace("\n", "")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Copyright,
            contentDescription = null // Only illustrative
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "Copyright: $copyContains"
        )
    }
}

// "Expandable" button.
// One of the main components to be able to expand the card component and display information.
@Composable
private fun ApodExpandableButton(
    onClick: () -> Unit,
    expanded: Boolean,
    @StringRes info: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = info)
        )
        Spacer(modifier = Modifier.width(5.dp))
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null
            )
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class ThemePreviews

@ThemePreviews
@Composable
private fun ApodPreview() {
    NASAComposeTheme {
        Surface {
            Column(
                content = {
//                    ApodScreenContent(
//                        image = R.drawable.image_preview,
//                        title = R.string.app_name,
//                        description = R.string.lorem_ipsum,
//                        date = "2023/11/27"
//                    )
//                    ApodLoading()
//                    ApodConnectionError(errorText = R.string.connection_problems)
                }
            )
        }
    }
}