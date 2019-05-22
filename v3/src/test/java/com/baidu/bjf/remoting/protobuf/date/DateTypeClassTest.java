package com.baidu.bjf.remoting.protobuf.date;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * @description:
 * @author: LiuHu
 * @create: 2019-05-22 11:34
 **/
public class DateTypeClassTest {

    /**
     * 单结构
     */
    public static class SingleDatePOJOClass {

        /** The BigDecimal. */
        @Protobuf(fieldType = FieldType.DATE, order = 1, required = true)
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    /**
     * List 结构
     */
    public static class ListDatePOJOClass {

        /** The BigDecimal. */
        @Protobuf(fieldType = FieldType.DATE, order = 1, required = true)
        private List<Date> dates;

        public List<Date> getDates() {
            return dates;
        }

        public void setDates(List<Date> dates) {
            this.dates = dates;
        }
    }


    /**
     * Map 结构
     */
    public static class MapDatePOJOClass {

        @Protobuf(fieldType = FieldType.MAP, order = 1, required = true)
        private Map<Long, Date> dateMap;

        public Map<Long, Date> getDateMap() {
            return dateMap;
        }

        public void setDateMap(Map<Long, Date> dateMap) {
            this.dateMap = dateMap;
        }
    }

    /**
     * Test encode decode.
     */
    @Test
    public void testSingleEncodeDecode() {

        Date date1 = new Date();
        Date date2 = new Date(1295668192000L);

        SingleDatePOJOClass pojo1 = new SingleDatePOJOClass();
        pojo1.setDate(date1);

        SingleDatePOJOClass pojo2 = new SingleDatePOJOClass();
        pojo2.setDate(date2);

        Codec<SingleDatePOJOClass> codec = ProtobufProxy.create(SingleDatePOJOClass.class);
        try {
            byte[] b1 = codec.encode(pojo1);
            byte[] b2 = codec.encode(pojo2);

            SingleDatePOJOClass newPojo1 = codec.decode(b1);
            SingleDatePOJOClass newPojo2 = codec.decode(b2);

            System.out.println("Original date = " + date1 + ", Decode date = " + newPojo1.getDate());
            Assert.assertEquals(date1, newPojo1.getDate());

            System.out.println("Original date = " + date2 + ", Decode date = " + newPojo2.getDate());
            Assert.assertEquals(date2, newPojo2.getDate());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test encode decode.
     */
    @Test
    public void testListEncodeDecode() {

        Date date1 = new Date();
        Date date2 = new Date(1295668192000L);


        List<Date> dates = Arrays.asList(date1, date2);

        ListDatePOJOClass pojo = new ListDatePOJOClass();
        pojo.setDates(dates);


        Codec<ListDatePOJOClass> codec = ProtobufProxy.create(ListDatePOJOClass.class);
        try {
            byte[] b = codec.encode(pojo);
            ListDatePOJOClass newPojo = codec.decode(b);

            System.out.println("Original dates = " + dates + ", Decode nums = " + newPojo.getDates());
            Date newDate1 = newPojo.getDates().get(0);
            Date newDate2 = newPojo.getDates().get(1);
            System.out.println("newDate1 = " + newDate1 + " , newDate1 = " + newDate2);
            Assert.assertEquals(dates, newPojo.getDates());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testMapEncodeDecode() {

        Date date1 = new Date();
        Date date2 = new Date(1295668192000L);

        Map<Long, Date> dateMap = new HashMap<>();
        dateMap.put(0L, date1);
        dateMap.put(1L, date2);

        MapDatePOJOClass pojo = new MapDatePOJOClass();
        pojo.setDateMap(dateMap);


        Codec<MapDatePOJOClass> codec = ProtobufProxy.create(MapDatePOJOClass.class, true);
        try {
            byte[] b = codec.encode(pojo);
            MapDatePOJOClass newPojo = codec.decode(b);

            System.out.println("Original dateMap = " + dateMap + ", Decode dateMap = " + newPojo.getDateMap());
            Date aa = newPojo.getDateMap().get(0L);
            System.out.println(aa);
            dateMap.forEach((k, v) -> {
                System.out.println("Original value = " + v + ", Decode value = " + newPojo.getDateMap().get(k));
                Assert.assertEquals(v, newPojo.getDateMap().get(k));
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
