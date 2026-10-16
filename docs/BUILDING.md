# Building and assets

Run `bash test.sh` and `bash build.sh` with JDK 21, Android platform 36 and build-tools 36.0.0 installed. Use `bash build.sh --unsigned` for a review artifact. Production signing material is stored outside the repository in a unique protected Alloy Tide keystore; preserve it for updates and never commit it.

The geometry is authored in `TileMesh.java` and animated by the bundled GLES shaders. `tools/build_environment.py` converts the licensed Studio Small 09 HDR to the bundled RGBM texture. Its source hash and author are in `assets/SOURCES.json`; the full CC0 license and Apache notices are bundled. Normal builds need no asset downloads.
