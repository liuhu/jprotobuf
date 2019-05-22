package com.baidu.bjf.remoting.protobuf.bigdecimal;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: LiuHu
 * @create: 2019-05-17 18:12
 **/
public class BigDecimalTypeClassTest {


    /**
     * 单结构
     */
    public static class SingleBigDecimalPOJOClass {

        /** The BigDecimal. */
        @Protobuf(fieldType = FieldType.BIG_DECIMAL, order = 1, required = true)
        private BigDecimal num;

        public BigDecimal getNum() {
            return num;
        }

        public void setNum(BigDecimal num) {
            this.num = num;
        }
    }

    /**
     * List 结构
     */
    public static class ListBigDecimalPOJOClass {

        /** The BigDecimal. */
        @Protobuf(fieldType = FieldType.BIG_DECIMAL, order = 1, required = true)
        private List<BigDecimal> nums;

        public List<BigDecimal> getNums() {
            return nums;
        }

        public void setNums(List<BigDecimal> nums) {
            this.nums = nums;
        }
    }

    /**
     * Map 结构
     */
    public static class MapDecimalPOJOClass {

        @Protobuf(fieldType = FieldType.MAP, order = 1, required = true)
        private Map<Long, BigDecimal> decimalMap;

        public Map<Long, BigDecimal> getDecimalMap() {
            return decimalMap;
        }

        public void setDecimalMap(Map<Long, BigDecimal> decimalMap) {
            this.decimalMap = decimalMap;
        }
    }

    /**
     * 内部类
     */
    public static class InternalDecimalPOJOClass {

        @Protobuf(fieldType = FieldType.OBJECT, order = 1, required = true)
        private SingleBigDecimalPOJOClass singleBigDecimalPOJOClass;

        @Protobuf(fieldType = FieldType.OBJECT, order = 2, required = true)
        private ListBigDecimalPOJOClass listBigDecimalPOJOClass;

        @Protobuf(fieldType = FieldType.OBJECT, order = 3, required = true)
        private MapDecimalPOJOClass mapDecimalPOJOClass;

        public SingleBigDecimalPOJOClass getSingleBigDecimalPOJOClass() {
            return singleBigDecimalPOJOClass;
        }

        public void setSingleBigDecimalPOJOClass(SingleBigDecimalPOJOClass singleBigDecimalPOJOClass) {
            this.singleBigDecimalPOJOClass = singleBigDecimalPOJOClass;
        }

        public ListBigDecimalPOJOClass getListBigDecimalPOJOClass() {
            return listBigDecimalPOJOClass;
        }

        public void setListBigDecimalPOJOClass(ListBigDecimalPOJOClass listBigDecimalPOJOClass) {
            this.listBigDecimalPOJOClass = listBigDecimalPOJOClass;
        }

        public MapDecimalPOJOClass getMapDecimalPOJOClass() {
            return mapDecimalPOJOClass;
        }

        public void setMapDecimalPOJOClass(MapDecimalPOJOClass mapDecimalPOJOClass) {
            this.mapDecimalPOJOClass = mapDecimalPOJOClass;
        }
    }

