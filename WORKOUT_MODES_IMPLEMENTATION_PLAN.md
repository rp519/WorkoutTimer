# Workout Modes Implementation Plan
## WorkoutTimer Android App - Feature Enhancement

---

## 📋 TABLE OF CONTENTS
1. [Current Architecture Analysis](#current-architecture-analysis)
2. [Database Schema Changes](#database-schema-changes)
3. [Implementation Phases](#implementation-phases)
4. [Detailed Technical Specifications](#detailed-technical-specifications)
5. [UI/UX Mockups](#uiux-specifications)
6. [Migration Strategy](#migration-strategy)
7. [Testing Plan](#testing-plan)

---

## 🏗️ CURRENT ARCHITECTURE ANALYSIS

### Existing Components:
```
Database Layer:
├── WorkoutPlan (id, name, rounds, exerciseCount, workSeconds, restSeconds)
├── Exercise (id, name, category, imagePath)
├── WorkoutExercise (workoutPlanId, exerciseId, orderIndex)
└── WorkoutHistory (id, planId, planName, completedAt, totalDurationSeconds, roundsCompleted)

ViewModels:
├── PlanEditViewModel - Workout creation/editing
├── ActiveTimerViewModel - Workout execution
└── PlanListViewModel - Workout list management

Screens:
├── PlanListScreen - Home screen with workout list
├── PlanEditScreen - Create/edit workouts (currently requires exercise selection)
├── ActiveTimerScreen - Workout timer with 50/50 split (image top, timer bottom)
└── WorkoutCompleteScreen - Post-workout summary

Timer Phases:
└── enum class TimerPhase { COUNTDOWN, WORK, REST, FINISHED }
```

### Current Limitations:
- ❌ Requires exercise selection (validation: `selectedExercises.isNotEmpty()`)
- ❌ No workout mode differentiation
- ❌ No prep time before workouts
- ❌ No next exercise preview during rest
- ❌ No quick timer functionality
- ❌ Fixed 3-second countdown (not configurable prep time)

---

## 🗄️ DATABASE SCHEMA CHANGES

### Phase 1: Add Workout Modes Support

#### Migration v6 → v7

**Update WorkoutPlan Table:**
```kotlin
@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rounds: Int,
    val exerciseCount: Int,
    val workSeconds: Int,
    val restSeconds: Int,

    // NEW FIELDS
    val workoutMode: WorkoutMode = WorkoutMode.SIMPLE,  // SIMPLE, LIBRARY, CUSTOM
    val prepTimeSeconds: Int = 30,                      // Configurable prep time
    val hasExercises: Boolean = false                   // Quick flag for UI logic
)

enum class WorkoutMode {
    SIMPLE,    // Mode A: Just rounds/exercises count
    LIBRARY,   // Mode B: Exercises from library
    CUSTOM     // Mode C: Mix of library + custom exercises
}
```

**Migration SQL:**
```sql
ALTER TABLE workout_plans ADD COLUMN workoutMode TEXT NOT NULL DEFAULT 'SIMPLE';
ALTER TABLE workout_plans ADD COLUMN prepTimeSeconds INTEGER NOT NULL DEFAULT 30;
ALTER TABLE workout_plans ADD COLUMN hasExercises INTEGER NOT NULL DEFAULT 0;

-- Migrate existing workouts to LIBRARY mode if they have associated exercises
UPDATE workout_plans
SET workoutMode = 'LIBRARY', hasExercises = 1
WHERE id IN (SELECT DISTINCT workoutPlanId FROM workout_exercises);
```

#### New Table: CustomExercise (for Mode C)

```kotlin
@Entity(tableName = "custom_exercises")
data class CustomExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutPlanId: Long,              // Belongs to specific workout
    val name: String,                     // User-defined name
    val durationSeconds: Int,             // Custom duration for this exercise
    val orderIndex: Int,                  // Position in workout
    val imagePath: String? = null,        // Optional custom image
    val isFromLibrary: Boolean = false,   // True if added from library, false if custom
    val libraryExerciseId: Long? = null   // Reference if from library
)
```

**Create Table SQL:**
```sql
CREATE TABLE IF NOT EXISTS custom_exercises (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    workoutPlanId INTEGER NOT NULL,
    name TEXT NOT NULL,
    durationSeconds INTEGER NOT NULL,
    orderIndex INTEGER NOT NULL,
    imagePath TEXT,
    isFromLibrary INTEGER NOT NULL DEFAULT 0,
    libraryExerciseId INTEGER,
    FOREIGN KEY(workoutPlanId) REFERENCES workout_plans(id) ON DELETE CASCADE
);

CREATE INDEX index_custom_exercises_workoutPlanId ON custom_exercises(workoutPlanId);
```

#### New DAO: CustomExerciseDao

```kotlin
@Dao
interface CustomExerciseDao {

    @Query("SELECT * FROM custom_exercises WHERE workoutPlanId = :workoutPlanId ORDER BY orderIndex")
    suspend fun getCustomExercisesForWorkout(workoutPlanId: Long): List<CustomExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<CustomExercise>)

    @Transaction
    suspend fun replaceCustomExercisesForWorkout(workoutPlanId: Long, exercises: List<CustomExercise>) {
        deleteCustomExercisesForWorkout(workoutPlanId)
        if (exercises.isNotEmpty()) {
            val indexed = exercises.mapIndexed { index, ex ->
                ex.copy(orderIndex = index, workoutPlanId = workoutPlanId)
            }
            insertAll(indexed)
        }
    }

    @Query("DELETE FROM custom_exercises WHERE workoutPlanId = :workoutPlanId")
    suspend fun deleteCustomExercisesForWorkout(workoutPlanId: Long)
}
```

---

## 📝 IMPLEMENTATION PHASES

### **PHASE 1: Database & Core Infrastructure** (Day 1-2)
**Priority: CRITICAL** - Foundation for all features

**Tasks:**
1. ✅ Create database migration v6 → v7
2. ✅ Add `WorkoutMode` enum
3. ✅ Update `WorkoutPlan` entity with new fields
4. ✅ Create `CustomExercise` entity and DAO
5. ✅ Update `AppDatabase` to version 7
6. ✅ Test migration with existing data

**Files to Modify:**
- `app/data/model/WorkoutPlan.kt` - Add new fields
- `app/data/model/CustomExercise.kt` - NEW FILE
- `app/data/dao/CustomExerciseDao.kt` - NEW FILE
- `app/data/AppDatabase.kt` - Add migration, new DAO
- Test migration script

**Acceptance Criteria:**
- Existing workouts migrate successfully
- Workouts with exercises become LIBRARY mode
- Workouts without exercises become SIMPLE mode
- No data loss during migration

---

### **PHASE 2: Prep Time Feature** (Day 2-3)
**Priority: HIGH** - Universal feature for all modes

**Tasks:**
1. ✅ Add `PREP` to `TimerPhase` enum
2. ✅ Add prep time fields to `ActiveTimerViewModel`
3. ✅ Implement 30-second prep phase before each round
4. ✅ Update `ActiveTimerScreen` UI for prep display
5. ✅ Show first exercise image during prep (if applicable)
6. ✅ Add prep complete sound/notification
7. ✅ Make prep time configurable in workout creation

**Implementation Details:**

**TimerPhase Update:**
```kotlin
enum class TimerPhase {
    COUNTDOWN,  // Initial 3-second countdown
    PREP,       // NEW: 30-second preparation time
    WORK,
    REST,
    FINISHED
}
```

**ActiveTimerViewModel Changes:**
```kotlin
class ActiveTimerViewModel(application: Application) : AndroidViewModel(application) {

    // Add prep time tracking
    private var prepTimeSeconds = 30

    fun load(planId: Long) {
        viewModelScope.launch {
            val plan = planDao.getById(planId) ?: return@launch
            prepTimeSeconds = plan.prepTimeSeconds
            // ... existing code
        }
    }

    private fun startWorkout() {
        timerJob = viewModelScope.launch {
            // Initial 3-second countdown
            phase = TimerPhase.COUNTDOWN
            countdown(3)

            for (round in 1..totalRounds) {
                currentRound = round

                // PREP PHASE - 30 seconds before each round
                phase = TimerPhase.PREP
                totalSecondsInPhase = prepTimeSeconds

                // Show first exercise during prep if available
                if (orderedExercises.isNotEmpty()) {
                    val firstExercise = orderedExercises[0]
                    currentExerciseName = firstExercise.name
                    currentExerciseImagePath = firstExercise.imagePath
                }

                soundManager.speakGetReady()
                countdown(prepTimeSeconds)
                soundManager.playGoSound()

                // Start workout rounds
                for (exercise in 1..totalExercises) {
                    // ... existing exercise loop
                }
            }
        }
    }
}
```

**ActiveTimerScreen UI Update:**
```kotlin
// In the top 50% image section
when (viewModel.phase) {
    TimerPhase.PREP -> {
        // Show first exercise or plan name
        ExerciseImage(
            imagePath = viewModel.currentExerciseImagePath,
            contentDescription = "Get Ready",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f))) {
            Column {
                Text(
                    text = "GET READY!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                if (viewModel.currentExerciseName.isNotBlank()) {
                    Text(
                        text = "First: ${viewModel.currentExerciseName}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(0.9f)
                    )
                }
            }
        }
    }
    // ... other phases
}
```

**Files to Modify:**
- `screen/activetimer/ActiveTimerViewModel.kt` - Add prep phase logic
- `screen/activetimer/ActiveTimerScreen.kt` - Add prep UI
- `screen/planedit/PlanEditScreen.kt` - Add prep time input field

**Acceptance Criteria:**
- 30-second prep shows before each round starts
- First exercise image displays during prep (if workout has exercises)
- Sound plays when prep ends
- Timer transitions smoothly to first exercise
- Works for all workout modes

---

### **PHASE 3: Next Exercise Preview** (Day 3-4)
**Priority: MEDIUM** - Enhances user experience

**Tasks:**
1. ✅ Add `nextExerciseName` and `nextExerciseImagePath` to ViewModel
2. ✅ Calculate next exercise during workout loop
3. ✅ Update REST phase UI to show next exercise preview
4. ✅ Handle last exercise case ("Final Exercise!")
5. ✅ Preload next image during rest

**Implementation:**

**ActiveTimerViewModel:**
```kotlin
var nextExerciseName by mutableStateOf("")
    private set
var nextExerciseImagePath by mutableStateOf<String?>(null)
    private set

private fun startWorkout() {
    timerJob = viewModelScope.launch {
        // ... countdown and prep

        for (round in 1..totalRounds) {
            for (exercise in 1..totalExercises) {
                currentExercise = exercise

                // Set current exercise
                if (orderedExercises.isNotEmpty()) {
                    val currentEx = orderedExercises[exercise - 1]
                    currentExerciseName = currentEx.name
                    currentExerciseImagePath = currentEx.imagePath
                }

                // Calculate NEXT exercise
                if (exercise < totalExercises && orderedExercises.isNotEmpty()) {
                    val nextEx = orderedExercises[exercise]
                    nextExerciseName = nextEx.name
                    nextExerciseImagePath = nextEx.imagePath
                } else if (exercise == totalExercises) {
                    nextExerciseName = "Final Exercise"
                    nextExerciseImagePath = null
                }

                // WORK phase
                phase = TimerPhase.WORK
                countdown(workSeconds)

                // REST phase with next exercise preview
                if (exercise < totalExercises) {
                    phase = TimerPhase.REST
                    countdown(restSeconds)
                }
            }
        }
    }
}
```

**ActiveTimerScreen REST Phase UI:**
```kotlin
// In top 50% when phase == TimerPhase.REST
Box(modifier = Modifier.fillMaxSize()) {
    // Show NEXT exercise image
    ExerciseImage(
        imagePath = viewModel.nextExerciseImagePath,
        contentDescription = viewModel.nextExerciseName,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )

    // Overlay with "Next Up" text
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.4f))
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = "NEXT UP:",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(0.7f)
            )
            Text(
                text = viewModel.nextExerciseName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
```

**Files to Modify:**
- `screen/activetimer/ActiveTimerViewModel.kt` - Add next exercise tracking
- `screen/activetimer/ActiveTimerScreen.kt` - Update REST phase UI

**Acceptance Criteria:**
- Next exercise image shows during rest periods
- "Next Up: [Exercise Name]" displays clearly
- Last exercise shows "Final Exercise!" message
- Only applies to workouts with selected exercises (LIBRARY/CUSTOM modes)

---

### **PHASE 4: Simple Rounds Mode (Mode A)** (Day 4-5)
**Priority: HIGH** - Restores core flexibility

**Tasks:**
1. ✅ Add workout mode selection UI to PlanEditScreen
2. ✅ Conditional rendering based on selected mode
3. ✅ Update validation to allow empty exercise list for SIMPLE mode
4. ✅ Update ActiveTimerScreen to handle SIMPLE mode (no images)
5. ✅ Test simple timer functionality

**PlanEditScreen Mode Selection:**
```kotlin
@Composable
fun PlanEditScreen(...) {
    var selectedMode by remember { mutableStateOf(WorkoutMode.SIMPLE) }

    Column {
        // Mode Selection Cards
        Text("Workout Type", style = MaterialTheme.typography.titleLarge)
        Spacer(8.dp)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeSelectionCard(
                title = "Simple Timer",
                description = "Rounds & exercises - no library",
                icon = Icons.Default.Timer,
                selected = selectedMode == WorkoutMode.SIMPLE,
                onClick = { selectedMode = WorkoutMode.SIMPLE }
            )

            ModeSelectionCard(
                title = "Exercise Library",
                description = "Select from exercise images",
                icon = Icons.Default.FitnessCenter,
                selected = selectedMode == WorkoutMode.LIBRARY,
                onClick = { selectedMode = WorkoutMode.LIBRARY }
            )

            ModeSelectionCard(
                title = "Custom Mix",
                description = "Combine library + custom",
                icon = Icons.Default.Build,
                selected = selectedMode == WorkoutMode.CUSTOM,
                onClick = { selectedMode = WorkoutMode.CUSTOM }
            )
        }

        Spacer(16.dp)

        // Conditional UI based on mode
        when (selectedMode) {
            WorkoutMode.SIMPLE -> SimpleMode UI()
            WorkoutMode.LIBRARY -> LibraryModeUI()
            WorkoutMode.CUSTOM -> CustomModeUI()
        }
    }
}

@Composable
fun SimpleModeUI(viewModel: PlanEditViewModel) {
    Column {
        OutlinedTextField(
            value = viewModel.planName,
            onValueChange = viewModel::updatePlanName,
            label = { Text("Workout Name") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumericTextField(
                value = viewModel.rounds,
                onValueChange = viewModel::updateRounds,
                label = "Rounds",
                modifier = Modifier.weight(1f)
            )
            NumericTextField(
                value = viewModel.exerciseCount,
                onValueChange = viewModel::updateExerciseCount,
                label = "Exercises",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumericTextField(
                value = viewModel.workSeconds,
                onValueChange = viewModel::updateWorkSeconds,
                label = "Work (sec)",
                modifier = Modifier.weight(1f)
            )
            NumericTextField(
                value = viewModel.restSeconds,
                onValueChange = viewModel::updateRestSeconds,
                label = "Rest (sec)",
                modifier = Modifier.weight(1f)
            )
        }

        NumericTextField(
            value = viewModel.prepTimeSeconds,
            onValueChange = viewModel::updatePrepTime,
            label = "Prep Time (sec)",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

**PlanEditViewModel Validation Update:**
```kotlin
fun save() {
    // Validation depends on mode
    when (workoutMode) {
        WorkoutMode.SIMPLE -> {
            if (planName.isBlank()) return
            // exerciseCount and rounds from input fields
        }
        WorkoutMode.LIBRARY, WorkoutMode.CUSTOM -> {
            if (planName.isBlank() || selectedExercises.isEmpty()) return
        }
    }

    viewModelScope.launch {
        val plan = WorkoutPlan(
            id = if (isEditing) editingPlanId else 0,
            name = planName.trim(),
            rounds = rounds,
            exerciseCount = when (workoutMode) {
                WorkoutMode.SIMPLE -> exerciseCount  // From input field
                else -> selectedExercises.size       // From selection
            },
            workSeconds = workSeconds,
            restSeconds = restSeconds,
            workoutMode = workoutMode,
            prepTimeSeconds = prepTimeSeconds,
            hasExercises = selectedExercises.isNotEmpty()
        )

        val planId = if (isEditing) {
            planDao.update(plan)
            editingPlanId
        } else {
            planDao.insert(plan)
        }

        // Save exercises only for LIBRARY/CUSTOM modes
        if (workoutMode != WorkoutMode.SIMPLE) {
            workoutExerciseDao.replaceExercisesForWorkout(planId, selectedExercises)
        }

        isSaved = true
    }
}
```

**ActiveTimerScreen Mode Handling:**
```kotlin
// Top 50% - conditional rendering
Box(modifier = Modifier.weight(1f)) {
    when {
        // If SIMPLE mode or no exercises
        viewModel.workoutMode == WorkoutMode.SIMPLE || !viewModel.hasExercises -> {
            // Show minimal info - no images
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = viewModel.planName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(8.dp)
                Text(
                    text = when (viewModel.phase) {
                        TimerPhase.PREP -> "GET READY!"
                        TimerPhase.WORK -> "WORK"
                        TimerPhase.REST -> "REST"
                        else -> ""
                    },
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }

        // LIBRARY/CUSTOM mode with exercises
        else -> {
            // Existing image display logic
            ExerciseImage(...)
        }
    }
}
```

**Files to Modify:**
- `screen/planedit/PlanEditScreen.kt` - Add mode selection UI
- `screen/planedit/PlanEditViewModel.kt` - Handle different modes
- `screen/activetimer/ActiveTimerScreen.kt` - Conditional rendering
- `screen/activetimer/ActiveTimerViewModel.kt` - Load mode from DB

**Acceptance Criteria:**
- User can select "Simple Timer" mode
- No exercise selection required
- Shows "Round X/Y - Exercise A/B" display
- Clean timer-only interface
- All existing functionality preserved

---

### **PHASE 5: Custom Mixed Mode (Mode C)** (Day 5-6)
**Priority: MEDIUM** - Advanced flexibility

**Tasks:**
1. ✅ Create custom exercise builder UI
2. ✅ Allow mixing library + custom exercises
3. ✅ Support reordering exercises
4. ✅ Custom duration per exercise
5. ✅ Optional image attachment

**Custom Exercise Builder UI:**
```kotlin
@Composable
fun CustomModeUI(viewModel: PlanEditViewModel) {
    Column {
        // List of added exercises (library + custom)
        LazyColumn {
            items(viewModel.customExerciseList) { exercise ->
                CustomExerciseCard(
                    exercise = exercise,
                    onRemove = { viewModel.removeCustomExercise(it) },
                    onReorder = { from, to -> viewModel.reorderExercises(from, to) },
                    onEditDuration = { ex, duration -> viewModel.updateExerciseDuration(ex, duration) }
                )
            }
        }

        // Add exercise options
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { showLibrarySheet = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FitnessCenter, null)
                Spacer(4.dp)
                Text("From Library")
            }

            OutlinedButton(
                onClick = { showCustomDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(4.dp)
                Text("Custom Entry")
            }
        }
    }
}

@Composable
fun AddCustomExerciseDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, duration: Int, image: Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(30) }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Exercise") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") }
                )
                NumericTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = "Duration (seconds)"
                )
                Button(onClick = { /* Image picker */ }) {
                    Text("Attach Image (Optional)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name, duration, selectedImage) }) {
                Text("Add")
            }
        }
    )
}
```

**Files to Create:**
- `screen/planedit/CustomExerciseBuilder.kt` - NEW FILE
- `data/model/CustomExercise.kt` - Already created in Phase 1

**Files to Modify:**
- `screen/planedit/PlanEditViewModel.kt` - Add custom exercise management
- `screen/planedit/PlanEditScreen.kt` - Integrate custom mode UI

---

### **PHASE 6: Quick Timer Mode** (Day 6-7)
**Priority: LOW** - Nice-to-have standalone feature

**Tasks:**
1. ✅ Create QuickTimerScreen & ViewModel
2. ✅ Add navigation route
3. ✅ Add "Quick Timer" button on home screen
4. ✅ Simple duration input → start countdown
5. ✅ Full-screen clean display

**Quick Timer Implementation:**

**New Screen: QuickTimerScreen.kt**
```kotlin
@Composable
fun QuickTimerScreen(
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    var duration by remember { mutableIntStateOf(60) }
    var isRunning by remember { mutableStateOf(false) }

    if (!isRunning) {
        // Setup screen
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Quick Timer", style = MaterialTheme.typography.displaySmall)
            Spacer(32.dp)

            NumericTextField(
                value = duration,
                onValueChange = { duration = it },
                label = "Duration (seconds)"
            )

            Spacer(32.dp)

            Button(
                onClick = { isRunning = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("START")
            }
        }
    } else {
        // Timer display
        QuickTimerDisplay(
            durationSeconds = duration,
            onComplete = onComplete,
            onBack = onBack
        )
    }
}
```

**Navigation Update:**
```kotlin
// Add to Screen.kt
data object QuickTimer : Screen("quick_timer")

