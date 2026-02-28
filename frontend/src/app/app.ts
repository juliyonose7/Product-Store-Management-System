import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from './core/models/product.model';
import { ProductService } from './core/services/product.service';
import { Sale } from './core/models/sale.model';
import { SaleService } from './core/services/sale.service';
import { AuthService } from './core/services/auth.service';
import { DashboardMetrics } from './core/models/metrics.model';
import { MetricsService } from './core/services/metrics.service';
import { Chart, registerables } from 'chart.js';
import { UserConnectionStatus } from './core/services/user.service';
import { UserService } from './core/services/user.service';

Chart.register(...registerables);

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy, AfterViewChecked {
  activeTab: 'dashboard' | 'ventas' | 'productos' | 'cuenta' = 'dashboard';
  products: Product[] = [];
  sales: Sale[] = [];
  salesActivity: Sale[] = [];
  metrics: DashboardMetrics | null = null;
  loading = true;
  error = '';
  saleError = '';
  saleSuccess = '';
  productError = '';
  productSuccess = '';
  userError = '';
  userSuccess = '';
  passwordError = '';
  passwordSuccess = '';
  loginError = '';
  totalUsersRegistered = 0;
  connectedUsers = 0;
  usersStatus: UserConnectionStatus[] = [];
  usersOverviewError = '';
  adminActionSuccess = '';
  adminActionError = '';
  showDeleteUserModal = false;
  pendingDeleteUsername = '';
  resetPasswordForm = {
    username: '',
    temporaryPassword: ''
  };
  isAuthenticated = false;
  firstAccessRequired = false;
  username = '';
  role = '';
  sessionRemainingSeconds = 0;
  isPasswordVisible = false;
  private dailyRevenueChart: Chart<'bar' | 'line'> | null = null;
  private dailyUnitsChart: Chart<'bar' | 'line'> | null = null;
  private topProductsChart: Chart<'doughnut'> | null = null;

  private sessionInterval: ReturnType<typeof setInterval> | null = null;
  private selectedProductsCsvFile: File | null = null;

  credentials = {
    username: '',
    password: ''
  };

  firstAccessForm = {
    username: '',
    temporaryPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  saleForm = {
    productId: 0,
    quantity: 1
  };

  productForm = {
    name: '',
    price: 0,
    stock: 0
  };

  createUserForm = {
    username: '',
    role: 'CAJERO' as 'ADMIN' | 'CAJERO',
    temporaryPassword: ''
  };

  changePasswordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  constructor(
    private readonly authService: AuthService,
    private readonly userService: UserService,
    private readonly productService: ProductService,
    private readonly saleService: SaleService,
    private readonly metricsService: MetricsService
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
    this.destroyCharts();
  }

  ngAfterViewChecked(): void {
    if (
      this.isAuthenticated &&
      this.activeTab === 'dashboard' &&
      !this.loading &&
      !this.error &&
      this.metrics &&
      !this.dailyRevenueChart
    ) {
      this.renderCharts();
    }
  }

  login(): void {
    this.loginError = '';
    this.loading = true;

    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.isAuthenticated = true;
        this.activeTab = 'dashboard';
        this.username = this.authService.getUsername();
        this.role = this.authService.getRole();
        this.credentials.password = '';
        this.firstAccessRequired = false;
        this.initSessionAutomation();
        this.loadUsersOverview();
        this.loadDashboardData();
      },
      error: (err) => {
        this.loading = false;
        const message = err?.error?.message;
        if (message === 'FIRST_ACCESS_PASSWORD_CHANGE_REQUIRED') {
          this.firstAccessRequired = true;
          this.firstAccessForm.username = this.credentials.username.trim();
          this.firstAccessForm.temporaryPassword = this.credentials.password;
          this.loginError = 'Primer acceso detectado. Debes crear una nueva contraseña.';
          return;
        }
        this.loginError = 'Credenciales inválidas';
      }
    });
  }

  get isAdmin(): boolean {
    return this.role === 'ROLE_ADMIN';
  }

  completeFirstAccess(): void {
    this.loginError = '';

    if (!this.firstAccessForm.newPassword || this.firstAccessForm.newPassword.length < 6) {
      this.loginError = 'La nueva contraseña debe tener al menos 6 caracteres.';
      return;
    }

    if (this.firstAccessForm.newPassword !== this.firstAccessForm.confirmPassword) {
      this.loginError = 'La confirmación no coincide con la nueva contraseña.';
      return;
    }

    this.authService.completeFirstAccess({
      username: this.firstAccessForm.username,
      temporaryPassword: this.firstAccessForm.temporaryPassword,
      newPassword: this.firstAccessForm.newPassword
    }).subscribe({
      next: () => {
        this.firstAccessRequired = false;
        this.credentials.username = this.firstAccessForm.username;
        this.credentials.password = this.firstAccessForm.newPassword;
        this.firstAccessForm = {
          username: '',
          temporaryPassword: '',
          newPassword: '',
          confirmPassword: ''
        };
        this.login();
      },
      error: (err) => {
        this.loginError = err?.error?.message || 'No se pudo completar el primer acceso.';
      }
    });
  }

  showPassword(): void {
    this.isPasswordVisible = true;
  }

  hidePassword(): void {
    this.isPasswordVisible = false;
  }

  logout(): void {
    this.authService.stopAutoRefresh();
    this.stopSessionCountdown();
    this.authService.logout();
    this.isAuthenticated = false;
    this.activeTab = 'dashboard';
    this.username = '';
    this.role = '';
    this.sessionRemainingSeconds = 0;
    this.products = [];
    this.sales = [];
    this.salesActivity = [];
    this.metrics = null;
    this.error = '';
    this.saleError = '';
    this.saleSuccess = '';
    this.productError = '';
    this.productSuccess = '';
    this.userError = '';
    this.userSuccess = '';
    this.passwordError = '';
    this.passwordSuccess = '';
    this.firstAccessRequired = false;
    this.credentials.password = '';
    this.totalUsersRegistered = 0;
    this.connectedUsers = 0;
    this.usersStatus = [];
    this.usersOverviewError = '';
    this.adminActionSuccess = '';
    this.adminActionError = '';
    this.showDeleteUserModal = false;
    this.pendingDeleteUsername = '';
    this.resetPasswordForm = {
      username: '',
      temporaryPassword: ''
    };
    this.firstAccessForm = {
      username: '',
      temporaryPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
    this.createUserForm = {
      username: '',
      role: 'CAJERO',
      temporaryPassword: ''
    };
    this.changePasswordForm = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
    this.selectedProductsCsvFile = null;
    this.destroyCharts();
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

  setActiveTab(tab: 'dashboard' | 'ventas' | 'productos' | 'cuenta'): void {
    this.activeTab = tab;

    if (tab === 'dashboard') {
      setTimeout(() => this.renderCharts(), 0);
      return;
    }

    if (tab === 'cuenta') {
      this.loadUsersOverview();
    }

    this.destroyCharts();
  }

  createProduct(): void {
    this.productError = '';
    this.productSuccess = '';

    if (!this.productForm.name.trim() || this.productForm.price <= 0 || this.productForm.stock < 0) {
      this.productError = 'Completa nombre, precio mayor a 0 y stock válido.';
      return;
    }

    this.productService.createProduct({
      name: this.productForm.name.trim(),
      price: this.productForm.price,
      stock: this.productForm.stock
    }).subscribe({
      next: () => {
        this.productSuccess = 'Producto creado correctamente.';
        this.productForm = { name: '', price: 0, stock: 0 };
        this.loadDashboardData();
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.productError = err?.error?.message || 'No se pudo crear el producto.';
      }
    });
  }

  onProductsCsvSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedProductsCsvFile = input.files?.[0] ?? null;
  }

  importProductsCsv(): void {
    this.productError = '';
    this.productSuccess = '';

    if (!this.selectedProductsCsvFile) {
      this.productError = 'Selecciona un archivo CSV.';
      return;
    }

    this.productService.importProductsCsv(this.selectedProductsCsvFile).subscribe({
      next: (result) => {
        const firstError = result.errors.length > 0 ? ` Error: ${result.errors[0]}` : '';
        this.productSuccess = `Importación finalizada. Creados: ${result.created}. Omitidos: ${result.skipped}.${firstError}`;
        this.selectedProductsCsvFile = null;
        this.loadDashboardData();
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.productError = err?.error?.message || 'No se pudo importar el CSV.';
      }
    });
  }

  createUser(): void {
    this.userError = '';
    this.userSuccess = '';
    this.adminActionSuccess = '';
    this.adminActionError = '';

    if (!this.createUserForm.username.trim() || !this.createUserForm.temporaryPassword.trim()) {
      this.userError = 'Completa usuario y contraseña temporal.';
      return;
    }

    if (this.createUserForm.temporaryPassword.trim().length < 6) {
      this.userError = 'La contraseña temporal debe tener al menos 6 dígitos.';
      return;
    }

    this.userService.createUser({
      username: this.createUserForm.username.trim(),
      role: this.createUserForm.role,
      temporaryPassword: this.createUserForm.temporaryPassword
    }).subscribe({
      next: (created) => {
        this.userSuccess = `Usuario ${created.username} creado. Debe cambiar contraseña en su primer acceso.`;
        this.createUserForm = {
          username: '',
          role: 'CAJERO',
          temporaryPassword: ''
        };
        this.loadUsersOverview();
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.userError = err?.error?.message || 'No se pudo crear el usuario.';
      }
    });
  }

  changeOwnPassword(): void {
    this.passwordError = '';
    this.passwordSuccess = '';

    if (!this.changePasswordForm.currentPassword || !this.changePasswordForm.newPassword) {
      this.passwordError = 'Completa contraseña actual y nueva contraseña.';
      return;
    }

    if (this.changePasswordForm.newPassword.length < 6) {
      this.passwordError = 'La nueva contraseña debe tener al menos 6 caracteres.';
      return;
    }

    if (this.changePasswordForm.newPassword !== this.changePasswordForm.confirmPassword) {
      this.passwordError = 'La confirmación no coincide con la nueva contraseña.';
      return;
    }

    this.authService.changeOwnPassword({
      currentPassword: this.changePasswordForm.currentPassword,
      newPassword: this.changePasswordForm.newPassword
    }).subscribe({
      next: () => {
        this.passwordSuccess = 'Contraseña actualizada correctamente.';
        this.changePasswordForm = {
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        };
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.passwordError = err?.error?.message || 'No se pudo cambiar la contraseña.';
      }
    });
  }

  toggleUserEnabled(user: UserConnectionStatus): void {
    if (user.username === this.username) {
      this.adminActionError = 'No puedes desactivar o activar tu propio usuario.';
      this.adminActionSuccess = '';
      return;
    }

    this.adminActionError = '';
    this.adminActionSuccess = '';

    this.userService.updateUserEnabled(user.username, { enabled: !user.enabled }).subscribe({
      next: (updated) => {
        const nextState = updated.enabled ? 'activado' : 'desactivado';
        this.adminActionSuccess = `Usuario ${updated.username} ${nextState} correctamente.`;
        this.loadUsersOverview();
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.adminActionError = err?.error?.message || 'No se pudo actualizar el estado del usuario.';
      }
    });
  }

  resetUserPassword(): void {
    if (!this.resetPasswordForm.username.trim() || !this.resetPasswordForm.temporaryPassword.trim()) {
      this.adminActionError = 'Selecciona usuario y define contraseña temporal.';
      this.adminActionSuccess = '';
      return;
    }

    if (this.resetPasswordForm.temporaryPassword.trim().length < 6) {
      this.adminActionError = 'La contraseña temporal debe tener al menos 6 dígitos.';
      this.adminActionSuccess = '';
      return;
    }

    if (this.resetPasswordForm.username.trim() === this.username) {
      this.adminActionError = 'No puedes reiniciar tu propia contraseña desde esta sección.';
      this.adminActionSuccess = '';
      return;
    }

    this.adminActionError = '';
    this.adminActionSuccess = '';

    this.userService.resetUserPassword(this.resetPasswordForm.username.trim(), {
      temporaryPassword: this.resetPasswordForm.temporaryPassword
    }).subscribe({
      next: (updated) => {
        this.adminActionSuccess = `Contraseña de ${updated.username} reiniciada. Debe cambiarla en su próximo acceso.`;
        this.resetPasswordForm = {
          username: '',
          temporaryPassword: ''
        };
        this.loadUsersOverview();
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.adminActionError = err?.error?.message || 'No se pudo reiniciar la contraseña del usuario.';
      }
    });
  }

  requestDeleteUser(username: string): void {
    if (username === this.username) {
      this.adminActionError = 'No puedes eliminar tu propio usuario.';
      this.adminActionSuccess = '';
      return;
    }

    this.adminActionError = '';
    this.adminActionSuccess = '';
    this.pendingDeleteUsername = username;
    this.showDeleteUserModal = true;
  }

  cancelDeleteUser(): void {
    this.showDeleteUserModal = false;
    this.pendingDeleteUsername = '';
  }

  confirmDeleteUser(): void {
    const username = this.pendingDeleteUsername;
    if (!username) {
      this.showDeleteUserModal = false;
      return;
    }

    this.showDeleteUserModal = false;
    this.pendingDeleteUsername = '';

    this.deleteUser(username);
  }

  deleteUser(username: string): void {
    if (username === this.username) {
      this.adminActionError = 'No puedes eliminar tu propio usuario.';
      this.adminActionSuccess = '';
      return;
    }

    this.adminActionError = '';
    this.adminActionSuccess = '';

    this.userService.deleteUser(username).subscribe({
      next: () => {
        this.adminActionSuccess = `Usuario ${username} eliminado correctamente.`;
        if (this.resetPasswordForm.username === username) {
          this.resetPasswordForm.username = '';
        }
        this.loadUsersOverview();
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.adminActionError = err?.error?.message || 'No se pudo eliminar el usuario.';
      }
    });
  }

  downloadSalesCsv(): void {
    this.saleService.exportSalesCsv().subscribe({
      next: (blob) => this.downloadBlob(blob, 'sales-report.csv'),
      error: (err) => this.handleDownloadError(err)
    });
  }

  downloadMetricsCsv(): void {
    this.metricsService.exportMetricsCsv().subscribe({
      next: (blob) => this.downloadBlob(blob, 'metrics-report.csv'),
      error: (err) => this.handleDownloadError(err)
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
        this.loadMetrics();
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

  private loadMetrics(): void {
    this.metricsService.getDashboardMetrics().subscribe({
      next: (items) => {
        this.metrics = items;
        this.error = '';
        this.loading = false;
        this.loadUsersOverview();
        setTimeout(() => this.renderCharts(), 0);
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }
        this.error = 'No se pudieron cargar las métricas del dashboard.';
        this.loading = false;
      }
    });
  }

  private loadUsersOverview(): void {
    this.usersOverviewError = '';

    this.userService.getUsersCount().subscribe({
      next: (response) => {
        this.totalUsersRegistered = response.totalUsers;
        this.connectedUsers = response.connectedUsers;
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }

        this.usersOverviewError = 'No se pudo cargar el resumen de usuarios.';
      }
    });

    this.userService.getUsersStatus().subscribe({
      next: (items) => {
        this.usersStatus = items;
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          this.logout();
          this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
          return;
        }

        this.usersOverviewError = 'No se pudo cargar la tabla de usuarios.';
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

  private downloadBlob(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }

  private handleDownloadError(err: any): void {
    if (err?.status === 401 || err?.status === 403) {
      this.logout();
      this.loginError = 'Tu sesión expiró. Inicia sesión nuevamente.';
      return;
    }
    this.error = 'No se pudo descargar el reporte CSV.';
  }

  private renderCharts(): void {
    if (!this.metrics || this.activeTab !== 'dashboard') {
      return;
    }

    const dailySales = this.metrics.dailySales.length > 0
      ? [...this.metrics.dailySales].reverse()
      : this.buildDailySalesFromHistory();
    const latestDailyRevenue = dailySales.length > 0
      ? Number(dailySales[dailySales.length - 1].totalRevenue) || 0
      : 0;
    const latestDailySales = dailySales.length > 0
      ? Number(dailySales[dailySales.length - 1].totalSales) || 0
      : 0;
    const comparisonLabels = ['Diario', 'Total mes'];

    const totalRevenueMonth = Number(this.metrics.summary.totalRevenueMonth) || 0;
    const totalSalesMonth = Number(this.metrics.summary.totalSalesMonth) || 0;

    const topProducts = this.metrics.topProducts.length > 0
      ? this.metrics.topProducts
      : this.buildTopProductsFromHistory();
    const topProductsLabels = topProducts.length > 0 ? topProducts.map((item) => item.productName) : ['Sin datos'];
    const topProductsData = topProducts.length > 0 ? topProducts.map((item) => item.totalRevenue) : [0];

    this.dailyRevenueChart?.destroy();
    this.dailyUnitsChart?.destroy();
    this.topProductsChart?.destroy();

    const revenueCanvas = document.getElementById('dailyRevenueChart') as HTMLCanvasElement | null;
    const unitsCanvas = document.getElementById('dailyUnitsChart') as HTMLCanvasElement | null;
    const topProductsCanvas = document.getElementById('topProductsChart') as HTMLCanvasElement | null;

    if (revenueCanvas) {
      this.dailyRevenueChart = new Chart(revenueCanvas, {
        type: 'bar',
        data: {
          labels: comparisonLabels,
          datasets: [
            {
              label: 'Ingresos',
              data: [latestDailyRevenue, totalRevenueMonth],
              borderColor: '#1f6feb',
              backgroundColor: 'rgba(31, 111, 235, 0.75)',
              borderWidth: 1
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: true }
          }
        }
      });
    }

    if (unitsCanvas) {
      this.dailyUnitsChart = new Chart(unitsCanvas, {
        type: 'bar',
        data: {
          labels: comparisonLabels,
          datasets: [
            {
              label: 'Ventas',
              data: [latestDailySales, totalSalesMonth],
              backgroundColor: 'rgba(22, 101, 52, 0.75)',
              borderColor: '#166534',
              borderWidth: 1
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: true }
          }
        }
      });
    }

    if (topProductsCanvas) {
      this.topProductsChart = new Chart(topProductsCanvas, {
        type: 'doughnut',
        data: {
          labels: topProductsLabels,
          datasets: [
            {
              label: 'Ingresos por producto',
              data: topProductsData,
              backgroundColor: ['#1f6feb', '#16a34a', '#f59e0b', '#dc2626', '#7c3aed']
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'bottom'
            }
          }
        }
      });
    }

    if (this.activeTab === 'dashboard' && (!this.dailyRevenueChart || !this.dailyUnitsChart || !this.topProductsChart)) {
      setTimeout(() => this.renderCharts(), 50);
    }
  }

  private destroyCharts(): void {
    this.dailyRevenueChart?.destroy();
    this.dailyUnitsChart?.destroy();
    this.topProductsChart?.destroy();
    this.dailyRevenueChart = null;
    this.dailyUnitsChart = null;
    this.topProductsChart = null;
  }

  private buildDailySalesFromHistory(): Array<{
    saleDate: string;
    totalRevenue: number;
    totalQuantity: number;
    totalSales: number;
  }> {
    const grouped = new Map<string, { totalRevenue: number; totalQuantity: number; totalSales: number }>();

    for (const sale of this.sales) {
      const current = grouped.get(sale.saleDate) ?? { totalRevenue: 0, totalQuantity: 0, totalSales: 0 };
      current.totalRevenue += Number(sale.subtotal) || 0;
      current.totalQuantity += Number(sale.quantity) || 0;
      current.totalSales += 1;
      grouped.set(sale.saleDate, current);
    }

    return [...grouped.entries()]
      .sort(([dateA], [dateB]) => dateA.localeCompare(dateB))
      .map(([saleDate, totals]) => ({
        saleDate,
        totalRevenue: totals.totalRevenue,
        totalQuantity: totals.totalQuantity,
        totalSales: totals.totalSales
      }));
  }

  private buildTopProductsFromHistory(): Array<{
    productName: string;
    totalRevenue: number;
  }> {
    const grouped = new Map<string, { totalRevenue: number }>();

    for (const sale of this.sales) {
      const key = sale.productName;
      const current = grouped.get(key) ?? { totalRevenue: 0 };
      current.totalRevenue += Number(sale.subtotal) || 0;
      grouped.set(key, current);
    }

    return [...grouped.entries()]
      .map(([productName, totals]) => ({ productName, totalRevenue: totals.totalRevenue }))
      .sort((a, b) => b.totalRevenue - a.totalRevenue)
      .slice(0, 5);
  }
}
