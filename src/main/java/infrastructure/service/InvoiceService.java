package infrastructure.service;

import core.dto.InvoiceDto;

import java.util.List;

public interface InvoiceService {
    InvoiceDto create(InvoiceDto invoiceDto);
    InvoiceDto update(InvoiceDto invoiceDto);
    InvoiceDto findById(String id);
    List<InvoiceDto> loadAll();
}
