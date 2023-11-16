package com.duanstar.locationfaker.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ContentAlpha.medium
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duanstar.locationfaker.R
import com.duanstar.locationfaker.fake_location.FakeLocation
import com.duanstar.locationfaker.ui.theme.Dimensions.marginVertical
import com.duanstar.locationfaker.ui.theme.Dimensions.spacing
import com.duanstar.locationfaker.ui.widgets.CenteredRow
import com.duanstar.locationfaker.ui.widgets.SingleLineText
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.launch

@Composable
fun SearchLayout(
    position: LatLng,
    viewModel: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val autocompletePredictions by viewModel.autocompleteResults.collectAsStateWithLifecycle()

    SearchLayout(
        position = position,
        favorites = favorites,
        autocompletePredictions = autocompletePredictions,
        removeFavorite = viewModel::removeFavorite,
        onSearchQueryChanged = viewModel::setQuery,
        setFakeLocation = viewModel::setFakeLocation,
        onPredictionClick = viewModel::setFakeLocation,
        onBack = onBack
    )
}

@Composable
fun SearchLayout(
    position: LatLng,
    favorites: List<FakeLocation>,
    autocompletePredictions: List<AutocompletePrediction>,
    removeFavorite: (FakeLocation) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    setFakeLocation: (FakeLocation) -> Unit,
    onPredictionClick: suspend (AutocompletePrediction) -> Boolean,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchTextField(
                        query = "",
                        onQueryChanged = onSearchQueryChanged,
                        hint = stringResource(R.string.search_hint)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                }
            )
        }
    ) { p ->
        val coroutineScope = rememberCoroutineScope()
        LazyColumn(modifier = Modifier.padding(p)) {
            items(favorites) { favorite ->
                FavoriteRow(
                    favorite = favorite,
                    onClick = {
                        setFakeLocation(favorite)
                        onBack()
                    },
                    onRemoveClick = {
                        removeFavorite(favorite)
                    }
                )
            }

            items(autocompletePredictions) { prediction ->
                AutocompletePredictionRow(
                    prediction = prediction,
                    onClick = {
                        coroutineScope.launch {
                            if (onPredictionClick(prediction)) {
                                onBack()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    favorite: FakeLocation,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    CenteredRow(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = marginVertical)
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            modifier = Modifier.minimumInteractiveComponentSize(),
            tint = MaterialTheme.colors.primaryVariant
        )
        Spacer(Modifier.size(spacing))
        Column(modifier = Modifier.weight(1f)) {
            SingleLineText(text = favorite.title)
            SingleLineText(
                text = favorite.subtitle.orEmpty(),
                style = MaterialTheme.typography.body2,
                modifier = Modifier.alpha(medium)
            )
        }
        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.remove_favorite),
                modifier = Modifier.alpha(medium)
            )
        }
    }
}

@Composable
private fun AutocompletePredictionRow(
    prediction: AutocompletePrediction,
    onClick: () -> Unit
) {
    CenteredRow(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = marginVertical)
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            modifier = Modifier.minimumInteractiveComponentSize(),
            tint = MaterialTheme.colors.primaryVariant
        )
        Spacer(Modifier.size(spacing))
        Column {
            SingleLineText(text = prediction.getPrimaryText(null).toString())
            SingleLineText(
                text = prediction.getSecondaryText(null).toString(),
                style = MaterialTheme.typography.body2,
                modifier = Modifier.alpha(medium)
            )
        }
    }
}
