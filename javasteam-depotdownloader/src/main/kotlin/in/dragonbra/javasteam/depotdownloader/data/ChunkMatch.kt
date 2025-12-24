package `in`.dragonbra.javasteam.depotdownloader.data

import `in`.dragonbra.javasteam.types.ChunkData

/**
 * Pairs matching chunks between old and new depot manifests for differential updates.
 * Used during file validation to identify chunks that can be reused from existing
 * files, avoiding unnecessary re-downloads when only portions of a file have changed.
 *
 * @property oldChunk Chunk from the previously installed manifest
 * @property newChunk Corresponding chunk from the new manifest being downloaded
 *
 * @author Oxters
 * @since Oct 29, 2024
 */
data class ChunkMatch(val oldChunk: ChunkData, val newChunk: ChunkData)
