package com.duanstar.locationfaker.feature.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha.medium
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.IconOpacity
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.duanstar.locationfaker.R
import com.duanstar.locationfaker.ui.theme.AppTheme
import com.duanstar.locationfaker.ui.theme.Dimensions.spacing
import com.duanstar.locationfaker.ui.widgets.CenteredRow

@Composable
fun SearchTextField(
    query: String,
    onQueryChanged: (String) -> Unit,
    backgroundColor: Color = MaterialTheme.colors.primary,
    hint: String = "",
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var shouldRequestFocus by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        if (shouldRequestFocus) {
            shouldRequestFocus = false
            focusRequester.requestFocus()
        }
    }

    var textState by rememberSaveable { mutableStateOf(query) }
    LaunchedEffect(textState) {
        onQueryChanged(textState)
    }

    CenteredRow {
        TextField(
            value = textState,
            onValueChange = {
                textState = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.body1,
            placeholder = {
                Text(
                    text = hint,
                    modifier = Modifier.padding(start = spacing)
                )
            },
            trailingIcon = {
                if (textState.isNotBlank()) {
                    IconButton(onClick = {
                        textState = ""
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear_search_query),
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = backgroundColor,
                cursorColor = contentColorFor(backgroundColor).copy(alpha = medium),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                leadingIconColor = contentColorFor(backgroundColor).copy(alpha = IconOpacity),
                trailingIconColor = contentColorFor(backgroundColor).copy(alpha = IconOpacity),
                placeholderColor = contentColorFor(backgroundColor).copy(alpha = medium)
            )
        )
    }
}

@Preview
@Composable
fun ExpandedSearchBarPreview() {
    AppTheme {
        SearchTextField(
            query = "",
            onQueryChanged = { },
            hint = "Search"
        )
    }
}