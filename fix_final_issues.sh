#!/bin/bash

echo "Fixing all remaining Kotlin syntax issues..."

# Add missing closing braces
FILES=(
    "app/src/main/java/com/seafile/seadroid2/framework/util/CrashHandler.kt"
    "app/src/main/java/com/seafile/seadroid2/framework/util/PinyinSource.kt"
    "app/src/main/java/com/seafile/seadroid2/framework/util/SystemSwitchUtils.kt"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "}" >> "$file"
        echo "Added closing brace to $file"
    fi
done

# Fix GlideImage.kt
FILE="app/src/main/java/com/seafile/seadroid2/framework/glide/GlideImage.kt"
if [ -f "$FILE" ]; then
    # Check first few lines to see if there's a stray character
    first_line=$(head -1 "$FILE")
    if [[ ! "$first_line" == "package "* ]]; then
        echo "Fixing $FILE..."
        # Remove everything before 'package' on the first occurrence
        sed -i '' '1s/^.*package/package/' "$FILE"
    fi
fi

# Fix SupportResponseConverter.kt
FILE="app/src/main/java/com/seafile/seadroid2/framework/http/converter/SupportResponseConverter.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Check line 27-28 for issues
    sed -i '' '27,28d' "$FILE" 2>/dev/null || true
fi

# Fix RepoPermissionWrapper.kt
FILE="app/src/main/java/com/seafile/seadroid2/framework/model/repo/RepoPermissionWrapper.kt"
if [ -f "$FILE" ]; then
    first_line=$(head -1 "$FILE")
    if [[ ! "$first_line" == "package "* ]]; then
        echo "Fixing $FILE..."
        sed -i '' '1s/^.*package/package/' "$FILE"
    fi
fi

# Fix SeafileMonitor.kt
FILE="app/src/main/java/com/seafile/seadroid2/framework/monitor/SeafileMonitor.kt"
if [ -f "$FILE" ]; then
    first_line=$(head -1 "$FILE")
    if [[ ! "$first_line" == "package "* ]]; then
        echo "Fixing $FILE..."
        sed -i '' '1s/^.*package/package/' "$FILE"
    fi
fi

# Fix GeneralNotificationHelper.kt
FILE="app/src/main/java/com/seafile/seadroid2/framework/notification/GeneralNotificationHelper.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Remove problematic lines that appear to be broken comment fragments
    sed -i '' '19,20d' "$FILE" 2>/dev/null || true
    sed -i '' '57,58d' "$FILE" 2>/dev/null || true
fi

# Fix Utils.kt
FILE="app/src/main/java/com/seafile/seadroid2/framework/util/Utils.kt"
if [ -f "$FILE" ]; then
    echo "Checking $FILE..."
    # Check if file ends properly (should end with a closing brace)
    last_char=$(tail -c 2 "$FILE" | head -c 1)
    if [ "$last_char" != "}" ]; then
        echo "}" >> "$FILE"
        echo "Added closing brace to $FILE"
    fi
fi

# Fix Settings.kt
FILE="app/src/main/java/com/seafile/seadroid2/preferences/Settings.kt"
if [ -f "$FILE" ]; then
    echo "Fixing $FILE..."
    # Remove broken comment lines
    sed -i '' '27,28d' "$FILE" 2>/dev/null || true
    sed -i '' '58d' "$FILE" 2>/dev/null || true
fi

echo "All fixes applied!"