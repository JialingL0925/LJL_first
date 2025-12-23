package com.example.demo.model.dto;

import com.example.demo.model.entity.SalesOrder;
import com.example.demo.model.entity.SalesOrderLine;
import lombok.Data;
import java.util.List;

@Data
public class SalesOrderDTO {
    private SalesOrder order;
    private List<SalesOrderLine> lines;
}