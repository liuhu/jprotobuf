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
import java.util.List;

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
            Assert.assertEquals(nums, newPojo.getNums());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
