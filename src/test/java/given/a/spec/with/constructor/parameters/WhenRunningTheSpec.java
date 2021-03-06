package given.a.spec.with.constructor.parameters;

import static matchers.IsFailure.failure;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import helpers.SpectrumRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;

import com.greghaskins.spectrum.UnableToConstructSpecException;

public class WhenRunningTheSpec {

    private Result result;

    @Before
    public void before() throws Exception {
        result = SpectrumRunner.run(Fixture.getSpecThatRequiresAConstructorParameter());
    }

    @Test
    public void thereIsOneFailure() throws Exception {
        assertThat(result.getFailureCount(), is(1));
    }

    @Test
    public void theFailureExplainsWhatHappened() throws Exception {
        assertThat(
                result.getFailures().get(0),
                is(failure("encountered an error", UnableToConstructSpecException.class, Fixture
                        .getSpecThatRequiresAConstructorParameter().getName())));
    }

}
