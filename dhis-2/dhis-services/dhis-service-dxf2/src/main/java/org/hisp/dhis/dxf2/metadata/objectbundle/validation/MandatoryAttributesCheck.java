/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
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
package org.hisp.dhis.dxf2.metadata.objectbundle.validation;

import static org.hisp.dhis.dxf2.metadata.objectbundle.validation.ValidationUtils.createObjectReport;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.attribute.AttributeValues;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundle;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.feedback.ObjectReport;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.preheat.Preheat;
import org.hisp.dhis.schema.Schema;
import org.springframework.stereotype.Component;

/**
 * @author Luciano Fiandesio
 */
@Component
public class MandatoryAttributesCheck implements ObjectValidationCheck {
  @Override
  public <T extends IdentifiableObject> void check(
      ObjectBundle bundle,
      Class<T> klass,
      List<T> persistedObjects,
      List<T> nonPersistedObjects,
      ImportStrategy importStrategy,
      ValidationContext ctx,
      Consumer<ObjectReport> addReports) {
    Schema schema = ctx.getSchemaService().getDynamicSchema(klass);
    List<T> objects =
        selectObjectsBasedOnImportStrategy(persistedObjects, nonPersistedObjects, importStrategy);

    if (objects.isEmpty() || !schema.hasAttributeValues()) return;

    Preheat preheat = bundle.getPreheat();
    Set<String> mandatoryAttributes = preheat.getMandatoryAttributes().get(klass);
    if (mandatoryAttributes == null || mandatoryAttributes.isEmpty()) return;

    for (T object : objects) {
      if (object != null && !preheat.isDefault(object)) {
        AttributeValues attributeValues = object.getAttributeValues();
        mandatoryAttributes.stream()
            .filter(attrId -> !isDefined(attrId, attributeValues))
            .forEach(
                attrId -> {
                  addReports.accept(
                      createObjectReport(
                          new ErrorReport(Attribute.class, ErrorCode.E4011, attrId)
                              .setMainId(attrId)
                              .setErrorProperty("value"),
                          object,
                          bundle));
                  ctx.markForRemoval(object);
                });
      }
    }
  }

  private static boolean isDefined(String attrId, AttributeValues values) {
    String value = values.get(attrId);
    return value != null && !value.isEmpty();
  }
}
