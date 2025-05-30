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
package org.hisp.dhis.fieldfiltering;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.hibernate.HibernateProxyUtils;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.PropertyType;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.security.acl.Access;
import org.hisp.dhis.system.util.ReflectionUtils;
import org.hisp.dhis.user.sharing.Sharing;
import org.hisp.dhis.user.sharing.UserAccess;
import org.hisp.dhis.user.sharing.UserGroupAccess;
import org.springframework.stereotype.Component;

/**
 * @author Morten Olav Hansen
 */
@Component
@RequiredArgsConstructor
public class FieldPathHelper {
  private final SchemaService schemaService;

  public List<FieldPath> apply(List<FieldPath> fieldPaths, Class<?> rootKlass) {
    if (fieldPaths.isEmpty() || rootKlass == null) {
      return List.of();
    }

    Map<String, FieldPath> fieldPathMap =
        fieldPaths.stream()
            .filter(not(FieldPath::isPreset).and(not(FieldPath::isExclude)))
            .collect(Collectors.toMap(FieldPath::toFullPath, Function.identity()));

    applyProperties(fieldPathMap.values(), rootKlass);

    List<FieldPath> presets = fieldPaths.stream().filter(FieldPath::isPreset).toList();
    applyPresets(presets, fieldPathMap, rootKlass);

    calculatePathCount(fieldPathMap.values())
        .forEach(
            (k, v) -> {
              if (v > 1L) {
                return;
              }

              applyDefaults(fieldPathMap.get(k), rootKlass, fieldPathMap);
            });

    List<FieldPath> exclusions = fieldPaths.stream().filter(FieldPath::isExclude).toList();
    applyExclusions(exclusions, fieldPathMap);

    return new ArrayList<>(fieldPathMap.values());
  }

  /**
   * Applies (recursively) default expansion on the remaining field paths, for example the path
   * 'dataElements.dataElementGroups' would get expanded to 'dataElements.dataElementGroups.id' so
   * that we expose the reference identifier.
   */
  private void applyDefaults(
      FieldPath fieldPath, Class<?> klass, Map<String, FieldPath> fieldPathMap) {
    List<String> paths = new ArrayList<>(fieldPath.getPath());
    paths.add(fieldPath.getName());

    Schema schema = getSchemaByPath(paths, klass);

    if (schema == null) {
      return;
    }

    Property property = fieldPath.getProperty();

    if (isComplex(property)) {
      expandComplex(fieldPathMap, paths, schema);
    } else if (isReference(property)) {
      expandReference(fieldPathMap, paths, schema);
    }
  }

  private void applyDefault(FieldPath fieldPath, Map<String, FieldPath> fieldPathMap) {
    List<String> paths = new ArrayList<>(fieldPath.getPath());
    paths.add(fieldPath.getName());

    Property property = fieldPath.getProperty();
    fieldPathMap.put(fieldPath.toFullPath(), fieldPath);

    if (property.isSimple()) {
      return;
    }

    Schema schema =
        schemaService.getDynamicSchema(
            property.isCollection() ? property.getItemKlass() : property.getKlass());

    if (schema == null) {
      return;
    }

    if (isComplex(property)) {
      expandComplex(fieldPathMap, paths, schema);
    } else if (isReference(property)) {
      expandReference(fieldPathMap, paths, schema);
    }
  }

  private void expandReference(
      Map<String, FieldPath> fieldPathMap, List<String> paths, Schema schema) {
    Property idProperty = schema.getProperty("id");

    FieldPath fp =
        new FieldPath(
            idProperty.isCollection() ? idProperty.getCollectionName() : idProperty.getName(),
            paths);
    fp.setProperty(idProperty);

    fieldPathMap.put(fp.toFullPath(), fp);
  }

  private void expandComplex(
      Map<String, FieldPath> fieldPathMap, List<String> paths, Schema schema) {
    schema
        .getProperties()
        .forEach(
            p -> {
              FieldPath fp =
                  new FieldPath(p.isCollection() ? p.getCollectionName() : p.getName(), paths);
              fp.setProperty(p);

              // check if anything else needs to be expanded
              applyDefault(fp, fieldPathMap);
            });
  }

