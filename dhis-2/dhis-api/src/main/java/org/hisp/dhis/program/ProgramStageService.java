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
package org.hisp.dhis.program;

import java.util.List;
import org.hisp.dhis.dataentryform.DataEntryForm;

/**
 * @author Abyot Asalefew
 */
public interface ProgramStageService {
  // -------------------------------------------------------------------------
  // ProgramStage
  // -------------------------------------------------------------------------

  /**
   * Adds an {@link ProgramStage}
   *
   * @param programStage The to ProgramStage add.
   * @return A generated unique id of the added {@link ProgramStage}.
   */
  long saveProgramStage(ProgramStage programStage);

  /**
   * Deletes a {@link ProgramStage}.
   *
   * @param programStage the ProgramStage to delete.
   */
  void deleteProgramStage(ProgramStage programStage);

  /**
   * Updates an {@link ProgramStage}.
   *
   * @param programStage the ProgramStage to update.
   */
  void updateProgramStage(ProgramStage programStage);

  /**
   * Returns a {@link ProgramStage}.
   *
   * @param id the id of the ProgramStage to return.
   * @return the ProgramStage with the given id
   */
  ProgramStage getProgramStage(long id);

  /**
   * Returns the {@link ProgramStage} with the given UID.
   *
   * @param uid the UID.
   * @return the ProgramStage with the given UID, or null if no match.
   */
  ProgramStage getProgramStage(String uid);

  /**
   * Retrieve all ProgramStages associated with the given DataEntryForm.
   *
   * @param dataEntryForm the DataEntryForm.
   * @return a list og ProgramStages.
   */
  List<ProgramStage> getProgramStagesByDataEntryForm(DataEntryForm dataEntryForm);

  /**
   * Retrieve all ProgramStages associated with the given Program.
   *
   * @param program the Program.
   * @return a list og ProgramStages.
   */
  List<ProgramStage> getProgramStagesByProgram(Program program);
}
