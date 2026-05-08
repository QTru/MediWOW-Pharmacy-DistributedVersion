package infrastructure.service;

import core.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto create(ProductDto productDto);
    ProductDto update(ProductDto productDto);
    ProductDto findById(String id);
    List<ProductDto> loadAll();
}
