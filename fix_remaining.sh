#!/bin/bash

APP_DIR="/Users/HONGBGU/Documents/seadroid/app/src/main/java/com/seafile/seadroid2"

echo "Fixing remaining errors..."

# Fix DataStoreManager calls
find "$APP_DIR" -name "SupportAccountManager.kt" -exec sed -i '' \
    -e 's/DataStoreManager\.getCommonSharePreference()/DataStoreManager.getCommonSharePreference()!!/g' \
    {} \;

# Fix HttpIO calls
find "$APP_DIR" -name "*.kt" -exec sed -i '' \
    -e 's/HttpIO\.getInstanceByAccount(/HttpIO.getInstanceByAccount!(/g' \
    -e 's/HttpIO\.getCurrentInstance()/HttpIO.getCurrentInstance()!!/g' \
    {} \;

# Fix Account type usage
find "$APP_DIR/account" -name "*.kt" -exec sed -i '' \
    -e 's/Account(getSignature()/android.accounts.Account(getSignature()/g' \
    {} \;

echo "Fixes applied!"
