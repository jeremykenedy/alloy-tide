"""Check the built package and its runtime permission boundary."""

import os
import re
import subprocess
import sys
from pathlib import Path

sdk = Path(os.environ.get("ANDROID_HOME", Path.home() / "Library/Android/sdk"))
aapt = sdk / "build-tools/36.0.0/aapt"
apk = Path(sys.argv[1])


def dump(*arguments):
    return subprocess.check_output([str(aapt), "dump", *arguments, str(apk)], text=True)


badging = dump("badging")
if not re.search(
    r"package: name='com\.jeremykenedy\.alloytide' versionCode='1' versionName='1\.0\.0'", badging
):
    raise SystemExit("Unexpected package or version")
permissions = dump("permissions")
if "uses-permission:" in permissions:
    raise SystemExit("Alloy Tide must not request runtime or network permissions")
manifest = subprocess.check_output(
    [str(aapt), "dump", "xmltree", str(apk), "AndroidManifest.xml"], text=True
)
for value in (
    "android.permission.BIND_DREAM_SERVICE",
    "android.service.dreams.DreamService",
    ".AlloyDreamService",
    "com.jeremykenedy.alloytide.settings",
):
    if value not in manifest:
        raise SystemExit("Missing required DreamService or provider declaration: " + value)
print("Verified package/version, DreamService, settings provider and no requested permissions.")