// Add to AppNavGraph.kt
composable(Screen.QuickTimer.route) {
    QuickTimerScreen(
        onBack = { navController.popBackStack() },
        onComplete = { navController.popBackStack() }
    )
}
```

**PlanListScreen Update:**
```kotlin
// Add Quick Timer FAB
Row(verticalAlignment = Alignment.Bottom) {
    FloatingActionButton(
        onClick = { navController.navigate(Screen.QuickTimer.route) }
    ) {
        Icon(Icons.Default.Timer, "Quick Timer")
    }
    Spacer(8.dp)
    FloatingActionButton(
        onClick = onCreatePlan
    ) {
        Icon(Icons.Default.Add, "Create Workout")
    }
}
```

**Files to Create:**
- `screen/quicktimer/QuickTimerScreen.kt` - NEW FILE
- `screen/quicktimer/QuickTimerViewModel.kt` - NEW FILE

**Files to Modify:**
- `navigation/Screen.kt` - Add QuickTimer route
- `navigation/AppNavGraph.kt` - Add composable
- `screen/planlist/PlanListScreen.kt` - Add Quick Timer button

---

## 🎨 UI/UX SPECIFICATIONS

### Mode Selection Cards Design
```
┌─────────────────────────────────────────────┐
│   SELECT WORKOUT TYPE                       │
├─────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │  SIMPLE  │  │ LIBRARY  │  │  CUSTOM  │ │
│  │    ⏱️     │  │    💪     │  │    🛠️     │ │
│  │  Timer   │  │ Exercises │  │   Mix    │ │
│  │  Mode    │  │ w/Images  │  │  Both    │ │
│  └──────────┘  └──────────┘  └──────────┘ │
└─────────────────────────────────────────────┘
```

### Prep Time Display
```
┌─────────────────────────────────────────────┐
│                                             │
│          [FIRST EXERCISE IMAGE]             │
│              (if available)                 │
│                                             │
│  ╔═══════════════════════════════════════╗ │
│  ║      GET READY!                       ║ │
│  ║      First: Bicycle Crunch            ║ │
│  ╚═══════════════════════════════════════╝ │
├─────────────────────────────────────────────┤
│              ⏱️ 00:28                       │
│          Preparation Time                   │
│                                             │
│         [Pause] [Abandon]                   │
└─────────────────────────────────────────────┘
```

### Next Exercise Preview (REST Phase)
```
┌─────────────────────────────────────────────┐
│                                             │
│          [NEXT EXERCISE IMAGE]              │
│                                             │
│  ╔═══════════════════════════════════════╗ │
│  ║      NEXT UP:                         ║ │
│  ║      Push Ups                         ║ │
│  ╚═══════════════════════════════════════╝ │
├─────────────────────────────────────────────┤
│              ⏱️ 00:08                       │
│               Rest                          │
│          Round 1/3 • Exercise 2/5           │
└─────────────────────────────────────────────┘
```

### Simple Mode Display (No Images)
```
┌─────────────────────────────────────────────┐
│                                             │
│                                             │
│          MY HIIT WORKOUT                    │
│                                             │
│              WORK                           │
│                                             │
│                                             │
├─────────────────────────────────────────────┤
│              ⏱️ 00:24                       │
│                                             │
│          Round 2/3                          │
│          Exercise 3/5                       │
│                                             │
│         [Pause] [Abandon]                   │
└─────────────────────────────────────────────┘
```

---

## 🔄 MIGRATION STRATEGY

### Backward Compatibility Plan:

**Existing Workouts (v6):**
- Have `exerciseCount` and possibly `workout_exercises` associations
- No `workoutMode` field yet

**Migration Logic:**
1. Add new columns with defaults:
   - `workoutMode = 'SIMPLE'` (default)
   - `prepTimeSeconds = 30`
   - `hasExercises = 0`

2. Detect and upgrade:
```sql
-- If workout has associated exercises → LIBRARY mode
UPDATE workout_plans
SET workoutMode = 'LIBRARY', hasExercises = 1
WHERE id IN (SELECT DISTINCT workoutPlanId FROM workout_exercises);

