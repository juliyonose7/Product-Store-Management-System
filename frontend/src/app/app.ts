import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product } from './core/models/product.model';
import { ProductService } from './core/services/product.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  products: Product[] = [];
  loading = true;
  error = '';

  constructor(private readonly productService: ProductService) {}

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (items) => {
        this.products = items;
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar los productos. Verifica backend y base de datos.';
        this.loading = false;
      }
    });
  }
}
