package infrastructure.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.dto.*;
import core.entities.*;

public class Mapper {
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <S, T> T map(S source, Class<T> target){
        return objectMapper.convertValue(source, target);
    }

    public static InvoiceDto map(Invoice invoice) {
        InvoiceDto invoiceDto = map(invoice, InvoiceDto.class);

        if (invoice.getCreator() != null) {
            invoiceDto.setCreatorId(invoice.getCreator().getId());
            invoiceDto.setCreatorFullName(invoice.getCreator().getFullName());
        }
        if (invoice.getShift() != null)
            invoiceDto.setShiftId(invoice.getShift().getId());
        if (invoice.getCustomer() != null) {
            invoiceDto.setCustomerId(invoice.getCustomer().getId());
            invoiceDto.setCustomerPhoneNumber(invoice.getCustomer().getPhoneNumber());
        }
        if (invoice.getPromotion() != null) {
            invoiceDto.setPromotionId(invoice.getPromotion().getId());
            invoiceDto.setPromotionName(invoice.getPromotion().getName());
        }
        if (invoice.getReferencedInvoice() != null)
            invoiceDto.setReferencedInvoiceId(invoice.getReferencedInvoice().getId());
        if (invoice.getParentInvoice() != null)
            invoiceDto.setParentInvoiceId(invoice.getParentInvoice().getId());

        return invoiceDto;
    }

    public static Invoice map(InvoiceDto invoiceDto) {
        Invoice invoice = map(invoiceDto, Invoice.class);

        if (invoiceDto.getCreatorId() != null)
            invoice.setCreator(Staff.builder().id(invoiceDto.getCreatorId()).build());
        if (invoiceDto.getShiftId() != null)
            invoice.setShift(Shift.builder().id(invoiceDto.getShiftId()).build());
        if (invoiceDto.getCustomerId() != null)
            invoice.setCustomer(Customer.builder().id(invoiceDto.getCustomerId()).build());
        if (invoiceDto.getPromotionId() != null)
            invoice.setPromotion(Promotion.builder().id(invoiceDto.getPromotionId()).build());
        if (invoiceDto.getReferencedInvoiceId() != null)
            invoice.setReferencedInvoice(Invoice.builder().id(invoiceDto.getReferencedInvoiceId()).build());
        if (invoiceDto.getParentInvoiceId() != null)
            invoice.setParentInvoice(Invoice.builder().id(invoiceDto.getParentInvoiceId()).build());

        return invoice;
    }

    public static InvoiceLineDto map(InvoiceLine invoiceLine) {
        InvoiceLineDto invoiceLineDto = map(invoiceLine, InvoiceLineDto.class);

        if (invoiceLine.getInvoice() != null)
            invoiceLineDto.setInvoiceId(invoiceLine.getInvoice().getId());
        if (invoiceLine.getUnitOfMeasure() != null) {
            invoiceLineDto.setProductId(invoiceLine.getUnitOfMeasure().getProduct().getId());
            invoiceLineDto.setMeasurementId(invoiceLine.getUnitOfMeasure().getMeasurement().getId());
            invoiceLineDto.setMeasurementName(invoiceLine.getUnitOfMeasure().getMeasurement().getName());
        }

        return invoiceLineDto;
    }

    public static InvoiceLine map(InvoiceLineDto invoiceLineDto) {
        InvoiceLine invoiceLine = map(invoiceLineDto, InvoiceLine.class);

        if (invoiceLineDto.getInvoiceId() != null)
            invoiceLine.setInvoice(Invoice.builder().id(invoiceLineDto.getInvoiceId()).build());
        if (invoiceLineDto.getProductId() != null && invoiceLineDto.getMeasurementId() != null)
            invoiceLine.setUnitOfMeasure(UnitOfMeasure.builder()
                    .product(Product.builder().id(invoiceLineDto.getProductId()).build())
                    .measurement(Measurement.builder().id(invoiceLineDto.getMeasurementId()).build())
                    .build());

        return invoiceLine;
    }

    public static LotAllocationDto map(LotAllocation lotAllocation) {
        LotAllocationDto lotAllocationDto = map(lotAllocation, LotAllocationDto.class);

        if (lotAllocation.getInvoiceLine() != null) {
            lotAllocationDto.setInvoiceId(lotAllocation.getInvoiceLine().getInvoice().getId());
            if (lotAllocation.getInvoiceLine().getUnitOfMeasure() != null) {
                lotAllocationDto.setProductId(lotAllocation.getInvoiceLine().getUnitOfMeasure().getProduct().getId());
                lotAllocationDto.setMeasurementId(lotAllocation.getInvoiceLine().getUnitOfMeasure().getMeasurement().getId());
            }
            lotAllocationDto.setInvoiceLineType(lotAllocation.getInvoiceLine().getType());
        }
        if (lotAllocation.getLot() != null)
            lotAllocationDto.setLotId(lotAllocation.getLot().getId());

        return lotAllocationDto;
    }

    public static LotDto map(Lot lot) {
        LotDto lotDto = map(lot, LotDto.class);

        if (lot.getProduct() != null)
            lotDto.setProductId(lot.getProduct().getId());

        return lotDto;
    }

    public static Lot map(LotDto lotDto) {
        Lot lot = map(lotDto, Lot.class);

        if (lotDto.getProductId() != null)
            lot.setProduct(Product.builder().id(lotDto.getProductId()).build());

        return lot;
    }

    public static ProductDto map(Product product) {
        ProductDto productDto = map(product, ProductDto.class);

        if (product.getBaseUnitOfMeasure() != null) {
            productDto.setBaseMeasurementId(product.getBaseUnitOfMeasure().getMeasurement().getId());
            productDto.setBaseMeasurementName(product.getBaseUnitOfMeasure().getMeasurement().getName());
        }

        return productDto;
    }

