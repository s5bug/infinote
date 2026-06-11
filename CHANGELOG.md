## v1.5.1

### Added

- Added `/infinote transpose <from> <to> <pitchTransposer>`.
  - Transposes Note Blocks in a selected cuboid area.
  - If the transposed note would go outside the vanilla Note Block range, Infinote tries to keep the same sound by switching the block under the Note Block to another configured mapping.
- Added `/bpm set <bpm> <tickPerQuarter>`.
  - Calculates TPS from BPM and tick-per-quarter values.
  - Applies the calculated tick rate directly, so it can be used from command blocks.
- Expanded Minecraft version support.
  - Added support for Minecraft `1.16` to `1.16.5`.
  - Added support for Minecraft `26.1`, `26.1.1`, and `26.1.2`.

### Changed

- Improved command result messages with clearer multi-line output.
- Improved internal command compatibility used by Infinote commands across supported Minecraft versions.

### Fixed

- Fixed custom Note Block playback so custom sounds do not play when the block above the Note Block is not air.
- Fixed config loading so mappings are applied correctly after schema migration.
- Improved config migration errors when `mappings` is missing or invalid.
