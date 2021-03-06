package given.a.spec.with.exception.in.constructor;

import static matchers.IsFailure.failure;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import helpers.SpectrumRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;

public class WhenRunningTheSpec {

    private Result result;

    @Before
    public void before() throws Exception {
        result = SpectrumRunner.run(Fixture.getSpecThatThrowsAnExceptionInConstructor());
    }

    @Test
    public void thereIsOneFailure() throws Exception {
        assertThat(result.getFailureCount(), is(1));
    }

    @Test
    public void theFailureExplainsWhatHappened() throws Exception {
        assertThat(result.getFailures().get(0), is(failure("encountered an error", Fixture.SomeException.class, "kaboom")));
    }

}
