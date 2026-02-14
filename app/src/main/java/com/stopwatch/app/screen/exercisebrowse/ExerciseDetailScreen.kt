package com.stopwatch.app.screen.exercisebrowse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stopwatch.app.R
import com.stopwatch.app.data.model.Exercise
import com.stopwatch.app.data.model.ExerciseCategories

/**
 * Screen displaying detailed information about a specific exercise
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    onBack: () -> Unit,
    viewModel: ExerciseBrowseViewModel = viewModel()
) {
    var exercise by remember { mutableStateOf<Exercise?>(null) }

    // Load exercise details
    LaunchedEffect(exerciseId) {
        exercise = viewModel.getExerciseById(exerciseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercise Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        exercise?.let { ex ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Large exercise image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    if (ex.imagePath.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/${ex.imagePath}")
                                .crossfade(true)
                                .build(),
                            contentDescription = ex.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = painterResource(id = R.drawable.ic_launcher_foreground)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ex.name.take(2),
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Breadcrumb
                    Text(
                        text = "${ExerciseCategories.getDisplayName(ex.category)} > ${ex.subcategory}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Exercise name
                    Text(
                        text = ex.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Key metrics in cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Intensity",
                            value = ex.intensity,
                            modifier = Modifier.weight(1f)
                        )
                        if (ex.caloriesPerMin > 0) {
                            MetricCard(
                                title = "Cal/Min",
                                value = ex.caloriesPerMin.toInt().toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Duration
                    if (ex.durationMin > 0 || ex.durationMax > 0) {
                        DetailSection(
                            title = "Duration",
                            content = "${ex.durationMin / 60}-${ex.durationMax / 60} minutes"
                        )
                    }

                    // Target muscles
                    if (ex.targetMuscles.isNotEmpty()) {
                        DetailSection(
                            title = "Target Muscles",
                            content = ex.targetMuscles
                        )
                    }

                    // Equipment
                    if (ex.equipment.isNotEmpty()) {
                        DetailSection(
                            title = "Equipment",
                            content = ex.equipment
                        )
                    }

                    // Description
                    if (ex.description.isNotEmpty()) {
                        DetailSection(
                            title = "Description",
                            content = ex.description
                        )
                    }

                    // Action button (placeholder for future functionality)
                    Button(
                        onClick = { /* TODO: Add to workout */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add to Workout")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } ?: run {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

/**
 * Metric card for key exercise stats
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Section displaying exercise detail information
 */
@Composable
private fun DetailSection(
    title: String,
    content: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
