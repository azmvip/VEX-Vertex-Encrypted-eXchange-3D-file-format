
<p align="center">
  <img src="assets/بدون اسم670_20260511112153.png" width="700"/>
</p>

# VEX-Vertex-Encrypted-eXchange-3D-file-format
An open-source 3D file format built for games and engines. Binary, encrypted, and compressed — it packs the model and texture into a single file. Data is structured GPU-ready with no parsing needed, making load times significantly faster than OBJ or FBX. Lightweight, blazing fast, and readable in any language.

---
*How VEX Works*

Most 3D formats were built for humans — verbose, text-based, and split across multiple files. VEX was built for machines.

Every vertex is packed into exactly 32 bytes in the order a GPU expects: position, normal, and UV coordinates. No reformatting. Just raw data that goes straight into GPU memory in a single call.

The texture is embedded directly inside the VEX file, compressed and sitting right after the geometry block. No separate files. No broken paths. Everything in one place.

A 32-byte header at the front tells you everything — vertex count, face count, encryption, compression, texture presence. Read it once, load everything instantly.

Compare that to OBJ, where your engine parses thousands of text lines before anything reaches the GPU — with the texture in a completely separate file. FBX is worse. Even GLTF still requires JSON parsing and external textures.

VEX skips all of that. Decrypt, decompress, send to GPU. Done in under a millisecond.

One file. One load. Zero overhead.

---
<a herf="VEX_Research.md">
  Learn more about VEX ←
</a>
