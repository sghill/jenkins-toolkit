package net.sghill.jenkins.toolkit.svgs;

import org.jetbrains.annotations.Nullable;

public class Categorizer {
    public Result categorize(@Nullable OssState oss, InternalState internal) {
        if (oss == null) {
            return Result.UNLISTED;
        }
        if (internal == InternalState.REMOVED) {
            return Result.GOOD_INVESTMENT;
        }
        switch (oss) {
            case ABANDONED:
            case DEPRECATED:
            case MERGED:
            case PROPOSED:
                return Result.ADOPT_OR_REMOVE;
            case UPDATED:
                return Result.ENSURE_VERSION;
        }
        return Result.UNKNOWN;
    }
}
