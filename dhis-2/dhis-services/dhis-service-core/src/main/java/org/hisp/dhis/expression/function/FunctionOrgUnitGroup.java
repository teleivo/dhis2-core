/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.expression.function;

import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.hisp.dhis.antlr.ParserExceptionWithoutContext;
import org.hisp.dhis.expression.ExpressionParams;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.parser.expression.CommonExpressionVisitor;
import org.hisp.dhis.parser.expression.ExpressionItem;
import org.hisp.dhis.parser.expression.antlr.ExpressionParser;

/**
 * Function orgUnit.group
 *
 * <p>Does the current orgUnit belong to one of the orgUnit groups?
 *
 * @author Jim Grace
 */
public class FunctionOrgUnitGroup implements ExpressionItem {
  @Override
  public Object getDescription(ExpressionParser.ExprContext ctx, CommonExpressionVisitor visitor) {
    for (TerminalNode uid : ctx.UID()) {
      OrganisationUnitGroup orgUnitGroup =
          visitor.getIdObjectManager().get(OrganisationUnitGroup.class, uid.getText());

      if (orgUnitGroup == null) {
        throw new ParserExceptionWithoutContext(
            "No organization unit group defined for " + uid.getText());
      }

      visitor.getItemDescriptions().put(uid.getText(), orgUnitGroup.getDisplayName());
    }

    return false;
  }

  @Override
  public Object getExpressionInfo(
      ExpressionParser.ExprContext ctx, CommonExpressionVisitor visitor) {
    visitor
        .getInfo()
        .getOrgUnitGroupIds()
        .addAll(ctx.UID().stream().map(TerminalNode::getText).collect(Collectors.toList()));

    return false;
  }

  @Override
  public Object evaluate(ExpressionParser.ExprContext ctx, CommonExpressionVisitor visitor) {
    ExpressionParams params = visitor.getParams();

    if (params.getOrgUnit() != null) {
      for (TerminalNode uid : ctx.UID()) {
        OrganisationUnitGroup oug = params.getOrgUnitGroupMap().get(uid.getText());

        if (oug != null && oug.getMembers().contains(params.getOrgUnit())) {
          return true;
        }
      }
    }

    return false;
  }
}
