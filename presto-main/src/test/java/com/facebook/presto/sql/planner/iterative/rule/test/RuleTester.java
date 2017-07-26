/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.planner.iterative.rule.test;

import com.facebook.presto.Session;
import com.facebook.presto.connector.ConnectorId;
import com.facebook.presto.cost.CostCalculator;
import com.facebook.presto.metadata.Metadata;
import com.facebook.presto.security.AccessControl;
import com.facebook.presto.sql.planner.iterative.Rule;
import com.facebook.presto.sql.planner.iterative.RuleSet;
import com.facebook.presto.sql.planner.plan.PlanNode;
import com.facebook.presto.testing.LocalQueryRunner;
import com.facebook.presto.tpch.TpchConnectorFactory;
import com.facebook.presto.transaction.TransactionManager;
import com.google.common.collect.ImmutableMap;

import java.io.Closeable;
import java.util.Optional;
import java.util.Set;

import static com.facebook.presto.testing.TestingSession.testSessionBuilder;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;

public class RuleTester
        implements Closeable
{
    public static final String CATALOG_ID = "local";
    public static final ConnectorId CONNECTOR_ID = new ConnectorId(CATALOG_ID);

    private final Metadata metadata;
    private final CostCalculator costCalculator;
    private final Session session;
    private final LocalQueryRunner queryRunner;
    private final TransactionManager transactionManager;
    private final AccessControl accessControl;

    public RuleTester()
    {
        session = testSessionBuilder()
                .setCatalog(CATALOG_ID)
                .setSchema("tiny")
                .setSystemProperty("task_concurrency", "1") // these tests don't handle exchanges from local parallel
                .build();

        queryRunner = new LocalQueryRunner(session);
        queryRunner.createCatalog(session.getCatalog().get(),
                new TpchConnectorFactory(1),
                ImmutableMap.of());

        this.metadata = queryRunner.getMetadata();
        this.costCalculator = queryRunner.getCostCalculator();
        this.transactionManager = queryRunner.getTransactionManager();
        this.accessControl = queryRunner.getAccessControl();
    }

    public RuleAssert assertThat(Rule rule)
    {
        return new RuleAssert(metadata, costCalculator, session, rule, transactionManager, accessControl);
    }

    public RuleAssert assertThat(RuleSet rules)
    {
        return assertThat(new RuleSetAdapter(rules));
    }

    @Override
    public void close()
    {
        queryRunner.close();
    }

    private class RuleSetAdapter
            implements Rule
    {
        private final RuleSet ruleSet;

        RuleSetAdapter(RuleSet ruleSet)
        {
            this.ruleSet = ruleSet;
        }

        @Override
        public Optional<PlanNode> apply(PlanNode node, Context context)
        {
            Set<Rule> matching = ruleSet.rules().stream().filter(rule -> rule.getPattern().matches(node)).collect(toSet());
            if (matching.size() == 0) {
                return empty();
            }

            return getOnlyElement(matching).apply(node, context);
        }
    }
}
