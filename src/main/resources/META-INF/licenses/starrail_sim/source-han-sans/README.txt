# Bundled Source Han Sans font

- Font: Adobe Source Han Sans CN Regular, official Simplified Chinese subset.
- Original file: `SubsetOTF/CN/SourceHanSansCN-Regular.otf`.
- Source revision: `a4f7cf94edfb9d7ffbdfc4841de276358bd7e0f2`.
- Source: https://github.com/adobe-fonts/source-han-sans/blob/a4f7cf94edfb9d7ffbdfc4841de276358bd7e0f2/SubsetOTF/CN/SourceHanSansCN-Regular.otf
- License: SIL Open Font License 1.1; see `LICENSE.txt` beside this file.
- SHA-256: `E2BC8A2E7F37474B774FFF8DB758681ECE40BB6947A90D571BCE9DD60671A8E4`.
- Original font bytes are unchanged; the local filename is a resource identifier.

The game loads the bundled bytes, not a Windows-installed font. Missing glyphs fall
back to Minecraft's standard providers. The first development stage uses this
font explicitly in the comparison screen; global font selection follows separately.

After the first in-game comparison, the preview uses `StarRailSharpText` for
axis-aligned 2D text: glyphs are rasterized at their final framebuffer size using
Java2D, then drawn at one texel per device pixel. The JSON provider remains the
fallback for missing characters or unavailable rasterization. No system font or
additional mod is required by this preview renderer.
