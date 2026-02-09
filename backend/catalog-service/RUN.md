## Encoding Note (BOM)

If IntelliJ shows `illegal character: '\ufeff':1`, one or more Java files were saved with UTF-8 BOM.

Use IntelliJ settings to prevent this:
- `File | Settings | Editor | File Encodings`
- Set `Global Encoding` and `Project Encoding` to `UTF-8`
- Set `Default encoding for properties files` to `UTF-8`
- Keep BOM disabled for UTF-8 files

This repository normalizes text files with `.gitattributes`, and Java sources should remain UTF-8 without BOM.
