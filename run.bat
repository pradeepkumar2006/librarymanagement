@echo off
if not exist sqlite-jdbc.jar (
    echo [INFO] Downloading SQLite JDBC driver...
    curl -L -o sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar
)
echo [INFO] Compiling all Java files...
javac *.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %errorlevel%
)
echo [INFO] Running LibrarySystem...
java -cp ".;sqlite-jdbc.jar" LibrarySystem
pause
