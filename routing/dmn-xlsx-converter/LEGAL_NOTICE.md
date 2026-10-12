This module is based on the
[camunda-dmn-xlsx:xlsx-dmn-converter](https://github.com/camunda-community-hub/camunda-dmn-xlsx/tree/master/xlsx-dmn-converter)
module and retains its Apache License 2.0 licensing.

The Jakarta/Camunda 7.20 adaptation originated in
[camunda-community-hub/camunda-dmn-xlsx#53](https://github.com/camunda-community-hub/camunda-dmn-xlsx/pull/53).

KADAI contains subsequent local maintenance changes to this vendored module.

The original Jakarta adaptation in upstream PR #53 upgraded docx4j to 11.4.9.
KADAI's current dependency version is managed by the parent build.

The current KADAI-specific behavior includes:

* `XlsxConverter` converts exactly one worksheet per invocation. `worksheetIndex` is zero-based,
  defaults to `0`, and an invalid index fails conversion.
* `XlsxConverter#convert(InputStream)` declares the checked
  `XlsxConversionException` when the document cannot be loaded or converted.
* `historyTimeToLive` is applied to converted decisions. Its default is 180 days (`P180D`).
