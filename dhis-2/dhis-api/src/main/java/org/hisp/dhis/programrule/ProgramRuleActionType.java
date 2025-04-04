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
package org.hisp.dhis.programrule;

import static org.hisp.dhis.programrule.ProgramRuleActionEvaluationTime.ON_COMPLETE;
import static org.hisp.dhis.programrule.ProgramRuleActionEvaluationTime.ON_DATA_ENTRY;
import static org.hisp.dhis.programrule.ProgramRuleActionEvaluationTime.getAll;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.notification.NotificationTemplate;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;

/**
 * @author Markus Bekken
 */
@RequiredArgsConstructor
public enum ProgramRuleActionType {
  DISPLAYTEXT("displaytext"),
  DISPLAYKEYVALUEPAIR("displaykeyvaluepair"),
  HIDEFIELD("hidefield"),
  HIDESECTION("hidesection"),
  HIDEPROGRAMSTAGE("hideprogramstage"),
  ASSIGN("assign", Set.of(ON_DATA_ENTRY, ON_COMPLETE)),
  SHOWWARNING("showwarning"),
  WARNINGONCOMPLETE("warningoncomplete"),
  SHOWERROR("showerror"),
  ERRORONCOMPLETE("erroroncomplete"),
  CREATEEVENT("createevent"),
  SETMANDATORYFIELD("setmandatoryfield", Set.of(ON_DATA_ENTRY)),
  SENDMESSAGE("sendmessage", Set.of(ON_DATA_ENTRY, ON_COMPLETE)),
  SCHEDULEMESSAGE("schedulemessage", Set.of(ON_DATA_ENTRY, ON_COMPLETE)),
  HIDEOPTION("hideoption"),
  SHOWOPTIONGROUP("showoptiongroup"),
  HIDEOPTIONGROUP("hideoptiongroup");

  final String value;

  final Set<ProgramRuleActionEvaluationTime> whenToRun;

  /** Actions which require server-side execution. */
  public static final Set<ProgramRuleActionType> IMPLEMENTED_ACTIONS =
      Set.of(SENDMESSAGE, SCHEDULEMESSAGE, ASSIGN);

  /** Actions associated with {@link DataElement} or {@link TrackedEntityAttribute}. */
  public static final Set<ProgramRuleActionType> DATA_LINKED_TYPES =
      Set.of(HIDEFIELD, SETMANDATORYFIELD, HIDEOPTION, HIDEOPTIONGROUP, SHOWOPTIONGROUP);

  /** Actions associated with {@link NotificationTemplate}. */
  public static final Set<ProgramRuleActionType> NOTIFICATION_LINKED_TYPES =
      Set.of(SENDMESSAGE, SCHEDULEMESSAGE);

  /** Complete set of actions which require server-side execution. */
  public static final Set<ProgramRuleActionType> SERVER_SUPPORTED_TYPES =
      Set.of(
          SENDMESSAGE,
          SCHEDULEMESSAGE,
          SHOWERROR,
          SHOWWARNING,
          ERRORONCOMPLETE,
          WARNINGONCOMPLETE,
          ASSIGN,
          SETMANDATORYFIELD);

  ProgramRuleActionType(String value) {
    this.value = value;
    this.whenToRun = getAll();
  }
}
