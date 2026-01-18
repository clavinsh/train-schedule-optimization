@echo off
@REM Maven Wrapper script for Windows
@REM This script downloads Maven wrapper jar if needed and runs Maven

setlocal EnableDelayedExpansion

set "MAVEN_PROJECTBASEDIR=%~dp0"
@REM Remove trailing backslash
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

@REM Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set "JAVA_EXE=java.exe"
"%JAVA_EXE%" -version >NUL 2>&1
if %ERRORLEVEL% EQU 0 goto checkWrapper

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto error

:findJavaFromJavaHome
set "JAVA_HOME=%JAVA_HOME:"=%"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

if exist "%JAVA_EXE%" goto checkWrapper

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto error

:checkWrapper
if exist "%WRAPPER_JAR%" goto runWrapper

echo Downloading Maven Wrapper from %WRAPPER_URL%...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"

if exist "%WRAPPER_JAR%" goto runWrapper

echo Failed to download maven-wrapper.jar
goto error

:runWrapper
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"
"%JAVA_EXE%" %MAVEN_OPTS% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
endlocal & exit /b %ERROR_CODE%
