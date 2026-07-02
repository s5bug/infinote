package com.rs256.infinote.transpose;

import com.rs256.infinote.Infinote;
import com.rs256.infinote.compat.ComponentCompat;
import com.rs256.infinote.compat.IdCompat;
import com.rs256.infinote.compat.RegistryCompat;
import com.rs256.infinote.config.BlockSoundConfigCompiled;
import com.rs256.infinote.config.InfinoteConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class TransposeUtil {
    public static Map<String, Map<String, Float>> buildTransposeCache() {
        Map<String, Map<String, Float>> cache = new HashMap<>();

        for (Map.Entry<String, BlockSoundConfigCompiled> entry : InfinoteConfig.BLOCK_SOUNDS_COMPILED.entrySet()) {
            String blockId = entry.getKey();
            BlockSoundConfigCompiled cfg = entry.getValue();
            if (cfg == null) continue;

            Map<String, Float> byBlock = cache.computeIfAbsent(cfg.sound, k -> new HashMap<>());

            if (byBlock.containsKey(blockId)) {
                Infinote.LOGGER.warn("Duplicate transpose candidate: sound={}, block={}", cfg.sound, blockId);
            }

            byBlock.put(blockId, cfg.pitchShift);
        }

        Infinote.LOGGER.info("Built transpose cache for {} sound ids.", cache.size());
        for (Map.Entry<String, Map<String, Float>> e : cache.entrySet()) {
            Infinote.LOGGER.info("Transpose cache: sound={} candidates={}", e.getKey(), e.getValue().size());
        }

        return cache;
    }

    public static MutableComponent transposeBlocks(Level level, BlockPos from, BlockPos to, int k, Map<String, Map<String, Float>> cache) {

        int minX = Math.min(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());

        int maxX = Math.max(from.getX(), to.getX());
        int maxY = Math.max(from.getY(), to.getY());
        int maxZ = Math.max(from.getZ(), to.getZ());

        int changed = 0;
        int notNoteBlock = 0;
        int noMapping = 0;
        int noCandidates = 0;
        int invalid = 0;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    mutable.set(x, y, z);

                    String result = transposeBlock(level, mutable, k, cache);

                    switch (result) {
                        case "changed" -> changed++;
                        case "not_note_block" -> notNoteBlock++;
                        case "no_mapping" -> noMapping++;
                        case "no_candidates" -> noCandidates++;
                        case "invalid_candidate_block" -> invalid++;
                    }
                }
            }
        }

        Infinote.LOGGER.info("transposeArea finished: changed={}, noMapping={}, noCandidates={}", changed, noMapping, noCandidates);

        return ComponentCompat.buildWithBreak(
                ComponentCompat.literal("Transpose result").withStyle(ChatFormatting.GOLD),
                ComponentCompat.literal("transposed: ")
                        .append(ComponentCompat.literal(String.valueOf(changed)).withStyle(ChatFormatting.GREEN)),
                ComponentCompat.literal("no mapping: ")
                        .append(ComponentCompat.literal(String.valueOf(noMapping)).withStyle(ChatFormatting.YELLOW))
        );
    }

    public static String transposeBlock(Level level, BlockPos pos, int k, Map<String, Map<String, Float>> cache) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof NoteBlock)) {
            return "not_note_block";
        }

        int currentNote = state.getValue(NoteBlock.NOTE);
        int targetNote = currentNote + k;

        if (0 <= targetNote && targetNote <= 24) {
            level.setBlock(pos, state.setValue(NoteBlock.NOTE, targetNote), 3);
            return "changed";
        }

        BlockPos belowPos = pos.below();
        String belowBlockId = RegistryCompat.getKey(level.getBlockState(belowPos).getBlock());
        BlockSoundConfigCompiled currentConfig = InfinoteConfig.BLOCK_SOUNDS_COMPILED.get(belowBlockId);

        if (currentConfig == null) {
            return "no_mapping";
        }

        String soundId = currentConfig.sound;
        float currentPitchShift = currentConfig.pitchShift;
        float targetSemitone = currentNote + currentPitchShift + k;

        Map<String, Float> candidates = cache.get(soundId);
        if (candidates == null || candidates.isEmpty()) {
            return "no_candidates";
        }

        String bestBlockId = null;
        float bestPitchShift = 0.0f;
        int bestNote = -1;
        boolean bestOctaveAligned = false;
        float bestShiftDistance = Float.MAX_VALUE;

        for (Map.Entry<String, Float> entry : candidates.entrySet()) {
            String candidateBlockId = entry.getKey();
            float candidatePitchShift = entry.getValue();

            float candidateNoteFloat = targetSemitone - candidatePitchShift;
            int candidateNote = Math.round(candidateNoteFloat);

            if (Math.abs(candidateNoteFloat - candidateNote) > 0.0001f) continue;
            if (candidateNote < 0 || candidateNote > 24) continue;

            float deltaShift = candidatePitchShift - currentPitchShift;
            boolean octaveAligned = Math.floorMod(Math.round(deltaShift), 12) == 0;
            float shiftDistance = Math.abs(deltaShift);

            boolean better = false;
            if (bestBlockId == null) {
                better = true;
            } else if (octaveAligned && !bestOctaveAligned) {
                better = true;
            } else if (octaveAligned == bestOctaveAligned && shiftDistance < bestShiftDistance) {
                better = true;
            }

            if (better) {
                bestBlockId = candidateBlockId;
                bestPitchShift = candidatePitchShift;
                bestNote = candidateNote;
                bestOctaveAligned = octaveAligned;
                bestShiftDistance = shiftDistance;
            }
        }

        if (bestBlockId == null) {
            Infinote.LOGGER.info("transpose skipped at {}: no valid candidate for sound={} targetSemitone={}", pos, soundId, targetSemitone);
            return "no_candidates";
        }

        var bestBlockIdObj = IdCompat.idFromString(bestBlockId);
        if (bestBlockIdObj == null) {
            Infinote.LOGGER.warn("transpose candidate block invalid: {}", bestBlockId);
            return "invalid_candidate_block";
        }

        Block newBelowBlock = RegistryCompat.getBlock(bestBlockId);
        if (newBelowBlock == null) {
            return "invalid_candidate_block";
        }

        level.setBlock(belowPos, newBelowBlock.defaultBlockState(), 3);
        level.setBlock(pos, state.setValue(NoteBlock.NOTE, bestNote), 3);

        Infinote.LOGGER.info(
                "transposed {}: sound={} note {} -> {} using below {} -> {} pitchShift {} -> {}",
                pos, soundId, currentNote, bestNote, belowBlockId, bestBlockId, currentPitchShift, bestPitchShift
        );

        return "changed";
    }
}