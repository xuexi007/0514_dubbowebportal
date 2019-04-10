package com.offcn.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVo {
private String id;
private String productname;
private Double price;
private Integer amount;
private Double total;
private String statu;

}
