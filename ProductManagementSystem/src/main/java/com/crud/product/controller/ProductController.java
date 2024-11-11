package com.crud.product.controller;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslProperties.Bundles.Watch.File;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.crud.product.model.Product;
import com.crud.product.model.ProductDto;
import com.crud.product.service.ProductRepository;


import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductRepository repo;
	
	@GetMapping({"","/"})
	public String showProductList(Model model) {
		List<Product> products = repo.findAll();
		model.addAttribute("products",products);
		return "products/index";
	}
	
	@GetMapping("/create")
	public String showCreatePage(Model model) {
	    ProductDto productDto = new ProductDto();
	    model.addAttribute("productDto", productDto);
	    return "products/createProduct";
	}

	@PostMapping("/create")
	public String createProduct(
			@Valid @ModelAttribute ProductDto productDto,
			BindingResult result) throws IOException {
		
		if (productDto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
		}
		
		if (result.hasErrors()) {
			return "products/createProduct";
		}
		// Handle the image file and save it to disk
        String imageFileName = null;
        if (!productDto.getImageFile().isEmpty()) {
            MultipartFile file = productDto.getImageFile();
			imageFileName = file.getOriginalFilename();
			String uploadDir = "src/main/resources/static/images/";
        }

        // Convert ProductDto to Product entity
        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setImageFileName(imageFileName);  // Save the image file name
        product.setCreatedAt(new java.util.Date());

        // Save product to the database
        repo.save(product);

        // Redirect to the product list page
        return "redirect:/products";
	}
	
	@GetMapping("/update/{id}")
	public String showEditPage(@PathVariable int id, Model model) {
	    try {
	        // Retrieve product by id
	        Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

	        // Create a productDto from the product entity to bind to the form
	        ProductDto productDto = new ProductDto();
	        productDto.setName(product.getName());
	        productDto.setBrand(product.getBrand());
	        productDto.setCategory(product.getCategory());
	        productDto.setPrice(product.getPrice());
	        productDto.setDescription(product.getDescription());

	        // Add both product and productDto to the model
	        model.addAttribute("product", product);   // For displaying read-only fields like product ID and created date
	        model.addAttribute("productDto", productDto);  // For form data binding

	        return "products/updateProduct"; // Return the view for editing the product
	    } catch (Exception ex) {
	        System.out.println("Exception: " + ex.getMessage());
	        return "redirect:/products"; // In case of error, redirect to the product list
	    }
	}

	@PostMapping("/update/{id}")
	public String updateProduct(@PathVariable int id,
	                            @ModelAttribute("productDto") ProductDto productDto,
	                            @RequestParam("imageFile") MultipartFile imageFile,
	                            BindingResult result, 
	                            Model model) {
	    if (result.hasErrors()) {
	        // If there are form errors, return to the form page
	        model.addAttribute("product", productDto);
	        return "products/updateProduct";
	    }
	    
	    try {
	        Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
	        
	        // Update product details from productDto
	        product.setName(productDto.getName());
	        product.setBrand(productDto.getBrand());
	        product.setCategory(productDto.getCategory());
	        product.setPrice(productDto.getPrice());
	        product.setDescription(productDto.getDescription());
	        
	        // Handle image file if provided
	        if (!imageFile.isEmpty()) {
	            // Process the file upload, e.g., saving the image and updating the file name
	            String imageFileName = imageFile.getOriginalFilename();
	            product.setImageFileName(imageFileName); // Assuming you store file names in the database
	            // Save the image to a location on the server (not shown here)
	        }

	        // Save the updated product
	        repo.save(product);
	        
	        return "redirect:/products"; // Redirect to the product list after update
	    } catch (Exception ex) {
	        model.addAttribute("error", "An error occurred while updating the product.");
	        return "products/updateProduct";
	    }
	}

//	@PostMapping("/delete")
//	public String deleteProduct(
//			@RequestParam int id) {
//		try {
//			Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
//			
//			Path imagePath = Paths.get("public/images/" + product.getImageFileName());
//			
//			try {
//				Files.delete(imagePath);
//			}
//			catch (Exception ex) {
//				System.out.println("Exception: " + ex.getMessage());
//			}
//			 repo.delete(product);
//		        System.out.println("Product deleted successfully: " + product.getId());
//		}
//		catch (Exception ex) {
//			System.out.println("Exception: " + ex.getMessage());
//		}
//		return "redirect:/products";
//	}
	
//	

	@PostMapping("/delete")
	public String deleteProduct(@RequestParam int id) {
	    try {
	        Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
	        
	        // Delete the image file if it exists
	        Path imagePath = Paths.get("src/main/resources/static/images/" + product.getImageFileName());
	        try {
	            Files.delete(imagePath);
	        } catch (Exception ex) {
	            System.out.println("Exception deleting image: " + ex.getMessage());
	        }

	        // Delete the product from the database
	        repo.delete(product);
	        System.out.println("Product deleted successfully: " + product.getId());
	    } catch (Exception ex) {
	        System.out.println("Exception: " + ex.getMessage());
	    }
	    return "redirect:/products"; // Redirect back to the product list
	}


}

