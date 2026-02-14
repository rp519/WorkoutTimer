# Design System Overhaul - Test Checklist

## ✅ Compilation Fixes Applied
- Fixed duplicate `style` parameter errors in ActiveTimerScreen
- All Text composables now properly use TextStyle
- All imports verified and correct

---

## 🧪 Manual Testing Checklist

### 1. **Splash Screen** 🚀
**What to test:**
- [ ] App launches with premium blue gradient background (not teal)
- [ ] Logo fades in smoothly (0→1 opacity over 800ms)
- [ ] Logo scales in (0.8→1.0 over 800ms)
- [ ] Logo is centered vertically
- [ ] Transitions to onboarding/home after 2 seconds

**Expected colors:**
- Background: Blue gradient (#1E88E5 → #42A5F5, diagonal)
- Text/Icon: White

---

### 2. **Home Screen (PlanListScreen)** 🏠
**What to test:**
- [ ] Quick Timer button at top has blue gradient background
- [ ] Quick Timer button text is white
- [ ] Quick Timer button has elevation/shadow
- [ ] Clicking Quick Timer navigates to Quick Timer screen
- [ ] Workout cards display correctly below

**Expected styling:**
- Quick Timer card: Blue gradient background, white text
- Icons: White on gradient

---

### 3. **Quick Timer Screen** ⏱️
**What to test:**
- [ ] App bar has blue gradient background
- [ ] App bar text is white
- [ ] Back button icon is white
- [ ] Main background is light blue (#F5F9FF)
- [ ] Input fields work correctly
- [ ] Start button works
- [ ] Timer countdown displays properly
- [ ] Pause/Resume buttons work
- [ ] Reset button works

**Expected colors:**
- Top bar: Blue gradient
- Background: Light blue (#F5F9FF)
- Timer ring: Blue (#42A5F5)

---

### 4. **Create/Edit Workout Screen** ✏️
**What to test:**
- [ ] App bar has blue gradient background
- [ ] App bar title text is white
- [ ] Back button is white
- [ ] Content background is light blue (#F5F9FF)
- [ ] All three workout mode cards display:
  - [ ] Simple Timer
  - [ ] Exercise Library
  - [ ] Custom Mix
- [ ] Mode selection works (cards highlight when selected)
- [ ] Input fields work correctly
- [ ] Save button works

**Expected colors:**
- Top bar: Blue gradient with white text
- Background: Light blue
- Cards: White with proper elevation

---

### 5. **Active Timer Screen (CRITICAL)** 🎯

#### **PREP Phase (Get Ready)**
**What to test:**
- [ ] Exercise image displays (if available)
- [ ] **"GET READY!" card is positioned 30% from top (200dp)**
- [ ] Card has rounded corners (16dp)
- [ ] Card has blur background (blue overlay #CC1565C0)
- [ ] Card has elevation/shadow
- [ ] Text "GET READY!" is:
  - [ ] 36sp font size
  - [ ] Bold
  - [ ] White color
  - [ ] Letter spacing 0.1
  - [ ] Has text shadow
- [ ] "First: [Exercise]" text displays below
- [ ] Gradient overlay makes text readable over image

**Critical positioning check:**
- [ ] Card does NOT cover the entire image
- [ ] Card is clearly positioned in upper portion (not center)
- [ ] Text is readable over image

#### **WORK Phase**
**What to test:**
- [ ] Exercise image displays
- [ ] Gradient overlay (dark at bottom) is visible
- [ ] Exercise name at bottom is:
  - [ ] 32sp font size
  - [ ] Bold
  - [ ] White color
  - [ ] Has text shadow
  - [ ] **Has 24dp margin from bottom edge**
- [ ] "Exercise X/Y" text below exercise name:
  - [ ] 16sp font size
  - [ ] Has 12dp margin from exercise name
  - [ ] Has text shadow
  - [ ] White color (95% opacity)

**Critical positioning check:**
- [ ] Exercise name is NOT touching bottom of screen
- [ ] Text has clear 24dp breathing room from edges
- [ ] Text is readable over dark gradient overlay

#### **REST Phase (Next Up)**
**What to test:**
- [ ] Next exercise image displays
- [ ] Gradient overlay (dark at bottom) is visible
- [ ] "NEXT UP:" label is:
  - [ ] 16sp font size
  - [ ] Has text shadow
  - [ ] White (80% opacity)
- [ ] Next exercise name is:
  - [ ] 32sp font size
  - [ ] Bold
  - [ ] White color
  - [ ] Has text shadow
  - [ ] **Has 24dp margin from bottom edge**

**Critical positioning check:**
- [ ] Text does NOT hit bottom edge
- [ ] Proper spacing between "NEXT UP:" and exercise name
- [ ] Text readable over gradient overlay

#### **Timer Controls (All Phases)**
**What to test:**
- [ ] Circular timer ring displays correctly
- [ ] Pause/Resume button works
- [ ] Stop/Abandon button works
- [ ] Timer countdown updates every second
- [ ] Phase colors change correctly:
  - Countdown: Amber
  - Prep: Blue
  - Work: Red
  - Rest: Green
  - Finished: Gold

---

### 6. **SIMPLE Mode Workout** 📊
**What to test:**
- [ ] No images display (just text)
- [ ] Plan name shows at top
- [ ] Phase text displays (GET READY, WORK, REST)
- [ ] Round and exercise counters display
- [ ] Timer works correctly
- [ ] Background is ivory white (#FFFFF0)

---

### 7. **LIBRARY/CUSTOM Mode Workout** 🖼️
**What to test:**
- [ ] Exercise images load correctly
- [ ] Gradient overlays ensure text readability
- [ ] All text shadows render properly
- [ ] Text positioning doesn't overlap with images
- [ ] Next exercise preview works in REST phase

---

## 🐛 Common Issues to Check

### Text Shadows
- [ ] All text over images has visible shadows
- [ ] Shadows are dark enough to provide contrast
- [ ] No text is unreadable due to image behind it

### Spacing
- [ ] No text touches screen edges (min 24dp margin)
- [ ] Proper spacing between UI elements
- [ ] Card padding is consistent (24dp horizontal, 16dp vertical)

### Gradients
- [ ] All gradients render smoothly (no banding)
- [ ] Gradient directions are correct
- [ ] Colors match design spec:
  - Primary: #1E88E5 → #42A5F5
  - Background: #F5F9FF

### Navigation
- [ ] Back buttons work on all screens
- [ ] Navigation flow is smooth
- [ ] No crashes when navigating between screens

---

## 📱 Device Testing Recommendations

Test on:
1. **Small phone** (5" screen) - Verify text doesn't overflow
2. **Large phone** (6.5" screen) - Verify proper scaling
3. **Tablet** - Verify layout adapts
4. **Dark mode** - Verify color scheme switches properly

---

## 🚨 Critical Items (Must Fix Before Release)

1. ✅ "GET READY!" card positioned 200dp from top (NOT centered)
2. ✅ All bottom text has 24dp margin from screen edge
3. ✅ Text shadows applied to all text over images
4. ✅ Gradient overlays make text readable
5. ✅ No compilation errors

---

## 📊 Performance Checks

- [ ] Splash screen animation is smooth (no stuttering)
- [ ] Timer updates smoothly (no lag)
- [ ] Screen transitions are fluid
- [ ] No memory leaks during workout
- [ ] Images load quickly

---

## 🎨 Design QA

Compare against design specifications:
- [ ] Blue gradient matches (#1E88E5 → #42A5F5)
- [ ] Light blue background matches (#F5F9FF)
- [ ] Amber accent matches (#FFA726)
- [ ] Text sizes match spec (36sp, 32sp, 16sp)
- [ ] Card corner radius is 16dp
- [ ] Elevations are 4-8dp

---

## ✅ Final Sign-off

Before marking complete:
- [ ] All screens tested on at least 2 devices
- [ ] No visual glitches
- [ ] No crashes
- [ ] UX improvements verified (text positioning)
- [ ] Color scheme consistent across app
- [ ] Text readability excellent over all images

---

**Status:** Ready for testing
**Priority:** Test ActiveTimerScreen positioning first (most critical UX fix)
