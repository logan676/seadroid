#!/bin/bash

APP_DIR="/Users/HONGBGU/Documents/seadroid/app/src/main/java/com/seafile/seadroid2"

echo "Applying final fixes..."

# Fix remaining nullability issues - add !! where safe receiver is expected
find "$APP_DIR" -name "*.kt" -exec sed -i '' \
    -e 's/SupportAccountManager\.getInstance()\.getCurrentAccount()/SupportAccountManager.getInstance()!!.getCurrentAccount()/g' \
    -e 's/SupportAccountManager\.getInstance()\.getSeafileAccount/SupportAccountManager.getInstance()!!.getSeafileAccount/g' \
    {} \;

# Fix interface instantiations - change to object expressions
find "$APP_DIR/widget" -name "SimpleMarkdownParser.kt" -exec sed -i '' \
    -e 's/MatchHandler(/object : MatchHandler {/g' \
    -e 's/ImageLoaderCallback(/object : ImageLoaderCallback {/g' \
    {} \;

# Fix PreloadWebView access
find "$APP_DIR/view/webview" -name "PreloadWebView.kt" -exec sed -i '' \
    -e 's/Holder\.INSTANCE/Holder.getInstance()/g' \
    {} \;

echo "Final fixes applied!"
