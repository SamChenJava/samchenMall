package com.samchenjava.mall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**n.238
 * shopping cart
 *   for those attributes that need to be calculated, I need to rewrite getter, inorder to
 * guarantee they will be recalculate each time
 */
public class CartVo {
    List<CartItemVo> items;
    private Integer countNum;   //total quantity
    private Integer countType;  //quantity of types of products
    private BigDecimal totalAmount;    //total price in all shopping cart
    private BigDecimal reduce = new BigDecimal("0.00");  //credit

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItemVo item : items) {
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0.00");
        //1 calculate total price
        if(items!=null && items.size()>0){
            for (CartItemVo item : items) {
                if(item.getCheck()){
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        //2 subtract credit
        BigDecimal subtract = amount.subtract(getReduce());
        return subtract;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
