#!/bin/bash

echo "Fixing remaining Kotlin syntax issues..."

# Fix files with missing closing braces (these appear to need an extra closing brace at the end)
FILES_WITH_MISSING_BRACE=(
    "app/src/main/java/com/seafile/seadroid2/BootAutostart.kt"
    "app/src/main/java/com/seafile/seadroid2/SeafException.kt"
    "app/src/main/java/com/seafile/seadroid2/account/Authenticator.kt"
    "app/src/main/java/com/seafile/seadroid2/account/AuthenticatorService.kt"
    "app/src/main/java/com/seafile/seadroid2/framework/datastore/DataManager.kt"
    "app/src/main/java/com/seafile/seadroid2/framework/glide/GlideImageModelLoaderFactory.kt"
    "app/src/main/java/com/seafile/seadroid2/framework/http/UnsafeOkHttpClient.kt"
    "app/src/main/java/com/seafile/seadroid2/framework/http/converter/GsonRequestBodyConverter.kt"
    "app/src/main/java/com/seafile/seadroid2/framework/livephoto/MotionPhotoExtractor.kt"
)

for file in "${FILES_WITH_MISSING_BRACE[@]}"; do
    if [ -f "$file" ]; then
        echo "}" >> "$file"
        echo "Fixed missing closing brace in $file"
    fi
done

# Fix FileTransferDAO.kt - appears to have syntax issues
FILE="app/src/main/java/com/seafile/seadroid2/framework/db/dao/FileTransferDAO.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Remove lines that appear to be broken comments
    sed -i '' '/^    \/\*.*\*\/$/d' "$FILE"
    sed -i '' 's/\/\*\s*//g' "$FILE"
    sed -i '' 's/\s*\*\///g' "$FILE"
fi

# Fix GlideImage.kt - has "Expecting a top level declaration" error
FILE="app/src/main/java/com/seafile/seadroid2/framework/glide/GlideImage.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Remove any stray characters at the beginning of the file
    sed -i '' '1s/^[^p]*//' "$FILE"
fi

# Fix SupportResponseConverter.kt
FILE="app/src/main/java/com/seafile/seadroid2/framework/http/converter/SupportResponseConverter.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Check if there's a stray character or incomplete comment
    sed -i '' 's/^}/}\n/' "$FILE"
fi

# Fix RepoPermissionWrapper.kt
FILE="app/src/main/java/com/seafile/seadroid2/framework/model/repo/RepoPermissionWrapper.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Remove stray characters at the beginning
    sed -i '' '1s/^[^p]*//' "$FILE"
fi

# Fix SeafileMonitor.kt
FILE="app/src/main/java/com/seafile/seadroid2/framework/monitor/SeafileMonitor.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Remove stray characters at the beginning
    sed -i '' '1s/^[^p]*//' "$FILE"
fi

# Fix GeneralNotificationHelper.kt - has multiple "Expecting member declaration" errors
FILE="app/src/main/java/com/seafile/seadroid2/framework/notification/GeneralNotificationHelper.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Remove broken comment fragments
    sed -i '' 's/\/\*\s*//g' "$FILE"
    sed -i '' 's/\s*\*\///g' "$FILE"
    sed -i '' '/^[[:space:]]*\*[[:space:]]*$/d' "$FILE"
fi

echo "Fixes applied!"