    public static Product map(ProductDto productDto) {
        Product product = map(productDto, Product.class);

        if (productDto.getBaseMeasurementId() != null)
            product.setBaseUnitOfMeasure(UnitOfMeasure.builder()
                    .product(Product.builder().id(productDto.getId()).build())
                    .measurement(Measurement.builder().id(productDto.getBaseMeasurementId()).build())
                    .build());

        return product;
    }

    public static PromotionActionDto map(PromotionAction promotionAction) {
        PromotionActionDto promotionActionDto = map(promotionAction, PromotionActionDto.class);

        if (promotionAction.getPromotion() != null)
            promotionActionDto.setPromotionId(promotionAction.getPromotion().getId());
        if (promotionAction.getProductUom() != null) {
            promotionActionDto.setProductId(promotionAction.getProductUom().getProduct().getId());
            promotionActionDto.setMeasurementId(promotionAction.getProductUom().getMeasurement().getId());
            promotionActionDto.setMeasurementName(promotionAction.getProductUom().getMeasurement().getName());
        }

        return promotionActionDto;
    }

    public static PromotionAction map(PromotionActionDto promotionActionDto) {
        PromotionAction promotionAction = map(promotionActionDto, PromotionAction.class);

        if (promotionActionDto.getPromotionId() != null)
            promotionAction.setPromotion(Promotion.builder().id(promotionActionDto.getPromotionId()).build());
        if (promotionActionDto.getProductId() != null && promotionActionDto.getMeasurementId() != null)
            promotionAction.setProductUom(UnitOfMeasure.builder()
                    .product(Product.builder().id(promotionActionDto.getProductId()).build())
                    .measurement(Measurement.builder().id(promotionActionDto.getMeasurementId()).build())
                    .build());

        return promotionAction;
    }

    public static PromotionConditionDto map(PromotionCondition promotionCondition) {
        PromotionConditionDto promotionConditionDto = map(promotionCondition, PromotionConditionDto.class);

        if (promotionCondition.getPromotion() != null)
            promotionConditionDto.setPromotionId(promotionCondition.getPromotion().getId());
        if (promotionCondition.getProductUom() != null) {
            promotionConditionDto.setProductId(promotionCondition.getProductUom().getProduct().getId());
            promotionConditionDto.setMeasurementId(promotionCondition.getProductUom().getMeasurement().getId());
            promotionConditionDto.setMeasurementName(promotionCondition.getProductUom().getMeasurement().getName());
        }

        return promotionConditionDto;
    }

    public static PromotionCondition map(PromotionConditionDto promotionConditionDto) {
        PromotionCondition promotionCondition = map(promotionConditionDto, PromotionCondition.class);

        if (promotionConditionDto.getPromotionId() != null)
            promotionCondition.setPromotion(Promotion.builder().id(promotionConditionDto.getPromotionId()).build());
        if (promotionConditionDto.getProductId() != null && promotionConditionDto.getMeasurementId() != null)
            promotionCondition.setProductUom(UnitOfMeasure.builder()
                    .product(Product.builder().id(promotionConditionDto.getProductId()).build())
                    .measurement(Measurement.builder().id(promotionConditionDto.getMeasurementId()).build())
                    .build());

        return promotionCondition;
    }

    public static ShiftDto map(Shift shift) {
        ShiftDto shiftDto = map(shift, ShiftDto.class);

        if (shift.getStaff() != null) {
            shiftDto.setStaffId(shift.getStaff().getId());
            shiftDto.setStaffUsername(shift.getStaff().getUsername());
        }
        if (shift.getClosedByStaff() != null) {
            shiftDto.setClosedByStaffId(shift.getClosedByStaff().getId());
            shiftDto.setClosedByStaffUsername(shift.getClosedByStaff().getUsername());
        }

        return shiftDto;
    }

    public static Shift map(ShiftDto shiftDto) {
        Shift shift = map(shiftDto, Shift.class);

        if (shiftDto.getStaffId() != null)
            shift.setStaff(Staff.builder().id(shiftDto.getStaffId()).build());
        if (shiftDto.getClosedByStaffId() != null)
            shift.setClosedByStaff(Staff.builder().id(shiftDto.getClosedByStaffId()).build());

        return shift;
    }

    public static UnitOfMeasureDto map(UnitOfMeasure unitOfMeasure) {
        UnitOfMeasureDto unitOfMeasureDto = map(unitOfMeasure, UnitOfMeasureDto.class);

        if (unitOfMeasure.getProduct() != null)
            unitOfMeasureDto.setProductId(unitOfMeasure.getProduct().getId());
        if (unitOfMeasure.getMeasurement() != null) {
            unitOfMeasureDto.setMeasurementId(unitOfMeasure.getMeasurement().getId());
            unitOfMeasureDto.setMeasurementName(unitOfMeasure.getMeasurement().getName());
        }

        return unitOfMeasureDto;
    }

    public static UnitOfMeasure map(UnitOfMeasureDto unitOfMeasureDto) {
        UnitOfMeasure unitOfMeasure = map(unitOfMeasureDto, UnitOfMeasure.class);

        if (unitOfMeasureDto.getProductId() != null)
            unitOfMeasure.setProduct(Product.builder().id(unitOfMeasureDto.getProductId()).build());
        if (unitOfMeasureDto.getMeasurementId() != null)
            unitOfMeasure.setMeasurement(Measurement.builder().id(unitOfMeasureDto.getMeasurementId()).build());

        return unitOfMeasure;
    }
}