  private void applyProperties(Collection<FieldPath> fieldPaths, Class<?> rootKlass) {
    fieldPaths.forEach(
        fp -> {
          Schema schema = getSchemaByPath(fp.getPath(), rootKlass);

          if (schema != null) {
            fp.setProperty(schema.getProperty(fp.getName()));
          }
        });
  }

  /**
   * Applies field presets. See {@link FieldPreset}.
   *
   * @param presets the list of {@link FieldPath}.
   * @param fieldPathMap mapping of full path and {@link FieldPath} to be populated.
   * @param rootKlass the root class type of the entity.
   */
  public void applyPresets(
      List<FieldPath> presets, Map<String, FieldPath> fieldPathMap, Class<?> rootKlass) {
    List<FieldPath> fieldPaths = new ArrayList<>();

    for (FieldPath preset : presets) {
      Schema schema = getSchemaByPath(preset.getPath(), rootKlass);

      if (schema == null) {
        continue;
      }
      if (FieldPreset.ALL.getName().equals(preset.getName())) {
        schema.getProperties().forEach(p -> fieldPaths.add(toFieldPath(preset.getPath(), p)));
      } else if (FieldPreset.OWNER.getName().equals(preset.getName())) {
        schema.getProperties().stream()
            .filter(Property::isOwner)
            .forEach(p -> fieldPaths.add(toFieldPath(preset.getPath(), p)));
      } else if (FieldPreset.PERSISTED.getName().equals(preset.getName())) {
        schema.getProperties().stream()
            .filter(Property::isPersisted)
            .forEach(p -> fieldPaths.add(toFieldPath(preset.getPath(), p)));
      } else if (FieldPreset.IDENTIFIABLE.getName().equals(preset.getName())) {
        schema.getProperties().stream()
            .filter(p -> FieldPreset.IDENTIFIABLE.getFields().contains(p.getName()))
            .forEach(p -> fieldPaths.add(toFieldPath(preset.getPath(), p)));
      } else if (FieldPreset.SIMPLE.getName().equals(preset.getName())) {
        schema.getProperties().stream()
            .filter(p -> p.getPropertyType().isSimple())
            .forEach(p -> fieldPaths.add(toFieldPath(preset.getPath(), p)));
      } else if (FieldPreset.NAMEABLE.getName().equals(preset.getName())) {
        schema.getProperties().stream()
            .filter(p -> FieldPreset.NAMEABLE.getFields().contains(p.getName()))
            .forEach(p -> fieldPaths.add(toFieldPath(preset.getPath(), p)));
      }
    }

    fieldPaths.forEach(fp -> fieldPathMap.putIfAbsent(fp.toFullPath(), fp));
  }

  public void visitFieldPaths(
      Object object, List<FieldPath> fieldPaths, Consumer<Object> objectConsumer) {
    if (object == null || fieldPaths.isEmpty()) {
      return;
    }

    Schema schema = schemaService.getDynamicSchema(HibernateProxyUtils.getRealClass(object));

    if (!schema.isIdentifiableObject()) {
      return;
    }

    fieldPaths.forEach(fp -> visitFieldPath(object, new ArrayList<>(fp.getPath()), objectConsumer));
  }

  private void visitFieldPath(Object object, List<String> paths, Consumer<Object> objectConsumer) {
    if (object == null) {
      return;
    }

    if (paths.isEmpty()) {
      objectConsumer.accept(object);
      return;
    }

    Schema schema = schemaService.getDynamicSchema(HibernateProxyUtils.getRealClass(object));
    String currentPath = paths.remove(0);

    Property property = schema.getProperty(currentPath);

    if (property == null) {
      return;
    }

    if (property.isCollection()) {
      Collection<?> currentObjects =
          ReflectionUtils.invokeMethod(object, property.getGetterMethod());

      for (Object o : currentObjects) {
        visitFieldPath(o, new ArrayList<>(paths), objectConsumer);
      }
    } else {
      Object currentObject = ReflectionUtils.invokeMethod(object, property.getGetterMethod());
      visitFieldPath(currentObject, new ArrayList<>(paths), objectConsumer);
    }
  }

