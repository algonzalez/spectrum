package com.greghaskins.spectrum;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import com.greghaskins.spectrum.Spectrum.Block;
import com.greghaskins.spectrum.Spectrum.Predicate;
import com.greghaskins.spectrum.Spectrum.SpecSection;

import java.util.HashSet;
import java.util.Set;

class Test implements Executable, SpecSection {
    private final Description description;
    private final Block block;

    private Predicate<SpecSection> ignoreCondition = null;
    private final Set<String> tags = new HashSet<>();

    public Test(final Description description, final Block block) {
        this.description = description;
        this.block = block;
    }

    @Override
    public void execute(final RunNotifier notifier) {
        if (this.willBeIgnored()) {
            notifier.fireTestIgnored(description);
        } else {
            notifier.fireTestStarted(description);
            try {
                block.run();
            } catch (final Throwable e) {
                notifier.fireTestFailure(new Failure(description, e));
            }
            notifier.fireTestFinished(description);
        }
    }

    @Override
    public SpecSection ignore() {
        return ignoreWhen((test) -> true);
    }

    @Override
    public SpecSection ignoreWhen(Predicate<SpecSection> condition) {
        ignoreCondition = condition;
        return this;
    }

    @Override
    public SpecSection onlyWhen(Predicate<SpecSection> condition) {
        ignoreCondition = (context) -> !condition.test(context);
        return this;
    }

    public boolean willBeIgnored() {
        return ignoreCondition != null && ignoreCondition.test(this);
    }

    @Override
    public Test tagWith(String... tags) {
        for(String tag : tags) {
            if (tag != null && !tag.isEmpty()) {
                this.tags.add(tag);
            }
        }
        return this;
    }

    @Override
    public boolean hasTag(String tag) {
        return tag != null && !tag.isEmpty() && tags.contains(tag);
    }

    public Iterable<String> getTags() {
        return tags;
    }
}