    /**
     * Test encode decode.
     */
    @Test
    public void testSingleEncodeDecode() {

        BigDecimal num1 = new BigDecimal("123");
        BigDecimal num2 = new BigDecimal("8733.37462938476239842634829346234");


        SingleBigDecimalPOJOClass pojo1 = new SingleBigDecimalPOJOClass();
        pojo1.setNum(num1);

        SingleBigDecimalPOJOClass pojo2 = new SingleBigDecimalPOJOClass();
        pojo2.setNum(num2);

        Codec<SingleBigDecimalPOJOClass> codec = ProtobufProxy.create(SingleBigDecimalPOJOClass.class);
        try {
            byte[] b1 = codec.encode(pojo1);
            byte[] b2 = codec.encode(pojo2);

            SingleBigDecimalPOJOClass newPojo1 = codec.decode(b1);
            SingleBigDecimalPOJOClass newPojo2 = codec.decode(b2);

            System.out.println("Original num = " + num1 + ", Decode num = " + newPojo1.getNum());
            Assert.assertEquals(num1, newPojo1.getNum());

            System.out.println("Original num = " + num2 + ", Decode num = " + newPojo2.getNum());
            Assert.assertEquals(num2, newPojo2.getNum());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Test encode decode.
     */
    @Test
    public void testListEncodeDecode() {

        BigDecimal num1 = new BigDecimal("123");
        BigDecimal num2 = new BigDecimal("8733.37462938476239842634829346234");

        List<BigDecimal> nums = Arrays.asList(num1, num2);

        ListBigDecimalPOJOClass pojo = new ListBigDecimalPOJOClass();
        pojo.setNums(nums);


        Codec<ListBigDecimalPOJOClass> codec = ProtobufProxy.create(ListBigDecimalPOJOClass.class);
        try {
            byte[] b = codec.encode(pojo);
            ListBigDecimalPOJOClass newPojo = codec.decode(b);
            System.out.println("Original nums = " + nums + ", Decode nums = " + newPojo.getNums());
            BigDecimal newNum1 = newPojo.getNums().get(0);
            BigDecimal newNum2 = newPojo.getNums().get(1);
            System.out.println("num1 = " + newNum1 + " , num2 = " + newNum2);
            Assert.assertEquals(nums, newPojo.getNums());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMapEncodeDecode() {

        Map<Long, BigDecimal> decimalMap = new HashMap<>();
        decimalMap.put(0L, new BigDecimal("0.1823478196234987269384"));
        decimalMap.put(1L, new BigDecimal("1234234234.1"));

        MapDecimalPOJOClass pojo = new MapDecimalPOJOClass();
        pojo.setDecimalMap(decimalMap);


        Codec<MapDecimalPOJOClass> codec = ProtobufProxy.create(MapDecimalPOJOClass.class, false);
        try {
            byte[] b = codec.encode(pojo);
            MapDecimalPOJOClass newPojo = codec.decode(b);
            System.out.println("Original decimalMap = " + decimalMap + ", Decode decimalMap = " + newPojo.getDecimalMap());
            BigDecimal aa = newPojo.getDecimalMap().get(0L);
            System.out.println(aa);
            decimalMap.forEach((k, v) -> {
                System.out.println("Original value = " + v + ", Decode value = " + newPojo.getDecimalMap().get(k));
                Assert.assertEquals(v, newPojo.getDecimalMap().get(k));
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInternalEncodeDecode() {

        BigDecimal num1 = new BigDecimal("123");
        BigDecimal num2 = new BigDecimal("8733.37462938476239842634829346234");

        SingleBigDecimalPOJOClass singleBigDecimalPOJOClass = new SingleBigDecimalPOJOClass();
        singleBigDecimalPOJOClass.setNum(num1);

        Map<Long, BigDecimal> decimalMap = new HashMap<>();
        decimalMap.put(0L, num1);
        decimalMap.put(1L, num2);
        MapDecimalPOJOClass mapDecimalPOJOClass = new MapDecimalPOJOClass();
        mapDecimalPOJOClass.setDecimalMap(decimalMap);

        ListBigDecimalPOJOClass listBigDecimalPOJOClass = new ListBigDecimalPOJOClass();
        listBigDecimalPOJOClass.setNums(Arrays.asList(num1, num2));


        InternalDecimalPOJOClass internalDecimalPOJOClass = new InternalDecimalPOJOClass();
        internalDecimalPOJOClass.setSingleBigDecimalPOJOClass(singleBigDecimalPOJOClass);
        internalDecimalPOJOClass.setListBigDecimalPOJOClass(listBigDecimalPOJOClass);
        internalDecimalPOJOClass.setMapDecimalPOJOClass(mapDecimalPOJOClass);

        Codec<InternalDecimalPOJOClass> codec = ProtobufProxy.create(InternalDecimalPOJOClass.class, false);
        try {
            byte[] b = codec.encode(internalDecimalPOJOClass);
            InternalDecimalPOJOClass newPojo = codec.decode(b);
            System.out.println("Original value = " + singleBigDecimalPOJOClass.getNum() + ", Decode value = " + newPojo.getSingleBigDecimalPOJOClass().getNum());
            Assert.assertEquals(singleBigDecimalPOJOClass.getNum(), newPojo.getSingleBigDecimalPOJOClass().getNum());

            System.out.println("Original value = " + listBigDecimalPOJOClass.getNums() + ", Decode value = " + newPojo.getListBigDecimalPOJOClass().getNums());
            Assert.assertEquals(listBigDecimalPOJOClass.getNums(), newPojo.getListBigDecimalPOJOClass().getNums());

            System.out.println("Original value = " + mapDecimalPOJOClass.getDecimalMap() + ", Decode value = " + newPojo.getMapDecimalPOJOClass().getDecimalMap());
            Assert.assertEquals(mapDecimalPOJOClass.getDecimalMap(), newPojo.getMapDecimalPOJOClass().getDecimalMap());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
