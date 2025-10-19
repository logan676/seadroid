#!/bin/bash

echo "Final comprehensive Kotlin fixes..."

# Remove extra closing braces (these files have one too many)
FILES_EXTRA_BRACE=(
    "app/src/main/java/com/seafile/seadroid2/framework/model/repo/RepoPermissionWrapper.kt"
    "app/src/main/java/com/seafile/seadroid2/framework/monitor/SeafileMonitor.kt"
    "app/src/main/java/com/seafile/seadroid2/framework/util/Utils.kt"
    "app/src/main/java/com/seafile/seadroid2/ui/camera_upload/config_fragment/ConfigWelcomeFragment.kt"
    "app/src/main/java/com/seafile/seadroid2/ui/camera_upload/config_fragment/HowToUploadFragment.kt"
    "app/src/main/java/com/seafile/seadroid2/ui/camera_upload/config_fragment/ReadyToScanFragment.kt"
    "app/src/main/java/com/seafile/seadroid2/ui/camera_upload/config_fragment/WhatToUploadFragment.kt"
)

for file in "${FILES_EXTRA_BRACE[@]}"; do
    if [ -f "$file" ]; then
        # Remove the last closing brace if present
        sed -i '' '$ s/^}$//' "$file"
        echo "Removed extra closing brace from $file"
    fi
done

# Add missing closing braces
FILES_MISSING_BRACE=(
    "app/src/main/java/com/seafile/seadroid2/ui/adapter/ViewPager2Adapter.kt"
    "app/src/main/java/com/seafile/seadroid2/ui/camera_upload/CameraUploadDBHelper.kt"
    "app/src/main/java/com/seafile/seadroid2/ui/camera_upload/GalleryBucketUtils.kt"
)

for file in "${FILES_MISSING_BRACE[@]}"; do
    if [ -f "$file" ]; then
        echo "}" >> "$file"
        echo "Added closing brace to $file"
    fi
done

# Fix Settings.kt - remove broken lines
FILE="app/src/main/java/com/seafile/seadroid2/preferences/Settings.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Look for and remove lines with broken comment syntax around line 56-57
    sed -i '' '56,57d' "$FILE" 2>/dev/null || true
fi

# Fix BaseActivity.kt
FILE="app/src/main/java/com/seafile/seadroid2/ui/base/BaseActivity.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Fix line 40 which has an issue
    sed -i '' '40s/import/AnnotationTarget.FIELD/' "$FILE" 2>/dev/null || true
fi

# Fix DataMigrationActivity.kt
FILE="app/src/main/java/com/seafile/seadroid2/ui/data_migrate/DataMigrationActivity.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Remove broken characters at line 818
    sed -i '' '818s/\*\///g' "$FILE" 2>/dev/null || true
fi

echo "Final fixes applied!"