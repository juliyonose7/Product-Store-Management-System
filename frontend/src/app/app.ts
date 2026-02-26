import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from './core/models/product.model';
import { ProductService } from './core/services/product.service';
import { Sale } from './core/models/sale.model';
import { SaleService } from './core/services/sale.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  products: Product[] = [];
  sales: Sale[] = [];
  loading = true;
  error = '';
  saleError = '';
  saleSuccess = '';

  saleForm = {
    productId: 0,
    quantity: 1
  };

  constructor(
    private readonly productService: ProductService,
    private readonly saleService: SaleService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  registerSale(): void {
    if (!this.saleForm.productId || this.saleForm.quantity < 1) {
      this.saleError = 'Selecciona un producto y cantidad válida.';
      this.saleSuccess = '';
      return;
    }

    this.saleService.createSale({
      productId: this.saleForm.productId,
      quantity: this.saleForm.quantity
    }).subscribe({
      next: () => {
        this.saleError = '';
        this.saleSuccess = 'Venta registrada correctamente.';
        this.saleForm.quantity = 1;
        this.loadDashboardData();
      },
      error: (err) => {
        this.saleSuccess = '';
        this.saleError = err?.error?.message || 'No se pudo registrar la venta.';
      }
    });
  }

  private loadDashboardData(): void {
    this.loading = true;

    this.productService.getProducts().subscribe({
      next: (items) => {
        this.products = items;
        this.loadSales();
      },
      error: () => {
        this.error = 'No se pudieron cargar los productos. Verifica backend y base de datos.';
        this.loading = false;
      }
    });
  }

  private loadSales(): void {
    this.saleService.getSales().subscribe({
      next: (items) => {
        this.sales = items;
        this.error = '';
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el historial de ventas.';
        this.loading = false;
      }
    });
  }
}
