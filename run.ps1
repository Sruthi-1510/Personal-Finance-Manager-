# PowerShell script to run WealthWatch

# Ensure script runs in its own directory so relative paths work
Set-Location $PSScriptRoot

# Check if javac is available in PATH (Preferred for portability)
$javacCmd = Get-Command javac -ErrorAction SilentlyContinue
$javaCmd = Get-Command java -ErrorAction SilentlyContinue

if ($javacCmd -and $javaCmd) {
    $javac = "javac"
    $java = "java"
    Write-Host "Using system Java from PATH."
}
else {
    # Fallback to hardcoded locations if not in PATH
    $jdkBin = "C:\Program Files\Java\jdk-25.0.2\bin"
    if (Test-Path "$jdkBin\javac.exe") {
        $javac = "$jdkBin\javac.exe"
        $java = "$jdkBin\java.exe"
        Write-Host "Java not in PATH. Using fallback JDK at: $jdkBin"
    }
    else {
        Write-Error "Java Compiler (javac) not found in PATH or standard locations! Please install JDK."
        exit 1
    }
}

# Create bin directory if it doesn't exist
if (Test-Path "bin") {
    Remove-Item "bin" -Recurse -Force
}
New-Item -ItemType Directory -Force -Path "bin" | Out-Null

Write-Host "Compiling..."
# Compile dependency classes first, then main class
# Use cmd /c to avoid PowerShell NativeCommandError on stderr warnings
$compileCmd = "& `"$javac`" --module-path `"lib/javafx-sdk-23/lib`" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp `"lib/sqlite-jdbc-3.46.1.3.jar;src`" -d bin src/Transaction.java src/DatabaseManager.java src/WealthWatch.java src/LoginPage.java src/RegistrationPage.java 2>&1"
Invoke-Expression $compileCmd | Write-Host

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful."
    
    # Copy resources (style.css) to bin so ClassLoader can find it
    if (Test-Path "src/style.css") {
        Copy-Item "src/style.css" -Destination "bin" -Force
        Write-Host "Copied style.css to bin."
    }

    Write-Host "Running WealthWatch..."
    # Run
    # Note: Added 'resources' to classpath so it can find the font file if it is in the root resources folder but accessed relatively
    # Use -cp "lib/...;bin" and run class WealthWatch
    & $java --module-path "lib/javafx-sdk-23/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "lib/sqlite-jdbc-3.46.1.3.jar;bin;." WealthWatch
}
else {
    Write-Host "Compilation failed."
}
