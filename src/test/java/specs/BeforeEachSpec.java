package specs;

import static com.greghaskins.spectrum.Spectrum.beforeEach;
import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.it;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import helpers.SpectrumRunner;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Result;
import org.junit.runner.RunWith;

import com.greghaskins.spectrum.Spectrum;

@RunWith(Spectrum.class)
public class BeforeEachSpec {{

    describe("A beforeEach block", () -> {

        final List<String> items = new ArrayList<String>();

        beforeEach(() -> {
            items.clear();
            items.add("foo");
        });

        it("runs before the first test", () -> {
            assertThat(items, contains("foo"));
            items.add("bar");
        });

        it("runs before the next test to reset the context", () -> {
            assertThat(items, contains("foo"));
            assertThat(items, not(contains("bar")));
        });

    });

    describe("Multiple beforeEach blocks", () -> {

        final List<String> items = new ArrayList<String>();

        beforeEach(() -> {
            items.clear();
        });

        beforeEach(() -> {
            items.add("foo");
        });

        beforeEach(() -> {
            items.add("bar");
        });

        it("run in order before the first test", () -> {
            assertThat(items, contains("foo", "bar"));
        });

        it("and before the other tests", () -> {
            assertThat(items, contains("foo", "bar"));
        });

    });

    describe("A beforeEach block that explodes", () -> {

        it("causes all tests in that context to fail", () -> {
            final Result result = SpectrumRunner.run(getSpecWithExplodingBeforeEach());
            assertThat(result.getFailureCount(), is(2));
        });

    });

}

private static Class<?> getSpecWithExplodingBeforeEach(){
    class Spec {{
        beforeEach(() -> {
            throw new Exception("boom");
        });

        it("should fail", () -> {

        });

        it("should also fail", () -> {

        });

    }}
    return Spec.class;
}

}
