# Update Launcher Icon to WT Logo

## Method 1: Using Android Studio (EASIEST - 2 minutes)

### Step-by-Step:

1. **Open Android Studio**

2. **Right-click on the `app` folder** in the Project view (left panel)

3. **Select: New → Image Asset**
   - If you don't see it, try: **File → New → Image Asset**

4. **In the Asset Studio window:**
   - Icon Type: **Launcher Icons (Adaptive and Legacy)**
   - Name: `ic_launcher` (leave as default)

5. **In the Foreground Layer tab:**
   - Source Asset: **Image**
   - Click the **folder icon** next to "Path:"
   - Navigate to: `G:\My Drive\downloadfolder 2022\`
   - Select: `Gemini_Generated_Image_naglhunaglhunagl.png`
   - Click **OK**

6. **Adjust the image:**
   - Resize: **80-90%** (so the logo isn't too big)
   - Trim: **No** (keep the padding)

7. **Background Layer:**
   - Color: `#F5EFE6` (cream color matching the logo)
   - OR select **Image** and use the same logo file

8. **Click Next → Finish**

9. **Clean and Rebuild:**
   - Build → Clean Project
   - Build → Rebuild Project

10. **Uninstall old app from your phone/emulator**
    - This is IMPORTANT! Old icon is cached
    - Long press the app → Uninstall

11. **Run the app again**
    - The new WT logo will appear on the home screen!

---

## Method 2: Manual (If Image Asset doesn't work)

I'll create a script to generate the icons for you...
