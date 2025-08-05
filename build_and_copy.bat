@echo off
setlocal

echo ==========================================
echo Starting Release APK Build and Copy
echo ==========================================

REM Clean previous builds
echo 1. Cleaning old build files...
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo ERROR: Clean failed! Please check error messages.
    pause
    exit /b 1
)

REM Build Release APK
echo 2. Building Release APK...
call gradlew.bat assembleRelease
if %errorlevel% neq 0 (
    echo ERROR: Build failed! Please check error messages.
    pause
    exit /b 1
)

echo SUCCESS: Build completed!

REM Set APK path
set "APK_PATH=app\build\outputs\apk\release\app-release.apk"

REM Check if APK exists
if not exist "%APK_PATH%" (
    echo ERROR: Cannot find APK file: %APK_PATH%
    pause
    exit /b 1
)

REM Display APK information
echo.
echo 3. APK Information:
echo File path: %APK_PATH%
for %%A in ("%APK_PATH%") do (
    set "FILE_SIZE=%%~zA"
    set /a "FILE_SIZE_MB=%%~zA/1024/1024"
)
call echo File size: %%FILE_SIZE_MB%% MB

REM Create output directory
set "OUTPUT_DIR=release"
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM Generate timestamp for filename
for /f "tokens=1-3 delims=/ " %%a in ("%date%") do (
    set "YEAR=%%a"
    set "MONTH=%%b"
    set "DAY=%%c"
)
for /f "tokens=1-2 delims=: " %%a in ("%time%") do (
    set "HOUR=%%a"
    set "MINUTE=%%b"
)
set "HOUR=%HOUR: =0%"

REM Create filename
set "OUTPUT_FILENAME=LingXi_v1.0_%YEAR%%MONTH%%DAY%_%HOUR%%MINUTE%.apk"
set "OUTPUT_PATH=%OUTPUT_DIR%\%OUTPUT_FILENAME%"

REM Copy APK to output directory
echo.
echo 4. Copying APK to output directory...
echo Source: %APK_PATH%
echo Target: %OUTPUT_PATH%
copy "%APK_PATH%" "%OUTPUT_PATH%"
if %errorlevel% equ 0 (
    echo SUCCESS: APK copied to: %OUTPUT_PATH%
) else (
    echo ERROR: Copy failed! Error code: %errorlevel%
    pause
    exit /b 1
)

REM Create README file
echo.
echo 5. Creating README file...
(
    echo Release APK File List:
    echo ========================
    echo.
    echo Filename: %OUTPUT_FILENAME%
    call echo Size: %%FILE_SIZE_MB%% MB
    echo Build time: %date% %time%
    echo Version: 1.0
    echo.
    echo Package name: com.fxzs.lingxiagent
    echo Signature: Release signature ^(yidiong.jks^)
    echo.
    echo Installation:
    echo adb install %OUTPUT_FILENAME%
    echo.
    echo Notes:
    echo - APK is signed with release certificate
    echo - Ready for distribution
    echo - Compatible with Android 7.0+ ^(API 24+^)
) > "%OUTPUT_DIR%\README.txt"

echo.
echo ==========================================
echo Release APK Build Completed Successfully!
echo ==========================================
echo.
echo Output files:
echo   APK: %OUTPUT_PATH%
echo   Info: %OUTPUT_DIR%\README.txt
echo.
echo You can:
echo   1. Upload %OUTPUT_FILENAME% to app store
echo   2. Share with testers for testing
echo   3. Use adb install "%OUTPUT_PATH%" to install on device
echo.

pause
