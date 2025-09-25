/*
 * Copyright 2025 Google LLC
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

package com.google.android.`as`.oss.policies.api.contextrules

import com.google.android.libraries.pcc.chronicle.util.TypedMap

/**
 * This defines the basic structure of a context rule.
 *
 * `name` and `operands` are used internally for the ledger. `operands` refers to any rules that are
 * used within the current rule.
 */
interface PolicyContextRule {
  val name: String
  val operands: List<PolicyContextRule>

  /** Returns whether a rule is true/false, for the given context */
  operator fun invoke(context: TypedMap): Boolean
}

/**
 * This context rule always returns true, regardless of the context. It can be used as a default
 * ContextRule if no rules need to be applied.
 */
object All : PolicyContextRule {
  override val name = "All"
  override val operands: List<PolicyContextRule> = emptyList()

  override fun invoke(context: TypedMap): Boolean = true
}

/** Allows policy rule expressions such as `Rule1 and Rule2` */
infix fun PolicyContextRule.and(other: PolicyContextRule): PolicyContextRule = And(this, other)

/** Used to perform an `&&` operation on the boolean evaluations of two PolicyContextRules */
class And(private val lhs: PolicyContextRule, private val rhs: PolicyContextRule) :
  PolicyContextRule {
  override val name = "And"
  override val operands: List<PolicyContextRule> = listOf(lhs, rhs)

  override fun invoke(context: TypedMap): Boolean = lhs(context) && rhs(context)
}

/** Allows policy rule expressions such as `Rule1 or Rule2` */
infix fun PolicyContextRule.or(other: PolicyContextRule): PolicyContextRule = Or(this, other)

/** Used to perform an `||` operation on the boolean evaluations of two PolicyContextRules */
class Or(private val lhs: PolicyContextRule, private val rhs: PolicyContextRule) :
  PolicyContextRule {
  override val name = "Or"
  override val operands: List<PolicyContextRule> = listOf(lhs, rhs)

  override fun invoke(context: TypedMap): Boolean = lhs(context) || rhs(context)
}

/** Allows policy rule expressions such as `not(Rule1)` */
fun not(rule: PolicyContextRule): PolicyContextRule = Not(rule)

/** Used to perform an `!` operation on the boolean evaluation of a PolicyContextRule */
class Not(private val inner: PolicyContextRule) : PolicyContextRule {
  override val name = "Not"
  override val operands: List<PolicyContextRule> = listOf(inner)

  override fun invoke(context: TypedMap): Boolean = !inner(context)
}
