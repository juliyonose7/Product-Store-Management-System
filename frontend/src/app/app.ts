import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from './core/models/product.model';
import { ProductService } from './core/services/product.service';
import { Sale } from './core/models/sale.model';
import { SaleService } from './core/services/sale.service';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  products: Product[] = [];
  sales: Sale[] = [];
  salesActivity: Sale[] = [];
  loading = true;
  error = '';
  saleError = '';
  saleSuccess = '';
  loginError = '';
  isAuthenticated = false;
  username = '';
  role = '';
  sessionRemainingSeconds = 0;

  private sessionInterval: ReturnType<typeof setInterval> | null = null;

  credentials = {
    username: '',
    password: ''
  };

  saleForm = {
    productId: 0,
    quantity: 1
  };

  constructor(
    private readonly authService: AuthService,
    private readonly productService: ProductService,
    private readonly saleService: SaleService
  ) {}

  ngOnInit(): void {
    this.isAuthenticated = this.authService.isAuthenticated();
    this.username = this.authService.getUsername();
    this.role = this.authService.getRole();

    if (this.isAuthenticated) {
      this.initSessionAutomation();
      this.loadDashboardData();
      return;
    }

    this.loading = false;
  }

  ngOnDestroy(): void {
    this.stopSessionCountdown();
    this.authService.stopAutoRefresh();
  }

  login(): void {
    this.loginError = '';
    this.loading = true;

    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.isAuthenticated = true;
        this.username = this.authService.getUsername();
        this.role = this.authService.getRole();
        this.credentials.password = '';
        this.initSessionAutomation();
        this.loadDashboardData();
      },
      error: () => {
        this.loading = false;
        this.loginError = 'Credenciales inválidas';
      }
    });
  }

  logout(): void {
    this.authService.stopAutoRefresh();
    this.stopSessionCountdown();
    this.authService.logout();
    this.isAuthenticated = false;
    this.username = '';
    this.role = '';
    this.sessionRemainingSeconds = 0;
    this.products = [];
    this.sales = [];
    this.error = '';
    this.saleError = '';
    this.saleSuccess = '';
    this.credentials.password = '';
  }

  get sessionTimeLabel(): string {
    const minutes = Math.floor(this.sessionRemainingSeconds / 60)
      .toString()
      .padStart(2, '0');
    const seconds = (this.sessionRemainingSeconds % 60).toString().padStart(2, '0');
    return `${minutes}:${seconds}`;
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
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
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
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.error = 'No se pudieron cargar los productos. Verifica backend y base de datos.';
        this.loading = false;
      }
    });
  }

  private loadSales(): void {
    this.saleService.getSales().subscribe({
      next: (items) => {
        this.sales = items;
        this.loadSalesActivity();
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.error = 'No se pudo cargar el historial de ventas.';
        this.loading = false;
      }
    });
  }

  private loadSalesActivity(): void {
    this.saleService.getSalesActivity().subscribe({
      next: (items) => {
        this.salesActivity = items;
        this.error = '';
        this.loading = false;
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.error = 'No se pudo cargar la actividad reciente de ventas.';
        this.loading = false;
      }
    });
  }

  private initSessionAutomation(): void {
    this.authService.startAutoRefresh(() => {
      this.logout();
      this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
    });

    this.startSessionCountdown();
  }

  private startSessionCountdown(): void {
    this.stopSessionCountdown();
    this.sessionRemainingSeconds = this.authService.getRemainingSessionSeconds();

    this.sessionInterval = setInterval(() => {
      this.sessionRemainingSeconds = this.authService.getRemainingSessionSeconds();
    }, 1000);
  }

  private stopSessionCountdown(): void {
    if (this.sessionInterval) {
      clearInterval(this.sessionInterval);
      this.sessionInterval = null;
    }
  }
}
