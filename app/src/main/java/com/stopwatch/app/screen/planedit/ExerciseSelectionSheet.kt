package com.stopwatch.app.screen.planedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stopwatch.app.data.model.Exercise
import com.stopwatch.app.data.model.ExerciseCategories
import com.stopwatch.app.ui.components.ExerciseImage

/**
 * Navigation state for the exercise selection sheet
 */
private enum class SheetView {
    CATEGORIES,
    SUBCATEGORIES,
    EXERCISES,
    SEARCH
}

/**
 * Bottom sheet for selecting exercises from the library with hierarchical navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionSheet(
    exercises: List<Exercise>,
    categories: List<String>,
    onExerciseSelected: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentView by remember { mutableStateOf(SheetView.CATEGORIES) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSubcategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Get unique categories from exercises
    val availableCategories = remember(exercises) {
        exercises.map { it.category }.distinct().sorted()
    }

    // Get subcategories for selected category
    val subcategories = remember(selectedCategory) {
        selectedCategory?.let { category ->
            ExerciseCategories.getSubcategories(category)
        } ?: emptyList()
    }

    // Filter exercises based on current selection
    val filteredExercises = remember(exercises, selectedCategory, selectedSubcategory, searchQuery, currentView) {
        when (currentView) {
            SheetView.SEARCH -> {
                if (searchQuery.isBlank()) emptyList()
                else exercises.filter { exercise ->
                    exercise.name.contains(searchQuery, ignoreCase = true) ||
                    exercise.targetMuscles.contains(searchQuery, ignoreCase = true) ||
                    exercise.equipment.contains(searchQuery, ignoreCase = true) ||
                    exercise.category.contains(searchQuery, ignoreCase = true) ||
                    exercise.subcategory.contains(searchQuery, ignoreCase = true)
                }
            }
            SheetView.EXERCISES -> {
                exercises.filter {
                    it.category == selectedCategory && it.subcategory == selectedSubcategory
                }
            }
            else -> emptyList()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(0.90f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentView != SheetView.CATEGORIES) {
                    IconButton(onClick = {
                        when (currentView) {
                            SheetView.SEARCH -> {
                                currentView = SheetView.CATEGORIES
                                searchQuery = ""
                            }
                            SheetView.SUBCATEGORIES -> {
                                currentView = SheetView.CATEGORIES
                                selectedCategory = null
                            }
                            SheetView.EXERCISES -> {
                                currentView = SheetView.SUBCATEGORIES
                                selectedSubcategory = null
                            }
                            else -> {}
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }

                Text(
                    text = when (currentView) {
                        SheetView.CATEGORIES -> "Select Exercise"
                        SheetView.SUBCATEGORIES -> ExerciseCategories.getDisplayName(selectedCategory ?: "")
                        SheetView.EXERCISES -> selectedSubcategory ?: "Exercises"
                        SheetView.SEARCH -> "Search Exercises"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )

                // Search icon (only show on categories view)
                if (currentView == SheetView.CATEGORIES) {
                    IconButton(onClick = {
                        currentView = SheetView.SEARCH
                    }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                }
            }

            // Content based on current view
            when (currentView) {
                SheetView.CATEGORIES -> {
                    CategoriesView(
                        categories = availableCategories,
                        onCategoryClick = { category ->
                            selectedCategory = category
                            currentView = SheetView.SUBCATEGORIES
                        }
                    )
                }

                SheetView.SUBCATEGORIES -> {
                    SubcategoriesView(
                        subcategories = subcategories,
                        exercises = exercises,
                        category = selectedCategory ?: "",
                        onSubcategoryClick = { subcategory ->
                            selectedSubcategory = subcategory
                            currentView = SheetView.EXERCISES
                        }
                    )
                }

                SheetView.EXERCISES -> {
                    ExercisesGridView(
                        exercises = filteredExercises,
                        onExerciseClick = { exercise ->
                            onExerciseSelected(exercise)
                            onDismiss()
                        }
                    )
                }

                SheetView.SEARCH -> {
                    SearchView(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        exercises = filteredExercises,
                        onExerciseClick = { exercise ->
                            onExerciseSelected(exercise)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Categories list view
 */
@Composable
private fun CategoriesView(
    categories: List<String>,
    onCategoryClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoryClick(category) },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ExerciseCategories.getDisplayName(category),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Browse",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Subcategories list view
 */
@Composable
private fun SubcategoriesView(
    subcategories: List<String>,
    exercises: List<Exercise>,
    category: String,
    onSubcategoryClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(subcategories) { subcategory ->
            val exerciseCount = exercises.count {
                it.category == category && it.subcategory == subcategory
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSubcategoryClick(subcategory) },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subcategory,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$exerciseCount exercise${if (exerciseCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Browse",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Exercises grid view
 */
@Composable
private fun ExercisesGridView(
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(exercises, key = { it.id }) { exercise ->
            ExerciseGridCard(
                exercise = exercise,
                onClick = { onExerciseClick(exercise) }
            )
        }
    }
}

/**
 * Search view with search bar and results
 */
@Composable
private fun SearchView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search exercises, muscles, equipment...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, "Clear")
                    }
                }
            }
        )

        // Search results
        if (searchQuery.isBlank()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔍",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Search for exercises",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (exercises.isEmpty()) {
            // No results
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No results for \"$searchQuery\"",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            // Show results
            ExercisesGridView(
                exercises = exercises,
                onExerciseClick = onExerciseClick
            )
        }
    }
}

/**
 * Card displaying an exercise with image and name in grid
 */
@Composable
private fun ExerciseGridCard(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            ExerciseImage(
                imagePath = exercise.imagePath,
                contentDescription = exercise.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.SemiBold
            )

            if (exercise.targetMuscles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.targetMuscles,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
