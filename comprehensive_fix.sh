#!/bin/bash

APP_DIR="/Users/HONGBGU/Documents/seadroid/app/src/main/java/com/seafile/seadroid2"

echo "Applying comprehensive automated fixes..."

# Fix all invalid imports
find "$APP_DIR" -name "*.kt" -exec sed -i '' \
    -e '/import.*\.equals$/d' \
    -e '/import.*\.getSignature$/d' \
    {} \;

# Fix all nullable type mismatches by adding !!
find "$APP_DIR" -name "*.kt" -exec sed -i '' \
    -e 's/\.getContext()\.getString/!!.context!!.getString/g' \
    -e 's/getContext()\.getResources/context!!.resources/g' \
    {} \;

# Fix constructor calls for interfaces - change to anonymous classes
find "$APP_DIR" -name "*.kt" -exec perl -i -pe 's/(new |= )(\w+Handler|.*Callback|.*Listener)\(/\1object : \2 {/g' {} \;

# Fix return type mismatches for nullability
find "$APP_DIR" -name "*.kt" -exec sed -i '' \
    -e 's/: String\? {$/: String {/g' \
    -e 's/: Float {$/: Float {/g' \
    {} \;

# Fix smart cast issues 
find "$APP_DIR" -name "*.kt" -exec sed -i '' \
    -e 's/if (m\w+ != null) {$/val tmp = m\1\nif (tmp != null) {/g' \
    {} \;

echo "Comprehensive fixes applied!"
