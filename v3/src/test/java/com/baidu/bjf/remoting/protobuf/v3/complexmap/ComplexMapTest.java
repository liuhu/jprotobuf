/**
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Baidu company (the "License");
 * you may not use this file except in compliance with the License.
 *
 */
package com.baidu.bjf.remoting.protobuf.v3.complexmap;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import org.junit.Assert;
import org.junit.Test;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.baidu.bjf.remoting.protobuf.v3.complexmap.AddressBookProtos.Person;
import com.baidu.bjf.remoting.protobuf.v3.complexmap.AddressBookProtos.Person.PhoneType;

/**
 * Complex map usage test for encode/decode
 * 
 * @author xiemalin
 * @since 2.0.0
 */
public class ComplexMapTest {

    @Test
    public void testOriginalMap() {
        java.util.Map<java.lang.String, AddressBookProtos.Person.PhoneNumber> personMap;
        personMap = new HashMap<java.lang.String, AddressBookProtos.Person.PhoneNumber>();

        personMap.put("a", AddressBookProtos.Person.PhoneNumber.newBuilder().setNumber("10000000000")
                .setType(PhoneType.HOME).build());

        java.util.Map<java.lang.String, AddressBookProtos.Person.PhoneType> personTypeMap;
        personTypeMap = new HashMap<java.lang.String, AddressBookProtos.Person.PhoneType>();

        personTypeMap.put("key1", AddressBookProtos.Person.PhoneType.MOBILE);

        Person person = AddressBookProtos.Person.newBuilder().putAllPhoneNumberObjectValueMap(personMap)
                .setName("xiemalin").putAllPhoneTypeEnumValueMap(personTypeMap).build();
        
        byte[] bytes = person.toByteArray();
        System.out.println(Arrays.toString(bytes));
        
        Codec<ComplexMapPOJO> complexMapPOJOCodec = ProtobufProxy.create(ComplexMapPOJO.class, false);
        
        try {
            ComplexMapPOJO decode = complexMapPOJOCodec.decode(bytes);
            Assert.assertEquals("xiemalin", decode.name);
            Assert.assertEquals(1, decode.phoneNumberObjectValueMap.size());
            Assert.assertEquals("10000000000", decode.phoneNumberObjectValueMap.get("a").number);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    @Test
    public void testPOJOEncode() throws IOException {

        Codec<ComplexMapPOJO> complexMapPOJOCodec = ProtobufProxy.create(ComplexMapPOJO.class, false);

        // initialize POJO value
        ComplexMapPOJO pojo = new ComplexMapPOJO();

        pojo.name = "xiemalin";

        pojo.phoneTypeEnumValueMap = new HashMap<String, PhoneTypeEnumPOJO>();
        pojo.phoneTypeEnumValueMap.put("a", PhoneTypeEnumPOJO.HOME);
        pojo.phoneTypeEnumValueMap.put("b", PhoneTypeEnumPOJO.MOBILE);
        pojo.phoneTypeEnumValueMap.put("c", PhoneTypeEnumPOJO.WORK);

        pojo.phoneNumberObjectValueMap = new HashMap<String, PhoneNumberPOJO>();

        PhoneNumberPOJO phoneNumberPOJO = new PhoneNumberPOJO();
        phoneNumberPOJO.number = "10000000000";
        phoneNumberPOJO.type = PhoneTypeEnumPOJO.MOBILE;

        pojo.phoneNumberObjectValueMap.put("key1", phoneNumberPOJO);

        byte[] bytes = complexMapPOJOCodec.encode(pojo);

        Person person = AddressBookProtos.Person.parseFrom(bytes);
        Assert.assertEquals(pojo.name, person.getName());

        Assert.assertEquals(pojo.phoneTypeEnumValueMap.size(), person.getPhoneTypeEnumValueMap().size());
        Assert.assertEquals(pojo.phoneTypeEnumValueMap.get("a").name(),
                person.getPhoneTypeEnumValueMap().get("a").name());

        Assert.assertEquals(pojo.phoneNumberObjectValueMap.size(), person.getPhoneNumberObjectValueMap().size());

        Assert.assertEquals(pojo.phoneNumberObjectValueMap.get("key1").number,
                person.getPhoneNumberObjectValueMap().get("key1").getNumber());
    }

    /**
     * Map 结构
     */
    public static class MapListPOJOClass {

        @Protobuf(fieldType = FieldType.MAP, order = 1, required = true)
        private Map<Long, List<String>> listMap;

        public Map<Long, List<String>> getListMap() {
            return listMap;
        }

        public void setListMap(Map<Long, List<String>> listMap) {
            this.listMap = listMap;
        }
    }

    /**
     * Test encode decode.
     * 不支持 MAP中嵌套List
     */
    @Test
    public void testMapEncodeDecode() {

        Map<Long, List<String>> listMap = new HashMap<>();
        listMap.put(1L, Arrays.asList("1", "11"));

        MapListPOJOClass pojo = new MapListPOJOClass();
        pojo.setListMap(listMap);


        Codec<MapListPOJOClass> codec = ProtobufProxy.create(MapListPOJOClass.class);
        try {
            byte[] b = codec.encode(pojo);
            MapListPOJOClass newPojo = codec.decode(b);
            System.out.println("Original listMap = " + listMap + ", Decode listMap = " + newPojo.getListMap());
            Assert.assertEquals(listMap, newPojo.getListMap());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
