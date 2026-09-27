# TrendPulse public source contract

TrendPulse is an Android research workspace for exact quoted Google Trends phrases. Its public source builds with the on-device Termux script and a synthetic sample plan. Personal case plans, captures, private methodology notes, and signing keys belong in a local installation or a private archive.

A query record keeps its exact UTF-8 phrases, full ordered comparison group, time range, Trends geography, category, search type, URL, and capture timestamp. A grouped chart and a solo chart have separate 0–100 normalization contexts. A URL alone may omit a category label visible in the page, so the captured record preserves the category ID.

The source supports named case-plan selection, reviewed language variants in English, Hebrew, Spanish, French, Arabic, and Chinese, a five-phrase comparison limit, desktop/mobile view, app-private raw and stamped screenshots, SHA-256 manifests, a captured-data browser, ZIP export on demand, and an in-app paced run while the app remains open. A 429 pauses further requests.

The current source is a beta. Future work includes system-assisted translation, OCR, multiple live tabs, scheduled/background runs, storage quota, an app lock, and project interchange. Build and test on the target Android device before general release.