-- All others remain SIMPLE mode
```

3. ActiveTimerViewModel handles both:
```kotlin
fun load(planId: Long) {
    viewModelScope.launch {
        val plan = planDao.getById(planId) ?: return@launch

        // Determine mode
        val mode = plan.workoutMode
        val hasExercises = plan.hasExercises

        if (hasExercises) {
            // Load exercises from junction table
            orderedExercises = workoutExerciseDao.getExercisesForWorkout(planId)
        } else {
            // Use exerciseCount for simple mode
            totalExercises = plan.exerciseCount
        }

        // Continue with timer setup
    }
}
```

---

## ✅ TESTING PLAN

### Phase-by-Phase Testing:

**Phase 1: Database Migration**
- [ ] Install v6 app with sample workouts
- [ ] Upgrade to v7
- [ ] Verify all existing workouts load correctly
- [ ] Verify mode assignment is correct
- [ ] Test creating new workouts in each mode

**Phase 2: Prep Time**
- [ ] Create workout in each mode
- [ ] Verify 30-second prep shows before first round
- [ ] Verify first exercise image shows (if applicable)
- [ ] Test sound plays at prep end
- [ ] Test pause during prep time

**Phase 3: Next Exercise Preview**
- [ ] Create LIBRARY mode workout with 5 exercises
- [ ] During REST, verify next exercise shows
- [ ] Verify last exercise shows "Final Exercise"
- [ ] Test with SIMPLE mode (should not show)

**Phase 4: Simple Mode**
- [ ] Create simple timer workout (no exercises)
- [ ] Verify no exercise selection required
- [ ] Verify timer display is clean
- [ ] Verify round/exercise counting works
- [ ] Compare with original app v1.0 behavior

**Phase 5: Custom Mode**
- [ ] Add library exercise + custom exercise
- [ ] Reorder exercises
- [ ] Set different durations per exercise
- [ ] Attach custom image
- [ ] Run workout and verify all exercises execute

**Phase 6: Quick Timer**
- [ ] Access Quick Timer from home screen
- [ ] Set 60 seconds
- [ ] Verify countdown runs correctly
- [ ] Test pause/resume
- [ ] Verify completion behavior

### Edge Cases to Test:
- [ ] Workout with 1 round, 1 exercise
- [ ] Workout with 0 seconds rest time
- [ ] Very long prep time (300 seconds)
- [ ] Migration from v5 → v6 → v7
- [ ] Low memory scenarios
- [ ] Screen rotation during timer
- [ ] App backgrounding during workout

---

## 📁 FILE STRUCTURE OVERVIEW

```
app/src/main/java/com/stopwatch/app/
│
├── data/
│   ├── model/
│   │   ├── WorkoutPlan.kt (MODIFY - add fields)
│   │   ├── Exercise.kt
│   │   ├── WorkoutExercise.kt
│   │   ├── CustomExercise.kt (NEW)
│   │   └── WorkoutMode.kt (NEW - enum)
│   │
│   ├── dao/
│   │   ├── WorkoutPlanDao.kt
│   │   ├── ExerciseDao.kt
│   │   ├── WorkoutExerciseDao.kt
│   │   └── CustomExerciseDao.kt (NEW)
│   │
│   └── AppDatabase.kt (MODIFY - v6→v7 migration)
│
├── screen/
│   ├── planedit/
│   │   ├── PlanEditScreen.kt (MODIFY - mode selection)
│   │   ├── PlanEditViewModel.kt (MODIFY - handle modes)
│   │   ├── ExerciseSelectionSheet.kt
│   │   └── CustomExerciseBuilder.kt (NEW)
│   │
│   ├── activetimer/
│   │   ├── ActiveTimerScreen.kt (MODIFY - prep & preview)
│   │   └── ActiveTimerViewModel.kt (MODIFY - prep & next exercise)
│   │
│   ├── quicktimer/ (NEW FOLDER)
│   │   ├── QuickTimerScreen.kt (NEW)
│   │   └── QuickTimerViewModel.kt (NEW)
│   │
│   └── planlist/
│       ├── PlanListScreen.kt (MODIFY - add Quick Timer button)
│       └── PlanListViewModel.kt
│
└── navigation/
    ├── Screen.kt (MODIFY - add QuickTimer route)
    └── AppNavGraph.kt (MODIFY - add QuickTimer composable)