  /** Modifies the passed in fieldPathMap by removing any matching exclusions. */
  private void applyExclusions(List<FieldPath> exclusions, Map<String, FieldPath> fieldPathMap) {
    Set<String> excludedPaths = new HashSet<>();
    for (FieldPath exclusion : exclusions) {
      excludedPaths.add(exclusion.toFullPath());

      for (String path : fieldPathMap.keySet()) {
        if (fieldEqualsRootField(path, exclusion.toFullPath())) {
          excludedPaths.add(path);
        }
      }
    }
    fieldPathMap.keySet().removeAll(excludedPaths);
  }

  // ----------------------------------------------------------------------------------------------------------------
  // Helpers
  // ----------------------------------------------------------------------------------------------------------------

  /**
   * Method that checks whether a field is equal to the root field. <br>
   * examples: <br>
   *
   * <ul>
   *   <li>fullFieldPath = "root", field = "root" -> return true
   *   <li>fullFieldPath = "root.name", field = "root" -> return true
   *   <li>fullFieldPath = "root.name", field = "name" -> return false
   *   <li>fullFieldPath = "username", field = "user" -> return false
   * </ul>
   *
   * @param fullFieldPath field path that represents the full path of a given field(in dot
   *     notation). It might look like any of the following examples: <br>
   *     <ul>
   *       <li>root
   *       <li>root.name
   *       <li>root.name.last
   *     </ul>
   *
   * @param field is the actual field which should be checked. This will only be 1 word (no dot
   *     notation)
   * @return true if the field is equal to the fullFieldPath root <br>
   */
  public static boolean fieldEqualsRootField(String fullFieldPath, String field) {
    if (ObjectUtils.anyNull(fullFieldPath, field)) return false;
    String root = fullFieldPath.split("\\.")[0];
    return root.equals(field);
  }

  private boolean isReference(Property property) {
    return property.is(PropertyType.REFERENCE) || property.itemIs(PropertyType.REFERENCE);
  }

  private boolean isComplex(Property property) {
    return property.is(PropertyType.COMPLEX)
        || property.itemIs(PropertyType.COMPLEX)
        || property.isEmbeddedObject()
        || Sharing.class.isAssignableFrom(property.getKlass())
        || Access.class.isAssignableFrom(property.getKlass())
        || UserAccess.class.isAssignableFrom(property.getKlass())
        || UserGroupAccess.class.isAssignableFrom(property.getKlass());
  }

  /** Calculates a weighted map of paths to find candidates for default expansion. */
  private Map<String, Long> calculatePathCount(Collection<FieldPath> fieldPaths) {
    Map<String, Long> pathCount = new HashMap<>();

    for (FieldPath fieldPath : fieldPaths) {
      Property property = fieldPath.getProperty();

      if (property == null) {
        continue;
      }

      List<String> paths = new ArrayList<>();

      for (String path : fieldPath.getPath()) {
        paths.add(path);
        pathCount.compute(
            StringUtils.join(paths, FieldPath.FIELD_PATH_SEPARATOR),
            (key, count) -> count == null ? 1L : count + 1L);
      }

      if (isReference(property) || isComplex(property)) {
        pathCount.compute(fieldPath.toFullPath(), (key, count) -> count == null ? 1L : count + 1L);
      }
    }

    return pathCount;
  }

  private FieldPath toFieldPath(List<String> path, Property property) {
    String name = property.isCollection() ? property.getCollectionName() : property.getName();

    FieldPath fieldPath = new FieldPath(name, path);
    fieldPath.setProperty(property);

    return fieldPath;
  }

  private Schema getSchemaByPath(List<String> paths, Class<?> klass) {
    requireNonNull(paths);
    requireNonNull(klass);

    // get root schema
    Schema schema = schemaService.getDynamicSchema(klass);
    Property currentProperty;

    for (String path : paths) {
      currentProperty = schema.getProperty(path);

      if (currentProperty == null) {
        return null; // invalid path
      }

      if (currentProperty.isCollection()) {
        schema = schemaService.getDynamicSchema(currentProperty.getItemKlass());
      } else {
        schema = schemaService.getDynamicSchema(currentProperty.getKlass());
      }
    }

    return schema;
  }
}
