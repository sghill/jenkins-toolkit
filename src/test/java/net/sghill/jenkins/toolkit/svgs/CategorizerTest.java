package net.sghill.jenkins.toolkit.svgs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

class CategorizerTest {
    private final Categorizer categorizer = new Categorizer();

    @ParameterizedTest
    @EnumSource(InternalState.class)
    void shouldTurnNullIntoUnlisted(InternalState internal) {
        Result actual = categorizer.categorize(null, internal);
        assertThat(actual).isEqualTo(Result.UNLISTED);
    }

    @ParameterizedTest
    @MethodSource("adoptOrRemove")
    void shouldTurnIntoAdoptOrRemove(OssState oss, InternalState internal) {
        Result actual = categorizer.categorize(oss, internal);
        assertThat(actual).isEqualTo(Result.ADOPT_OR_REMOVE);
    }

    @ParameterizedTest
    @EnumSource(value = InternalState.class, mode = EXCLUDE, names = "REMOVED")
    void shouldTurnIntoEnsureVersion(InternalState internal) {
        Result actual = categorizer.categorize(OssState.UPDATED, internal);
        assertThat(actual).isEqualTo(Result.ENSURE_VERSION);
    }

    @ParameterizedTest
    @EnumSource(value = OssState.class)
    void shouldTurnIntoGoodInvestment(OssState oss) {
        Result actual = categorizer.categorize(oss, InternalState.REMOVED);
        assertThat(actual).isEqualTo(Result.GOOD_INVESTMENT);
    }

    @ParameterizedTest
    @MethodSource("all")
    void allCombinationsShouldBeHandled(OssState oss, InternalState internal) {
        Result actual = categorizer.categorize(oss, internal);
        assertThat(actual).isNotEqualTo(Result.UNKNOWN);
    }

    public static Stream<Arguments> all() {
        List<Arguments> args = new LinkedList<>();
        for (OssState oss : OssState.values()) {
            for (InternalState internal : InternalState.values()) {
                args.add(Arguments.of(oss, internal));
            }
        }
        return args.stream();
    }

    public static Stream<Arguments> adoptOrRemove() {
        List<Arguments> args = new LinkedList<>();
        for (OssState oss : OssState.values()) {
            if (oss == OssState.UPDATED) {
                continue;
            }
            for (InternalState internal : InternalState.values()) {
                if (internal == InternalState.REMOVED) {
                    continue;
                }
                args.add(Arguments.of(oss, internal));
            }
        }
        return args.stream();
    }
}
