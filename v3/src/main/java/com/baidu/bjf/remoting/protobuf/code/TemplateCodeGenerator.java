/*
 * Copyright 2002-2007 the original author or authors.
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
package com.baidu.bjf.remoting.protobuf.code;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.utils.ClassHelper;
import com.baidu.bjf.remoting.protobuf.utils.FieldInfo;
import com.baidu.bjf.remoting.protobuf.utils.MiniTemplator;
import com.baidu.bjf.remoting.protobuf.utils.MiniTemplator.TemplateSyntaxException;
import com.baidu.bjf.remoting.protobuf.utils.StringBuilderWriter;
import com.baidu.bjf.remoting.protobuf.utils.StringUtils;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.WireFormat;

/**
 * Code generator utility class by template engine.
 * 
 * @author xiemalin
 * @since 1.12.0
 */
public class TemplateCodeGenerator extends AbstractCodeGenerator {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateCodeGenerator.class.getCanonicalName());

    /** The Constant TEMPLATE_FILE. */
    private static final String TEMPLATE_FILE = "/jprotobuf_classes_template.tpl";

    /** The relative proxy classes. */
    private Set<Class<?>> relativeProxyClasses = new HashSet<Class<?>>();

    /** The templator. */
    private MiniTemplator templator;

    /** auto proxied suffix class name. */
    public static final String DEFAULT_SUFFIX_CLASSNAME = "$$JProtoBufClass";

    /**
     * Instantiates a new template code generator.
     *
     * @param cls the cls
     */
    public TemplateCodeGenerator(Class<?> cls) {
        super(cls);
        InputStream templateFile = cls.getResourceAsStream(TEMPLATE_FILE);
        try {
            templator = new MiniTemplator(TEMPLATE_FILE, templateFile);
        } catch (TemplateSyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Template file locate failed. " + templateFile, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.bjf.remoting.protobuf.code.ICodeGenerator#getClassName()
     */
    @Override
    public String getClassName() {
        return ClassHelper.getClassName(cls) + DEFAULT_SUFFIX_CLASSNAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.bjf.remoting.protobuf.code.ICodeGenerator#getCode()
     */
    @Override
    public String getCode() {
        String pkg = getPackage();
        if (!StringUtils.isEmpty(pkg)) {
            pkg = "package " + pkg + ";";
        }

        // set package
        templator.setVariable("package", pkg);

        // add loop block for import packages
        genImportCode();

        // generate class
        String className = getClassName();
        templator.setVariable("className", className);

        // to implements Codec interface
        templator.setVariable("codecClassName", Codec.class.getName());
        templator.setVariable("targetProxyClassName", getTargetProxyClassname());

        // define Descriptor field
        String descriptorClsName = ClassHelper.getInternalName(Descriptor.class.getCanonicalName());
        templator.setVariable("descriptorClsName", descriptorClsName);

        // create methods
        initEncodeMethodTemplateVariable();
        initDecodeMethodTemplateVariable();

        StringBuilderWriter writer = new StringBuilderWriter();
        try {
            templator.generateOutput(writer);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return writer.toString();
    }

    /**
     * Inits the encode method template variable.
     */
    protected void initEncodeMethodTemplateVariable() {

        Set<Integer> orders = new HashSet<Integer>();
        // encode method
        for (FieldInfo field : fields) {
            boolean isList = field.isList();
            boolean isMap = field.isMap();
            // check type
            if (!isList) {
                checkType(field.getFieldType(), field.getField());
            }

            if (orders.contains(field.getOrder())) {
                throw new IllegalArgumentException("Field order '" + field.getOrder() + "' on field"
                        + field.getField().getName() + " already exsit.");
            }
            // define field

            FieldType fieldType = field.getFieldType();
            String accessByField = getAccessByField("t", field.getField(), cls);

            String fieldName = CodedConstant.getFieldName(field.getOrder());
            String encodeFieldType = CodedConstant.getFiledType(fieldType, isList);
            templator.setVariable("encodeFieldType", encodeFieldType);
            templator.setVariable("encodeFieldName", fieldName);
            templator.setVariable("encodeFieldGetter", accessByField);
            String writeValueToField = CodedConstant.getWriteValueToField(fieldType, accessByField, isList);
            templator.setVariable("writeValueToField", writeValueToField);

            if (field.isRequired()) {
                templator.setVariableOpt("checkNull",
                        CodedConstant.getRequiredCheck(field.getOrder(), field.getField()));
            } else {
                templator.setVariable("checkNull", "");
            }

            String calcSize = CodedConstant.getMappedTypeSize(field, field.getOrder(), field.getFieldType(), isList,
                    isMap, debug, outputPath);
            templator.setVariable("calcSize", calcSize);

            // set write to byte
            String encodeWriteFieldValue = CodedConstant.getMappedWriteCode(field, "output", field.getOrder(),
                    field.getFieldType(), isList, isMap);
            templator.setVariable("encodeWriteFieldValue", encodeWriteFieldValue);
            templator.addBlock("encodeFields");

        }
    }

    protected void initDecodeMethodTemplateVariable() {
        // 执行初始化，主要针对枚举类型
        for (FieldInfo field : fields) {
            boolean isList = field.isList();
            if (field.getFieldType() == FieldType.ENUM) {
                String clsName = ClassHelper.getInternalName(field.getField().getType().getCanonicalName());
                if (!isList) {
                    String express =
                            "CodedConstant.getEnumValue(" + clsName + ".class, " + clsName + ".values()[0].name())";
                    // add set get method
                    String setToField =
                            getSetToField("ret", field.getField(), cls, express, isList, field.isMap(), false);
                    templator.setVariable("enumInitialize", setToField);
                    templator.addBlock("enumFields");
                }
            }
        }

        StringBuilder code = new StringBuilder();
        // 处理field解析
        for (FieldInfo field : fields) {
            boolean isList = field.isList();
            String t = field.getFieldType().getType();
            t = CodedConstant.capitalize(t);

            boolean listTypeCheck = false;
            String express;
            String objectDecodeExpress = "";
            String objectDecodeExpressSuffix = "";

            String decodeOrder = "-1";
            if (field.getFieldType() != FieldType.DEFAULT) {
                decodeOrder = CodedConstant.makeTag(field.getOrder(),
                        field.getFieldType().getInternalFieldType().getWireType()) + "";
            } else {
                decodeOrder = "CodedConstant.makeTag(" + field.getOrder() + ",WireFormat."
                        + field.getFieldType().getWireFormat() + ")";
            }
            templator.setVariable("decodeOrder", decodeOrder);

            // enumeration type
            if (field.getFieldType() == FieldType.ENUM) {
                String clsName = ClassHelper.getInternalName(field.getField().getType().getCanonicalName());
                if (isList) {
                    if (field.getGenericKeyType() != null) {
                        Class cls = field.getGenericKeyType();
                        clsName = ClassHelper.getInternalName(cls.getCanonicalName());
                    }
                }
                express = "CodedConstant.getEnumValue(" + clsName + ".class, CodedConstant.getEnumName(" + clsName
                        + ".values()," + "input.read" + t + "()))";
            } else if (field.getFieldType() == FieldType.BIG_DECIMAL) {
                express = "CodedConstant.getBigDecimal(" + "input.read" + t + "()" + ")";
            } else if (field.getFieldType() == FieldType.DATE) {
                express = "CodedConstant.getDate(" + "input.read" + t + "()" + ")";
            } else {
                express = "input.read" + t + "()";
            }

            // if List type and element is object message type
            if (isList && field.getFieldType() == FieldType.OBJECT) {
                if (field.getGenericKeyType() != null) {
                    Class cls = field.getGenericKeyType();

                    String name = ClassHelper.getInternalName(cls.getCanonicalName()); // need
                    // to
                    // parse
                    // nested
                    // class
                    code.append("codec = ProtobufProxy.create(").append(name).append(".class");
                    if (debug) {
                        code.append(", true");
                    } else {
                        code.append(", false");
                    }

                    String spath = "ProtobufProxy.OUTPUT_PATH.get()";
                    code.append(",").append(spath);
                    code.append(")").append(ClassCode.JAVA_LINE_BREAK);
                    objectDecodeExpress = code.toString();
                    code.setLength(0);

                    objectDecodeExpress += "int length = input.readRawVarint32()" + ClassCode.JAVA_LINE_BREAK;
                    objectDecodeExpress += "final int oldLimit = input.pushLimit(length)" + ClassCode.JAVA_LINE_BREAK;
                    listTypeCheck = true;
                    express = "(" + name + ") codec.readFrom(input)";

                }
            } else if (field.isMap()) {

                String getMapCommand = getMapCommand(field);

                if (field.isEnumValueType()) {
                    String enumClassName = field.getGenericeValueType().getCanonicalName();
                    code.append("EnumHandler<").append(enumClassName).append("> handler");
                    code.append("= new EnumHandler");
                    code.append("<").append(enumClassName).append(">() {");
                    code.append(ClassCode.LINE_BREAK);
                    code.append("public ").append(enumClassName).append(" handle(int value) {");
                    code.append(ClassCode.LINE_BREAK);
                    code.append("String enumName = CodedConstant.getEnumName(").append(enumClassName)
                            .append(".values(), value)");
                    code.append(ClassCode.JAVA_LINE_BREAK);
                    code.append("return ").append(enumClassName).append(".valueOf(enumName)");
                    code.append(ClassCode.JAVA_LINE_BREAK);
                    code.append("}}");
                    code.append(ClassCode.JAVA_LINE_BREAK);

                }
                objectDecodeExpress = code.toString();
                code.setLength(0);

                express = "CodedConstant.putMapValue(input, " + getMapCommand + ",";
                express += CodedConstant.getMapFieldGenericParameterString(field);
                if (field.isEnumValueType()) {
                    express += ", handler";
                }
                express += ")";

            } else if (field.getFieldType() == FieldType.OBJECT) { // if object
                // message
                // type
                Class cls = field.getField().getType();
                String name = ClassHelper.getInternalName(cls.getCanonicalName()); // need
                // to
                // parse
                // nested
                // class
                code.append("codec = ProtobufProxy.create(").append(name).append(".class");
                if (debug) {
                    code.append(", true");
                } else {
                    code.append(", false");
                }

                String spath = "ProtobufProxy.OUTPUT_PATH.get()";
                code.append(",").append(spath);
                code.append(")").append(ClassCode.JAVA_LINE_BREAK);
                objectDecodeExpress = code.toString();
                code.setLength(0);

                objectDecodeExpress += "int length = input.readRawVarint32()" + ClassCode.JAVA_LINE_BREAK;
                objectDecodeExpress += "final int oldLimit = input.pushLimit(length)" + ClassCode.JAVA_LINE_BREAK;

                listTypeCheck = true;
                express = "(" + name + ") codec.readFrom(input)";
            }

            if (field.getFieldType() == FieldType.BYTES) {
                express += ".toByteArray()";
            }

            String decodeFieldSetValue =
                    getSetToField("ret", field.getField(), cls, express, isList, field.isMap(), false)
                            + ClassCode.JAVA_LINE_BREAK;

            if (listTypeCheck) {
                objectDecodeExpressSuffix += "input.checkLastTagWas(0)" + ClassCode.JAVA_LINE_BREAK;
                objectDecodeExpressSuffix += "input.popLimit(oldLimit)" + ClassCode.JAVA_LINE_BREAK;
            }

            if (field.isRequired()) {
                templator.setVariable("deocdeCheckNull", CodedConstant
                        .getRetRequiredCheck(getAccessByField("ret", field.getField(), cls), field.getField()));
            } else {
                templator.setVariable("deocdeCheckNull", "");
            }

            String objectPackedDecodeExpress = "";
            // read packed type
            if (isList) {
                FieldType fieldType = field.getFieldType();
                if (fieldType.isPrimitive() || fieldType.isEnum()) {
                    code.append("if (tag == ")
                            .append(CodedConstant.makeTag(field.getOrder(), WireFormat.WIRETYPE_LENGTH_DELIMITED));
                    code.append(") {").append(ClassCode.LINE_BREAK);

                    code.append("int length = input.readRawVarint32()").append(ClassCode.JAVA_LINE_BREAK);
                    code.append("int limit = input.pushLimit(length)").append(ClassCode.JAVA_LINE_BREAK);

                    code.append(getSetToField("ret", field.getField(), cls, express, isList, field.isMap(), true));

                    code.append("input.popLimit(limit)").append(ClassCode.JAVA_LINE_BREAK);

                    code.append("continue").append(ClassCode.JAVA_LINE_BREAK);
                    code.append("}").append(ClassCode.LINE_BREAK);

                    objectPackedDecodeExpress = code.toString();
                }
            }
            templator.setVariable("objectPackedDecodeExpress", objectPackedDecodeExpress);
            templator.setVariable("objectDecodeExpress", objectDecodeExpress);
            templator.setVariable("objectDecodeExpressSuffix", objectDecodeExpressSuffix);
            templator.setVariable("decodeFieldSetValue", decodeFieldSetValue);
            templator.addBlock("decodeFields");
        }
    }

    /**
     * @param field
     * @return
     */
    private String getMapCommand(FieldInfo field) {
        String keyGeneric;
        keyGeneric = field.getGenericKeyType().getCanonicalName();

        String valueGeneric;
        valueGeneric = field.getGenericeValueType().getCanonicalName();
        String getMapCommand = "(Map<" + keyGeneric;
        getMapCommand = getMapCommand + ", " + valueGeneric + ">)";
        getMapCommand = getMapCommand + getAccessByField("ret", field.getField(), cls);
        return getMapCommand;
    }

    /**
     * Gen import code.
     */
    private void genImportCode() {
        Set<String> imports = new HashSet<String>();
        imports.add("java.util.*");
        imports.add("java.math.BigDecimal");
        imports.add("java.io.IOException");
        imports.add("java.lang.reflect.*");
        imports.add("com.baidu.bjf.remoting.protobuf.code.*");
        imports.add("com.baidu.bjf.remoting.protobuf.utils.*");
        imports.add("com.baidu.bjf.remoting.protobuf.*");
        imports.add("com.google.protobuf.*");

        if (!StringUtils.isEmpty(getPackage())) {
            imports.add(getTargetProxyClassname());
        }

        for (String pkg : imports) {
            templator.setVariable("importPackage", pkg);
            templator.addBlock("imports");
        }
    }

    /**
     * Gets the relative proxy classes.
     *
     * @return the relative proxy classes
     */
    public Set<Class<?>> getRelativeProxyClasses() {
        return relativeProxyClasses;
    }

    /**
     * generate access {@link Field} value source code. support public field access, getter method access and reflection
     * access.
     * 
     * @param target
     * @param field
     * @param cls
     * @param express
     * @param isList
     * @return
     */
    protected String getSetToField(String target, Field field, Class<?> cls, String express, boolean isList,
            boolean isMap, boolean packed) {
        StringBuilder ret = new StringBuilder();
        if (isList || isMap) {
            ret.append("if ((").append(getAccessByField(target, field, cls)).append(") == null) {")
                    .append(ClassCode.LINE_BREAK);
        }
        // if field of public modifier we can access directly
        if (Modifier.isPublic(field.getModifiers())) {
            if (isList) {
                // should initialize list
                ret.append(target).append(ClassHelper.PACKAGE_SEPARATOR).append(field.getName())
                        .append("= new ArrayList()").append(ClassCode.JAVA_LINE_BREAK).append("}")
                        .append(ClassCode.LINE_BREAK);
                if (express != null) {
                    if (packed) {
                        ret.append("while (input.getBytesUntilLimit() > 0) {").append(ClassCode.LINE_BREAK);
                    }
                    ret.append(target).append(ClassHelper.PACKAGE_SEPARATOR).append(field.getName()).append(".add(")
                            .append(express).append(")");
                    if (packed) {
                        ret.append(";}").append(ClassCode.LINE_BREAK);
                    }
                }
                return ret.toString();
            } else if (isMap) {
                ret.append(target).append(ClassHelper.PACKAGE_SEPARATOR).append(field.getName())
                        .append("= new HashMap()").append(ClassCode.JAVA_LINE_BREAK).append("}")
                        .append(ClassCode.LINE_BREAK);
                return ret.append(express).toString();
            }
            return target + ClassHelper.PACKAGE_SEPARATOR + field.getName() + "=" + express + ClassCode.LINE_BREAK;
        }
        String setter = "set" + CodedConstant.capitalize(field.getName());
        // check method exist
        try {
            cls.getMethod(setter, new Class<?>[] { field.getType() });
            if (isList) {
                ret.append("List __list = new ArrayList()").append(ClassCode.JAVA_LINE_BREAK);
                ret.append(target).append(ClassHelper.PACKAGE_SEPARATOR).append(setter).append("(__list)")
                        .append(ClassCode.JAVA_LINE_BREAK).append("}").append(ClassCode.LINE_BREAK);

                if (express != null) {
                    if (packed) {
                        ret.append("while (input.getBytesUntilLimit() > 0) {").append(ClassCode.LINE_BREAK);
                    }
                    ret.append("(").append(getAccessByField(target, field, cls)).append(").add(").append(express)
                            .append(")");
                    if (packed) {
                        ret.append(";}").append(ClassCode.LINE_BREAK);
                    }
                }
                return ret.toString();
            } else if (isMap) {
                ret.append("Map __map = new HashMap()").append(ClassCode.JAVA_LINE_BREAK);
                ret.append(target).append(ClassHelper.PACKAGE_SEPARATOR).append(setter).append("(__map)")
                        .append(ClassCode.JAVA_LINE_BREAK).append("}").append(ClassCode.LINE_BREAK);
                return ret + express;
            }
            return target + ClassHelper.PACKAGE_SEPARATOR + setter + "(" + express + ")\n";
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            }
        }

        if (isList) {
            ret.append("List __list = new ArrayList()").append(ClassCode.JAVA_LINE_BREAK);
            ret.append("FieldUtils.setField(").append(target).append(", \"").append(field.getName())
                    .append("\", __list)").append(ClassCode.JAVA_LINE_BREAK).append("}").append(ClassCode.LINE_BREAK);
            if (express != null) {
                if (packed) {
                    ret.append("while (input.getBytesUntilLimit() > 0) {").append(ClassCode.LINE_BREAK);
                }
                ret.append("(").append(getAccessByField(target, field, cls)).append(").add(").append(express)
                        .append(")");
                if (packed) {
                    ret.append(";}").append(ClassCode.LINE_BREAK);
                }
            }
            return ret.toString();
        } else if (isMap) {
            ret.append("Map __map = new HashMap()").append(ClassCode.JAVA_LINE_BREAK);
            ret.append("FieldUtils.setField(").append(target).append(", \"").append(field.getName())
                    .append("\", __map)").append(ClassCode.JAVA_LINE_BREAK).append("}").append(ClassCode.LINE_BREAK);
            return ret + express;
        }

        // use reflection to get value
        String code = "";
        if (express != null) {
            code = "FieldUtils.setField(" + target + ", \"" + field.getName() + "\", " + express + ")"
                    + ClassCode.LINE_BREAK;
        }
        return code;
    }

}
