package com.crud.product.service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crud.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
