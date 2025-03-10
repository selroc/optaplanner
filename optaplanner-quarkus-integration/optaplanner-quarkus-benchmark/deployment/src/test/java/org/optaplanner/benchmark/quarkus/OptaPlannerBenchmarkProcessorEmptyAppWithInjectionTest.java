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

package org.optaplanner.benchmark.quarkus;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import io.quarkus.arc.Arc;
import io.quarkus.test.QuarkusUnitTest;

public class OptaPlannerBenchmarkProcessorEmptyAppWithInjectionTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.optaplanner.benchmark.solver.termination.spent-limit", "5s")
            .overrideConfigKey("quarkus.arc.unremovable-types", PlannerBenchmarkFactory.class.getName())
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses());

    @Test
    public void emptyAppDoesNotCrash() {
        assertThatIllegalStateException().isThrownBy(() -> Arc.container().instance(PlannerBenchmarkFactory.class).get())
                .withMessageContaining("The " + PlannerBenchmarkFactory.class.getName() + " is not available as there are no");
    }

}
