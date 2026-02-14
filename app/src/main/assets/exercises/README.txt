Exercise Images Directory
========================

This folder should contain WebP exercise images organized by category subdirectories.

Expected structure:
  exercises/
    ├── abs/
    │   ├── bicycle_crunch.webp
    │   ├── plank.webp
    │   └── ...
    ├── chest/
    │   ├── push_up.webp
    │   ├── bench_press.webp
    │   └── ...
    ├── legs/
    │   └── ...
    └── [other categories]/

ACTION REQUIRED:
Copy the category folders from:
  C:\Users\rpk51\Downloads\Workoutimagesv2\workout_assets
to this directory (app/src/main/assets/exercises/)

The ExerciseLibraryInitializer will automatically scan this folder on first launch
and populate the Exercise database with all images found.

Filename Format:
  - Filenames represent exercise names (e.g., "bicycle_crunch.webp" → "Bicycle Crunch")
  - Folder names represent categories (e.g., "abs" → "Abs")
  - Underscores in filenames are converted to spaces
  - First letter of each word is capitalized
