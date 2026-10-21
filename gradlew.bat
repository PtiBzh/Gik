@rem Set local directory if not set
@if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle

@rem Set JAVA_HOME if not set
@if "%JAVA_HOME%"=="" set JAVA_HOME=%JAVA_HOME_17_X64%

@rem Execute Gradle
"%GRADLE_USER_HOME%\caches\gradle-8.4\bin\gradle" %*