```

---

## 📊 IMPLEMENTATION TIMELINE

**Week 1:**
- Mon-Tue: Phase 1 (Database)
- Wed: Phase 2 (Prep Time)
- Thu: Phase 3 (Next Preview)
- Fri: Testing & Bug Fixes

**Week 2:**
- Mon-Tue: Phase 4 (Simple Mode)
- Wed: Phase 5 (Custom Mode)
- Thu: Phase 6 (Quick Timer)
- Fri: Integration Testing

**Week 3:**
- Mon-Tue: Polish & UX refinements
- Wed: Performance testing
- Thu: Final QA
- Fri: Release preparation

---

## 🎯 SUCCESS METRICS

✅ **Feature Completeness:**
- All 4 workout modes functional
- Prep time working universally
- Next exercise preview working
- Quick timer standalone feature

✅ **Backward Compatibility:**
- 100% of existing workouts migrate successfully
- No data loss
- Existing functionality preserved

✅ **User Experience:**
- Intuitive mode selection
- Smooth transitions
- Clear visual feedback
- No performance degradation

✅ **Code Quality:**
- Clean architecture maintained
- Proper separation of concerns
- Comprehensive test coverage
- Documented code

---

## 📝 NOTES & CONSIDERATIONS

1. **Image Loading Performance:**
   - Preload next exercise image during current exercise
   - Consider image caching strategy for custom images
   - Monitor memory usage with many custom images

2. **Sound Management:**
   - Ensure prep time end sound is distinct
   - Consider different sounds for different phases
   - Respect user's sound preferences

3. **User Preferences:**
   - Consider saving "last used mode" for quick access
   - Allow hiding prep time (optional)
   - Configurable default prep time in settings

4. **Future Enhancements:**
   - Per-exercise rest times (beyond uniform timing)
   - Exercise substitutions mid-workout
   - Workout templates/presets
   - Workout sharing/export

---

## 🚀 READY TO IMPLEMENT

This plan provides a complete roadmap for implementing all requested features while maintaining code quality and backward compatibility. Each phase builds upon the previous one, allowing for incremental development and testing.

**Recommended Start:** Begin with Phase 1 (Database) as it's the foundation for all other features.

Would you like me to proceed with implementation starting from Phase 1?
