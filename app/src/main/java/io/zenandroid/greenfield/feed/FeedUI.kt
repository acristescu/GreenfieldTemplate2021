package io.zenandroid.greenfield.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.coil.CoilImage
import io.zenandroid.greenfield.data.model.Image
import io.zenandroid.greenfield.R
import io.zenandroid.greenfield.data.model.MediaLink
import java.text.SimpleDateFormat
import java.util.*

private val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.UK)

@Composable
fun FeedUI(state: FeedState, listener: (FeedAction) -> Unit) {
    Scaffold(topBar = {
        TopBar(
                subtitle = state.tags,
                searchBoxVisible = state.searchBoxVisible,
                searchText = state.searchText,
                listener = listener
        )
    }) {
        Box {
            state.images?.let {
                LazyColumn {
                    items(state.images) {
                        FeedItem(it, listener)
                    }
                }
            }
            if (state.loading) {
                Card(
                        shape = CircleShape,
                        modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 50.dp),
                        elevation = 4.dp
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                }
            }
            state.errorMessage?.let {
                Toast(it, listener)
            }
        }
    }
    if(state.sortDialogVisible) {
        SortDialog(state.criterion, listener)
    }
}

@Composable
fun Toast(text: String, listener: (FeedAction) -> Unit) {
    Card(modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(1f)
    ) {
        Box {
            Text(
                    text = text,
                    modifier = Modifier
                            .padding(24.dp)
            )
            IconButton(
                    onClick = { listener(FeedAction.DismissError) },
                    modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Filled.Close)
            }
        }
    }
}

@Composable
fun SortDialog(currentCriterion: SortCriterion, listener: (FeedAction) -> Unit) {
    AlertDialog(
            onDismissRequest = { listener(FeedAction.DismissFilterDialog) },
            buttons = {},
            text = {
                Column(Modifier.fillMaxWidth(1f)) {
                    Text(
                            text = "Select sorting",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .alpha(1f)
                    )
                    Box {
                        Text(
                                text = "Date Published",
                                color = Color.Black,
                                modifier = Modifier
                                        .clickable(onClick = { listener(FeedAction.SortCriterion(SortCriterion.PUBLISHED)) })
                                        .padding(vertical = 16.dp)
                                        .fillMaxWidth(1f)
                                        .alpha(1f)
                        )
                        if (currentCriterion == SortCriterion.PUBLISHED) {
                            Icon(imageVector = Icons.Filled.Check, Modifier.align(Alignment.CenterEnd))
                        }
                    }
                    Box {
                        Text(
                                text = "Date Taken",
                                color = Color.Black,
                                modifier = Modifier
                                        .clickable(onClick = { listener(FeedAction.SortCriterion(SortCriterion.TAKEN)) })
                                        .padding(vertical = 16.dp)
                                        .fillMaxWidth(1f)
                                        .alpha(1f)
                        )
                        if (currentCriterion == SortCriterion.TAKEN) {
                            Icon(imageVector = Icons.Filled.Check, Modifier.align(Alignment.CenterEnd))
                        }
                    }
                }
            }
    )
}

@Composable
fun FeedItem(item: Image, listener: (FeedAction) -> Unit) {
    Surface(
            shape = RoundedCornerShape(4.dp),
            elevation = 4.dp,
            color = Color(0xFFEAEAEA),
            modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(1f)
    ) {
        Column {
            item.media?.m?.let {
                CoilImage(it, Modifier
                        .height(250.dp)
                        .fillMaxWidth(1f)
                )
            }
            val modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
            Text(item.title ?: "", modifier.padding(top = 4.dp))
            Text(item.tags ?: "", modifier)
            Text(item.author ?: "", modifier)
            Text(
                    text = item.published?.let { String.format("Published: %s", format.format(it)) } ?: "",
                    modifier = modifier,
                    style = MaterialTheme.typography.subtitle2,
                    color = Color(0xAA000000)
            )
            Text(
                    text = item.dateTaken?.let { String.format("Taken: %s", format.format(it)) } ?: "",
                    modifier = modifier,
                    style = MaterialTheme.typography.subtitle2,
                    color = Color(0xAA000000)
            )
            Row {
                TextButton(onClick = { listener(FeedAction.Browse(item)) }) {
                    Text(
                            text = "BROWSE",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                    )
                }
                TextButton(onClick = { listener(FeedAction.Save(item)) }) {
                    Text(
                            text = "SAVE",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                    )
                }
                TextButton(onClick = { listener(FeedAction.Share(item)) }) {
                    Text(
                            text = "SHARE",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(
        subtitle: String?,
        searchBoxVisible: Boolean,
        searchText: String,
        listener: (FeedAction) -> Unit
) {
    TopAppBar(
            title = {
                if (searchBoxVisible) {
                    TextField(
                            value = searchText,
                            placeholder = {
                                Row {
                                    Icon(Icons.Filled.Search)
                                    Spacer(modifier = Modifier.preferredSize(8.dp))
                                    Text(text = "Tags separated by space", modifier = Modifier.padding(start = 8.dp))
                                }
                            },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { listener(FeedAction.SearchCloseButtonPressed) }) {
                                    Icon(Icons.Filled.Close)
                                }
                            },
                            activeColor = MaterialTheme.colors.onPrimary,
                            inactiveColor = MaterialTheme.colors.onPrimary,
                            textStyle = MaterialTheme.typography.h6,
                            modifier = Modifier
                                    .fillMaxWidth(1f)
                                    .padding(bottom = 4.dp),
                            backgroundColor = Color.Transparent,
                            onValueChange = { listener(FeedAction.SearchTextChanged(it)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            onImeActionPerformed = { _, _ -> listener(FeedAction.SearchComplete) }
                    )
                } else {
                    Column {
                        Text("Public images", fontWeight = FontWeight.Bold)
                        subtitle?.let { Text(text = it, style = MaterialTheme.typography.subtitle2) }
                    }
                }
            },
            actions = {
                if (!searchBoxVisible) {
                    IconButton(onClick = { listener(FeedAction.SearchButtonPressed) }) {
                        Icon(Icons.Filled.Search)
                    }
                }
                IconButton(onClick = { listener(FeedAction.ChangeFiltering) }) {
                    val image = loadVectorResource(id = R.drawable.ic_sort_24dp)
                    image.resource.resource?.let {
                        Icon(it)
                    }
                }
            }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewNormal() {
    FeedUI(state = FeedState(
            loading = false,
            images = listOf(
                    Image(title = "Title 1", tags = "cats dogs", author = "Author", media = MediaLink("https://picsum.photos/300/300")),
                    Image(title = "Title 2", tags = "cats dogs", author = "Author", media = MediaLink("https://picsum.photos/300/300")),
                    Image(title = "Title 3", tags = "cats dogs", author = "Author", media = MediaLink("https://picsum.photos/300/300")),
                    Image(title = "Title 4", tags = "cats dogs", author = "Author", media = MediaLink("https://picsum.photos/300/300")),
                    Image(title = "Title 5", tags = "cats dogs", author = "Author", media = MediaLink("https://picsum.photos/300/300"))
            ),
            tags = "cats dogs",
            sortDialogVisible = true,
            errorMessage = "Network Error"
    ), {})
}

@Preview(showBackground = true)
@Composable
private fun PreviewLoading() {
    FeedUI(state = FeedState(
            loading = true,
            searchBoxVisible = true
    ), {})
}
