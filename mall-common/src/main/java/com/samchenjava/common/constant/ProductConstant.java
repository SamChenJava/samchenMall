package com.samchenjava.common.constant;

public class ProductConstant {

    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "base attr"), ATTR_TYPE_SALE(0, "sale attr");
        private int code;
        private String msg;

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum shelfStatusEnum {
        NEW_SPU(0, "new"), SPU_ON_SHELF(1, "on shelf"), SPU_OFF_SHELF(2, "off shelf");
        private int code;
        private String msg;

        shelfStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
