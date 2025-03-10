/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.test.impl.score.stream;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.BiFunction;

import org.drools.core.util.Drools;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.stream.AbstractConstraintStreamScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.stream.BavetConstraintStreamScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.stream.DroolsConstraintStreamScoreDirectorFactory;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public final class DefaultConstraintVerifier<ConstraintProvider_ extends ConstraintProvider, Solution_, Score_ extends Score<Score_>>
        implements ConstraintVerifier<ConstraintProvider_, Solution_> {

    private final ConstraintProvider_ constraintProvider;
    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private ConstraintStreamImplType constraintStreamImplType;
    private Boolean droolsAlphaNetworkCompilationEnabled;

    public DefaultConstraintVerifier(ConstraintProvider_ constraintProvider, SolutionDescriptor<Solution_> solutionDescriptor) {
        this.constraintProvider = constraintProvider;
        this.solutionDescriptor = solutionDescriptor;
    }

    public ConstraintStreamImplType getConstraintStreamImplType() {
        return Objects.requireNonNullElse(constraintStreamImplType, ConstraintStreamImplType.DROOLS);
    }

    @Override
    public ConstraintVerifier<ConstraintProvider_, Solution_> withConstraintStreamImplType(
            ConstraintStreamImplType constraintStreamImplType) {
        requireNonNull(constraintStreamImplType);
        this.constraintStreamImplType = constraintStreamImplType;
        return this;
    }

    public boolean isDroolsAlphaNetworkCompilationEnabled() {
        return Objects.requireNonNullElse(droolsAlphaNetworkCompilationEnabled, !Drools.isNativeImage());
    }

    @Override
    public ConstraintVerifier<ConstraintProvider_, Solution_> withDroolsAlphaNetworkCompilationEnabled(
            boolean droolsAlphaNetworkCompilationEnabled) {
        this.droolsAlphaNetworkCompilationEnabled = droolsAlphaNetworkCompilationEnabled;
        return this;
    }

    // ************************************************************************
    // Verify methods
    // ************************************************************************

    @Override
    public DefaultSingleConstraintVerification<Solution_, Score_> verifyThat(
            BiFunction<ConstraintProvider_, ConstraintFactory, Constraint> constraintFunction) {
        requireNonNull(constraintFunction);
        AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory =
                createScoreDirectorFactory(constraintFunction);
        return new DefaultSingleConstraintVerification<>(scoreDirectorFactory);
    }

    private AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> createScoreDirectorFactory(
            BiFunction<ConstraintProvider_, ConstraintFactory, Constraint> constraintFunction) {
        ConstraintProvider actualConstraintProvider = constraintFactory -> new Constraint[] {
                constraintFunction.apply(constraintProvider, constraintFactory)
        };
        return createScoreDirectorFactory(actualConstraintProvider);
    }

    private AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> createScoreDirectorFactory(
            ConstraintProvider constraintProvider) {
        ConstraintStreamImplType constraintStreamImplType_ = getConstraintStreamImplType();
        switch (constraintStreamImplType_) {
            case DROOLS:
                return new DroolsConstraintStreamScoreDirectorFactory<>(solutionDescriptor, constraintProvider,
                        isDroolsAlphaNetworkCompilationEnabled());
            case BAVET:
                if (droolsAlphaNetworkCompilationEnabled != null && droolsAlphaNetworkCompilationEnabled) {
                    throw new IllegalArgumentException("Constraint stream implementation (" + constraintStreamImplType_ +
                            ") does not support droolsAlphaNetworkCompilationEnabled ("
                            + droolsAlphaNetworkCompilationEnabled + ").");
                }
                return new BavetConstraintStreamScoreDirectorFactory<>(solutionDescriptor, constraintProvider);
            default:
                throw new UnsupportedOperationException("Unsupported constraintStreamImplType ("
                        + this.constraintStreamImplType + ").");
        }
    }

    @Override
    public DefaultMultiConstraintVerification<Solution_, Score_> verifyThat() {
        AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory =
                createScoreDirectorFactory(constraintProvider);
        return new DefaultMultiConstraintVerification<>(scoreDirectorFactory, constraintProvider);
    }

}
