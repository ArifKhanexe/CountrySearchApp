package com.example.sampleapp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.sampleapp.data.Country
import com.example.sampleapp.data.CountryRepository
import com.example.sampleapp.data.CountryViewModelFactory
import com.example.sampleapp.ui.theme.SampleAppTheme
import com.example.sampleapp.viewmodel.CountryViewModel
import com.example.sampleapp.viewmodel.MainActivityViewModel


@OptIn(ExperimentalMaterial3Api::class)
class MainScreenCompose : ComponentActivity() {


    private val countryRepository = CountryRepository() // Initialize your repository here
    private val countryViewModel: CountryViewModel by viewModels {
        CountryViewModelFactory(countryRepository)
    }
    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            setContent {
                SampleAppTheme {
                    MainScreenContent(
                        countryViewModel = countryViewModel,
                        mainActivityViewModel = mainActivityViewModel
                    )
                }
            }

        }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun MainScreenContent(
        countryViewModel: CountryViewModel,
        mainActivityViewModel: MainActivityViewModel
    ) {
        LaunchedEffect(Unit) {
            countryViewModel.fetchCountries()
        }

        val countryList by countryViewModel.countries.observeAsState(emptyList())
        val isLoading by countryViewModel.isLoading.observeAsState(false)
        val query = remember { mutableStateOf(TextFieldValue("")) }
        val showBottomSheet = remember { mutableStateOf(false) }

        val filteredList by remember(query.value.text) {
            derivedStateOf {
                if (query.value.text.isEmpty()) {
                    countryList
                } else {
                    countryList.filter { country ->
                        country.name?.common?.contains(query.value.text, ignoreCase = true) == true
                    }
                }
            }
        }

        val images by mainActivityViewModel.images.observeAsState(emptyList())
        val currentPosition by mainActivityViewModel.currentPosition.observeAsState(0)

        val listState = rememberLazyListState()

        val visibleItems = remember(listState.layoutInfo.visibleItemsInfo) {
            listState.layoutInfo.visibleItemsInfo.map { info ->
                filteredList.getOrNull(info.index)
            }.filterNotNull()
        }

        Scaffold(
            topBar = { /* Your TopBar content */ },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    showBottomSheet.value = true
                }) {
                    Icon(
                        painterResource(id = R.drawable.menu_icon),
                        contentDescription = "Statistics"
                    )
                }
            },
            content = { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    state = listState
                ) {
                    item {
                        if (images.isNotEmpty()) {
                            HorizontalCarousel(
                                images = images.map { painterResource(id = it) },
                                currentPosition = currentPosition,
                                onPositionChange = { position ->
                                    mainActivityViewModel.updatePosition(position)
                                }
                            )
                        }
                    }

                    // Sticky Header Item
                    stickyHeader {
                        SearchBar(
                            query = query,
                            onQueryChanged = { newText ->
                                query.value = newText
                            },
                            hint = "Search countries...",
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.search_icon), // Replace with your icon resource
                                    contentDescription = "Search",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        )
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        items(filteredList) { country ->
                            CountryItem(country = country)
                        }
                    }
                }
            }
        )

        if (showBottomSheet.value) {
            ShowStatisticsBottomSheet(
                visibleItems = visibleItems,
                showBottomSheet = showBottomSheet
            )
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShowStatisticsBottomSheet(
        visibleItems: List<Country>,
        showBottomSheet: MutableState<Boolean>
    ) {
        val bottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        if (showBottomSheet.value) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet.value = false },
                sheetState = bottomSheetState
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Visible Item Count: ${visibleItems.size}")
                    visibleItems.forEach { country ->
                        val countryName = country.name?.common ?: ""
                        val capitalName = country.capital?.firstOrNull() ?: ""
                        val combinedName = "$countryName $capitalName"
                        val charFrequency = calculateCharacterFrequency(combinedName)
                        val totalCharCount = combinedName.replace(" ", "").length

                        Text(
                            text = "Country: $countryName, Capital: $capitalName\n" +
                                    "Character Frequencies: $charFrequency\n" +
                                    "Total Character Count: $totalCharCount\n\n"
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun HorizontalCarousel(
        images: List<Painter>,
        currentPosition: Int,
        onPositionChange: (Int) -> Unit
    ) {
        val listState = rememberLazyListState()

        LaunchedEffect(currentPosition) {
            listState.animateScrollToItem(currentPosition)
        }

        val firstVisibleItemIndex by remember {
            derivedStateOf { listState.firstVisibleItemIndex }
        }
        val firstVisibleItemOffset by remember {
            derivedStateOf { listState.firstVisibleItemScrollOffset }
        }

        val visibleItemIndex = if (firstVisibleItemOffset > 0) {
            firstVisibleItemIndex + 1
        } else {
            firstVisibleItemIndex
        }

        LaunchedEffect(visibleItemIndex) {
            if (visibleItemIndex != currentPosition) {
                onPositionChange(visibleItemIndex)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(images) { image ->
                    Box(
                        modifier = Modifier
                            .width(300.dp)
                            .height(200.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val index = images.indexOf(image)
                                onPositionChange(index)
                            }
                    ) {
                        Image(
                            painter = image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DotIndicators(
                dotCount = images.size,
                currentIndex = currentPosition
            )
        }
    }
    @Composable
    fun DotIndicators(
        dotCount: Int,
        currentIndex: Int
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(dotCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentIndex) Color.Black else Color.Gray
                        )
                )
            }
        }
    }

    @Composable
    fun SearchBar(
        query: MutableState<TextFieldValue>,
        onQueryChanged: (TextFieldValue) -> Unit,
        hint: String,
        leadingIcon: @Composable (() -> Unit)? = null
    ) {
        TextField(
            value = query.value,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .background(Color.White, shape = MaterialTheme.shapes.small),
            placeholder = {
                Text(text = hint, style = MaterialTheme.typography.bodyMedium)
            },
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (query.value.text.isNotEmpty()) {
                    IconButton(onClick = { query.value = TextFieldValue("") }) {
                        Icon(
                            imageVector = Icons.Default.Close, // Cross icon
                            contentDescription = "Clear text"
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = MaterialTheme.shapes.small
        )
    }

    @Composable
    fun CountryList(countryList: List<Country>) {
        LazyColumn {
            items(countryList) { country ->
                CountryItem(country = country)
            }
        }
    }

    @Composable
    fun CountryItem(country: Country) {
        val imagePainter = rememberImagePainter(
            data = country.flags?.png,
            builder = {
                crossfade(true)
            }
        )

        // Define your background color, radius, and padding
        val backgroundColor = Color(0xFFDDF0EE)
        val cornerRadius = 10.dp
        val paddingBetweenItems = 5.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = paddingBetweenItems) // Padding between items
                .clip(RoundedCornerShape(cornerRadius)) // Corner radius
                .background(backgroundColor) // Background color
                .padding(8.dp) // Padding inside the item
        ) {
            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = country.name?.common ?: "Unknown Country",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = country.capital?.firstOrNull() ?: "No Capital",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    fun calculateCharacterFrequency(item: String): String {
        val charCount = mutableMapOf<Char, Int>()

        item.forEach { char ->
            if (char.isLetter()) {
                charCount[char.lowercaseChar()] =
                    charCount.getOrDefault(char.lowercaseChar(), 0) + 1
            }
        }

        return charCount.entries
            .sortedByDescending { it.value }
            .take(3)
            .joinToString(separator = ", ") { "${it.key} = ${it.value}" }
    }




}

