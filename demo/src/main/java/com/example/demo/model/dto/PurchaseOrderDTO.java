package com.example.demo.model.dto;

import com.example.demo.model.entity.PurchaseOrder;
import com.example.demo.model.entity.PurchaseOrderLine;
import lombok.Data;
import java.util.List;

@Data
public class PurchaseOrderDTO {
    private PurchaseOrder order;
    private List<PurchaseOrderLine> lines;
}