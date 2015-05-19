package com.greghaskins.spectrum;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import com.greghaskins.spectrum.Spectrum.Block;
import com.greghaskins.spectrum.Spectrum.Predicate;
import com.greghaskins.spectrum.Spectrum.SpecSection;

class Context implements Executable, SpecSection {

    private final Description description;
    private final List<Block> setupBlocks;
    private final List<Block> teardownBlocks;
    private final List<RunOnceBlock> contextSetupBlocks;
    private final List<Block> contextTeardownBlocks;
    private final Deque<Executable> executables;

    private Predicate<SpecSection> ignoreCondition = null;
    private final Set<String> tags = new HashSet<>();

    public Context(final Description description) {
        this.description = description;
        setupBlocks = new ArrayList<Block>();
        teardownBlocks = new ArrayList<Block>();
        contextSetupBlocks = new ArrayList<RunOnceBlock>();
        contextTeardownBlocks = new ArrayList<Block>();

        executables = new ArrayDeque<Executable>();
    }

    @Override
    public void execute(final RunNotifier notifier) {
        if (descriptionHasAnyTests(description)) {
            addExecutablesForFixtureLevelSetupAndTeardown();
        } else {
            notifier.fireTestIgnored(description);
        }
        for (final Executable child : executables) {
            child.execute(notifier);
        }
    }

    private boolean descriptionHasAnyTests(final Description currentDescription) {
        for (final Description child : currentDescription.getChildren()) {
            if (isTest(child) || descriptionHasAnyTests(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTest(final Description child) {
        return child.getMethodName() != null;
    }

    private void addExecutablesForFixtureLevelSetupAndTeardown() {
        executables.addFirst(new BlockExecutable(description, new CompositeBlock(contextSetupBlocks)));
        executables.addLast(new BlockExecutable(description, new CompositeBlock(contextTeardownBlocks)));
    }

    public void addTestSetup(final Block block) {
        setupBlocks.add(block);
    }

    public void addTestTeardown(final Block block) {
        teardownBlocks.add(block);
    }

    public void addContextSetup(final Block block) {
        contextSetupBlocks.add(new RunOnceBlock(block));
    }

    public void addContextTeardown(final Block block) {
        contextTeardownBlocks.add(block);
    }

    public Test addTest(final String behavior, final Block block) {
        final Description testDescription = Description.createTestDescription(description.getClassName(),
                behavior + (block instanceof EmptyBlock ? " - [pending]" : ""));
        final CompositeBlock testBlock = putTestBlockInContext(block);
        final Test test = new Test(testDescription, testBlock);
        description.addChild(testDescription);
        executables.add(test);
        return test;
    }

    private CompositeBlock putTestBlockInContext(final Block testBlock) {
        return new CompositeBlock(new CompositeBlock(contextSetupBlocks), new CompositeBlock(setupBlocks), testBlock,
                new CompositeBlock(teardownBlocks));
    }

    public void addChild(final Context childContext) {
        description.addChild(childContext.description);
        childContext.addContextSetup(new CompositeBlock(contextSetupBlocks));
        childContext.addTestSetup(new CompositeBlock(setupBlocks));
        childContext.addTestTeardown(new CompositeBlock(teardownBlocks));
        executables.add(childContext);
    }

    @Override
    public SpecSection ignore() {
        return ignoreWhen((x) -> true);
    }

    @Override
    public SpecSection ignoreWhen(Predicate<SpecSection> condition) {
        ignoreCondition = condition;
        if (condition.test(this)) {
            for (Executable exe : executables) {
                ((Test)exe).ignore();
            }
        }
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
    public SpecSection tagWith(String... tags) {
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
